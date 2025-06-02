package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Tag;
import com.sashkomusic.domain.model.TagCategory;
import com.sashkomusic.domain.repository.TagRepository;
import com.sashkomusic.web.dto.TagDto;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final ChatClient chatClient;
    private final static PromptTemplate askCategoryTemplate = new PromptTemplate("""
                Categorize tag by name {name} having options {tagCategories} with fallback
                to OTHER. Return ONLY ONE category constant in response without additional explanations.
            """);

    private final static PromptTemplate askShadeTemplate = new PromptTemplate("""
                Give color shade for a tag using it's name: {name} and category: {category}.
                Return only ONE name within CSS Named Colors palette without additional explanations.
            """);

    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource systemPromptTemplate;

    public TagService(TagRepository tagRepository, AnthropicChatModel chatModel) {
        this.tagRepository = tagRepository;
        this.chatClient = ChatClient.create(chatModel);
    }

    public Tag create(TagDto tagDto) {
        Optional<Tag> tag = tagRepository.findByCategoryAndName(tagDto.category(), tagDto.name());
        return tag.orElseGet(() -> tagRepository.save(buildTag(tagDto)));
    }

    private Tag buildTag(TagDto tagDto) {
        TagCategory category = tagDto.category();
        if (tagDto.category() == null) {
            category = askCategory(tagDto.name());
        }
        String shade = resolveShade(tagDto.name(), category);

        return new Tag(tagDto.name(), category, shade);
    }

    public TagCategory askCategory(String name) {
        Prompt prompt = askCategoryTemplate.create(Map.of("name", name,
                "tagCategories", TagCategory.concatenated())
        );
        String category = chatClient.prompt(prompt)
                .system(systemPromptTemplate).call().content();
        return TagCategory.valueOf(category);
    }

    private String resolveShade(String name, TagCategory category) {
        if (category.equals(TagCategory.OTHER)) return "gray";
        return askShade(name, category);
    }

    private String askShade(String name, TagCategory category) {
        Prompt prompt = askShadeTemplate.create(Map.of("name", name, "category", category.name()));
        return chatClient.prompt(prompt)
                .system(systemPromptTemplate).call().content();
    }
}
