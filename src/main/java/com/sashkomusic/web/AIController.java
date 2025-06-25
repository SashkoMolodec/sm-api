package com.sashkomusic.web;

import com.sashkomusic.domain.model.TagCategory;
import com.sashkomusic.domain.service.AiService;
import com.sashkomusic.domain.service.TagService;
import com.sashkomusic.web.dto.TagDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ai")
public class AIController {
    private final TagService tagService;
    private final AiService aiService;

    public AIController(TagService tagService, AiService aiService) {
        this.tagService = tagService;
        this.aiService = aiService;
    }

    @PostMapping("/tag")
    public TagCategory askTagCategory(@RequestBody TagDto tagDto) {
        return tagService.askCategory(tagDto.name());
    }

    @PostMapping("/ask")
    public String ask(@RequestHeader(name = "X_AI_CONVERSATION_ID", defaultValue = "default") String conversationId,
                      @RequestParam String question) {
        return aiService.ask(question, conversationId);
    }
}
