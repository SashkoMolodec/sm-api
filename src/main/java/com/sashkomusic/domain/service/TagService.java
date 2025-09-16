package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.model.tag.TagCategory;
import com.sashkomusic.domain.repository.TagRepository;
import com.sashkomusic.web.dto.TagDto;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import com.sashkomusic.web.dto.ai.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TagService {
    private final AiService aiService;
    private final TagRepository tagRepository;

    public Map<TagCategory, List<String>> getDictionary() {
        return tagRepository.findAll().stream()
                .collect(Collectors.groupingBy(Tag::getCategory, Collectors.mapping(
                        Tag::getName, Collectors.toList()
                )));
    }

    public Set<String> getNamesDictionary() {
        return tagRepository.findAll().stream().map(Tag::getName).collect(Collectors.toSet());
    }

    public Tag create(TagDto tagDto) {
        Optional<Tag> tag = tagRepository.findByCategoryAndName(tagDto.category(), tagDto.name());
        return tag.orElseGet(() -> tagRepository.save(buildTag(tagDto)));
    }

    private Tag buildTag(TagDto tagDto) {
        TagCategory category = tagDto.category();
        if (tagDto.category() == null) {
            category = aiService.askTagCategory(tagDto.name());
        }
        String shade = askShade(tagDto.name(), category);


        return new Tag(tagDto.name(), category, shade);
    }

    private String askShade(String name, TagCategory category) {
        if (category.equals(TagCategory.OTHER)) return "gray";
        return aiService.askTagShade(name, category);
    }

    public Set<Tag> askTags(ItemCreateDto itemCreateDto) {
        List<TagResponse> suggestedTags = aiService.askTags(itemCreateDto, getDictionary());
        Map<Boolean, List<TagResponse>> groupedByPresence = suggestedTags.stream().collect(Collectors.partitioningBy(TagResponse::exists));

        Set<Tag> existingTags = groupedByPresence.get(true).stream()
                .map(existingTag -> tagRepository.findByCategoryAndName(existingTag.category(), existingTag.name()).get())
                .collect(Collectors.toSet());
        Set<Tag> newTags = createNewTags(groupedByPresence.get(false));

        Set<Tag> itemTags = new HashSet<>(existingTags);
        itemTags.addAll(newTags);
        return itemTags;
    }

    private Set<Tag> createNewTags(List<TagResponse> tagResponses) {
        Set<Tag> newTags = tagResponses.stream()
                .map(newTag -> new Tag(newTag.name(), newTag.category(), newTag.shade()))
                .collect(Collectors.toSet());

        tagRepository.saveAll(newTags);
        return newTags;
    }

    public void createTagsFromDocuments(List<TagDto> tags) {
        tags.forEach(this::create);
    }
}
