package com.sashkomusic.web;

import com.sashkomusic.domain.model.tag.TagCategory;
import com.sashkomusic.domain.service.AiService;
import com.sashkomusic.domain.service.SearchService;
import com.sashkomusic.web.dto.ItemDto;
import com.sashkomusic.web.dto.TagDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("ai")
public class AIController {
    private final AiService aiService;
    private final SearchService searchService;

    @PostMapping("/tag")
    public TagCategory askTagCategory(@RequestBody TagDto tagDto) {
        return aiService.askTagCategory(tagDto.name());
    }

    @PostMapping("/ask")
    public String ask(@RequestHeader(name = "X_AI_CONVERSATION_ID", defaultValue = "default") String conversationId,
                      @RequestParam String question) {
        return aiService.ask(question, conversationId);
    }

    @PostMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String userQuery) {
        return searchService.findByRelevantTags(userQuery);
    }
}
