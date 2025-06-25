package com.sashkomusic.domain.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class AiService {

    @Value("classpath:/promptTemplates/system/default.st")
    Resource systemPrompt;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiService(VectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    public String ask(String question, String conversationId) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .advisors(advisorSpec ->
                        advisorSpec
                                //.param(FILTER_EXPRESSION, "artists == 'Elektryky'")
                                .param(CONVERSATION_ID, conversationId)
                )
                .call()
                .content();
    }

}
