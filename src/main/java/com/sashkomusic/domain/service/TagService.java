package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.model.tag.TagCategory;
import com.sashkomusic.domain.repository.TagRepository;
import com.sashkomusic.web.dto.TagCategoryDto;
import com.sashkomusic.web.dto.TagDto;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import com.sashkomusic.web.dto.ai.TagResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TagService {
    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    @Lazy
    private final AiService aiService;
    private final TagRepository tagRepository;

    public List<Tag> findMostSimilarByQuery(String query, int limit, Double maxDistance) {
        String vector = aiService.embedAsPgVectorLiteral(query);
        return tagRepository.findMostSimilarWithinDistance(vector, maxDistance, limit);
    }

    public List<TagCategoryDto> getTagCategories() {
        return Arrays.stream(TagCategory.values())
                .map(category -> TagCategoryDto.of(category.getName(), category.getDescription()))
                .collect(Collectors.toList());
    }

    public Tag create(TagDto tagDto) {
        Optional<Tag> tag = tagRepository.findByCategoryAndName(tagDto.category(), tagDto.name());
        return tag.orElseGet(() -> tagRepository.save(buildTag(tagDto)));
    }

    public Set<Tag> askTags(ItemCreateDto itemCreateDto) {
        List<TagDto> suggestedTags = aiService.askTags(itemCreateDto);
        return suggestedTags.stream().map(this::create).collect(Collectors.toSet());
    }

    private Tag buildTag(TagDto tagDto) {
        TagCategory category = tagDto.category();
        if (tagDto.category() == null) {
            category = aiService.askTagCategory(tagDto.name());
        }
        String shade = askShade(tagDto.name(), category);

        Tag tag = new Tag(tagDto.name(), category, shade);
        setEmbedding(tagDto, tag);
        return tag;
    }

    private void setEmbedding(TagDto tagDto, Tag tag) {
        try {
            String embedding = aiService.embedAsPgVectorLiteral(tagDto.name());
            tag.setEmbedding(embedding);
        } catch (Exception e) {
            log.error("Could not create embedding for tag {}", tagDto.name(), e);
        }
    }

    private String askShade(String name, TagCategory category) {
        if (category.isShadeAutoGray()) return "gray";
        return aiService.askTagShade(name, category);
    }
}
