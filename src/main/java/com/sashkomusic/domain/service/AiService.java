package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.ItemFormat;
import com.sashkomusic.domain.model.tag.TagCategory;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import com.sashkomusic.web.dto.ai.TagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    public AiService(VectorStore vectorStore, ChatClient chatClient, EmbeddingModel embeddingModel) {
        this.chatClient = chatClient;
        this.embeddingModel = embeddingModel;
    }

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

    public List<TagResponse> askTags(ItemCreateDto item, Map<TagCategory, List<String>> availableTags) {
        String title = item.title();
        List<String> artists = item.artists();
        ItemFormat format = item.format();

        Prompt prompt = PromptTemplate.builder().resource(tagsAskPrompt).variables(
                Map.of("title", title, "artists", artists.toString(), "format", format.name(),
                        "availableTagCategories", TagCategory.concatenatedAll(),
                        "availableTags", availableTags.toString())).build().create();
        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<>() {
        });
    }

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

    public String askTagShade(String name, TagCategory category) {
        Prompt prompt = PromptTemplate.builder().resource(askTagShadePrompt)
                .variables(Map.of("name", name, "category", category.name())).build().create();

        return chatClient.prompt(prompt).call().content();
    }

    public TagCategory askTagCategory(String name) {
        Prompt prompt = PromptTemplate.builder().resource(askTagCategoryPrompt).variables(Map.of("name", name,
                "tagCategories", TagCategory.concatenatedAll())
        ).build().create();
        String category = chatClient.prompt(prompt).call().content();

        return TagCategory.findByName(category);
    }

    public float[] embedRaw(String text) {
        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        return response.getResults().getFirst().getOutput();
    }

    public String embedAsPgVectorLiteral(String text) {
        float[] vector = embedRaw(text);
        return toPgvectorLiteral(vector);
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
}
