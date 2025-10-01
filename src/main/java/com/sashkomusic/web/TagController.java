package com.sashkomusic.web;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.service.TagService;
import com.sashkomusic.web.dto.TagCategoryDto;
import com.sashkomusic.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {
    private final TagService tagService;

    @PostMapping
    public void create(@RequestBody List<TagDto> tags) {
        tags.forEach(tagService::create);
    }

    @GetMapping("/similar")
    public List<Tag> findSimilar(@RequestParam("query") String query,
                                 @RequestParam(value = "limit", defaultValue = "5") int limit,
                                 @RequestParam(value = "maxDistance", required = false) Double maxDistance) {
        return tagService.findMostSimilarByQuery(query, limit, maxDistance);
    }

    @GetMapping("/categories")
    public List<TagCategoryDto> getCategories() {
        return tagService.getTagCategories();
    }
}
