package com.sashkomusic.web;

import com.sashkomusic.domain.model.TagCategory;
import com.sashkomusic.domain.service.TagService;
import com.sashkomusic.web.dto.TagDto;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai")
public class AIController {
    private final TagService tagService;
    private final ChatClient chatClient;
    public AIController(TagService tagService, AnthropicChatModel chatModel) {
        this.tagService = tagService;
        this.chatClient = ChatClient.create(chatModel);

    }
    @PostMapping("/tag")
    public TagCategory askTagCategory(@RequestBody TagDto tagDto) {
        return tagService.askCategory(tagDto.name());
    }
}
