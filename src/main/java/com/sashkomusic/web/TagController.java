package com.sashkomusic.web;

import com.sashkomusic.domain.service.TagService;
import com.sashkomusic.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {
    private final TagService tagService;

    @PostMapping("/from-documents")
    public void createFromDocuments(@RequestBody List<TagDto> tags) {
        tagService.createTagsFromDocuments(tags);
    }

    @PostMapping
    public void create(@RequestBody List<TagDto> tags) {
        tags.forEach(tagService::create);
    }
}
