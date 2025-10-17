package com.sashkomusic.domain.service;

import com.sashkomusic.aop.UseWebSearchTool;
import com.sashkomusic.domain.model.ItemFormat;
import com.sashkomusic.domain.model.tag.TagCategory;
import com.sashkomusic.web.dto.TagDto;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.sashkomusic.domain.model.tag.TagCategory.*;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.ai.chat.memory.ChatMemory.DEFAULT_CONVERSATION_ID;

@Service
public class AiService {
    @Value("classpath:/promptTemplates/create/tagAsk.st")
    Resource tagsAskPrompt;

    @Value("classpath:/promptTemplates/search/tagExtract.st")
    Resource tagsExtractPrompt;

    @Value("classpath:/promptTemplates/create/tagCategoryAsk.st")
    Resource askTagCategoryPrompt;

    @Value("classpath:/promptTemplates/create/tagShadeAsk.st")
    Resource askTagShadePrompt;

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public AiService(VectorStore vectorStore, ChatClient chatClient, EmbeddingModel embeddingModel) {
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    //@UseWebSearchTool
    public String ask(String question, String conversationId) {
        return chatClient.prompt()
                .user(question)
                .advisors(advisorSpec ->
                        advisorSpec
                                //.param(FILTER_EXPRESSION, "artists == 'Elektryky'")
                                .param(CONVERSATION_ID, conversationId)
                )
                .call()
                .content();
    }

    public String ask(String question) {
        return ask(question, DEFAULT_CONVERSATION_ID);
    }

    @UseWebSearchTool
    public List<TagDto> askTags(ItemCreateDto item) {
        String title = item.title();
        List<String> artists = item.artists();
        ItemFormat format = item.format();

        Prompt prompt = PromptTemplate.builder().resource(tagsAskPrompt).variables(
                Map.of("title", title, "artists", artists.toString(), "format", format.name(),
                        "availableTagCategories", TagCategory.concatenatedAll())).build().create();

        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().filterExpression(buildDocsFilterExpression(item)).build())
                .build();

        return chatClient.prompt(prompt)
                .advisors(advisor)
                .call().entity(new ParameterizedTypeReference<>() {
                });
    }

    @UseWebSearchTool
    public String askTagShade(String name, TagCategory category) {
        Prompt prompt = PromptTemplate.builder().resource(askTagShadePrompt)
                .variables(Map.of("name", name, "category", category.name())).build().create();

        String raw = chatClient.prompt(prompt).call().content();
        return normalizeShadeColor(raw);
    }

    @UseWebSearchTool
    public TagCategory askTagCategory(String name) {
        Prompt prompt = PromptTemplate.builder().resource(askTagCategoryPrompt).variables(Map.of("name", name,
                "tagCategories", TagCategory.concatenatedAll())
        ).build().create();
        String category = chatClient.prompt(prompt).call().content();
        return TagCategory.findByName(category);
    }

    public String embedAsPgVectorLiteral(String text) {
        float[] vector = embedRaw(text);
        return toPgvectorLiteral(vector);
    }

    @UseWebSearchTool
    public Set<String> extractTags(String userQuery, Set<String> tagDictionary) {
        Prompt prompt = PromptTemplate.builder()
                .resource(tagsExtractPrompt)
                .variables(Map.of("userQuery", userQuery, "tagsList", tagDictionary.toString()))
                .build().create();

        return chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
    }

    private float[] embedRaw(String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        return response.getResults().getFirst().getOutput();
    }

    private String toPgvectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format(Locale.US, "%.8f", vector[i]));
        }
        sb.append(']');
        return sb.toString();
    }

    private String normalizeShadeColor(String raw) {
        if (raw == null) return "gray";
        String s = raw.trim();
        if (s.isEmpty()) return "gray";
        String[] lines = s.split("\\R");
        String candidate = s;
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                candidate = line;
                break;
            }
        }
        String onlyLetters = candidate.replaceAll("[^A-Za-z]", "");
        onlyLetters = onlyLetters.toLowerCase(Locale.ROOT);
        if (onlyLetters.isEmpty()) {
            onlyLetters = s.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
        }
        if (onlyLetters.isEmpty()) return "gray";
        return onlyLetters;
    }

    private static Filter.Expression buildDocsFilterExpression(ItemCreateDto item) {
        var b = new FilterExpressionBuilder();
        FilterExpressionBuilder.Op op = null;

        op = buildOrExpression(b, op, item.artists(), ARTIST.getName());
        op = buildOrExpression(b, op, item.artists(), RELATED_ARTIST.getName());
        op = op == null
                ? b.eq(ALBUM.getName(), item.title())
                : b.or(op, b.eq(ALBUM.getName(), item.title()));

        return op.build();
    }

    private static FilterExpressionBuilder.Op buildOrExpression(
            FilterExpressionBuilder builder,
            FilterExpressionBuilder.Op currentOp,
            List<String> values,
            String fieldName) {

        FilterExpressionBuilder.Op result = currentOp;
        for (String value : values) {
            if (result == null) {
                result = builder.eq(fieldName, value);
            } else {
                result = builder.or(result, builder.eq(fieldName, value));
            }
        }
        return result;
    }
}
