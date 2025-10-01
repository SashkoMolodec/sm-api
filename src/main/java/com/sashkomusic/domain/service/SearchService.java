package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.web.dto.ItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class SearchService {

    private final AiService aiService;
    private final ItemService itemService;
    private final TagService tagService;
    private final VectorStore vectorStore;

    public List<ItemDto> findByRelevantTags(String userQuery) {
        Set<String> tags = tagService.findMostSimilarByQuery(userQuery, 5, 0.3)
                .stream().map(Tag::getName)
                .collect(Collectors.toSet());

        return itemService.findRelevantByTags(tags);
    }

    private void addSimilarTags(String userQuery, Set<String> extractedTags) {
        // possible to filter by metadata, but for simplicity leave without it
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(0.85)
                .topK(2)
                .query(userQuery)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        Set<String> similarTags = extractTags(documents);
        extractedTags.addAll(similarTags);
    }

    private static Set<String> extractTags(List<Document> documents) {
        return documents.stream()
                .map(Document::getMetadata)
                .map(java.util.Map::values)
                .flatMap(Collection::stream)
                .flatMap(value -> {
                    if (value instanceof Collection<?>) {
                        return ((Collection<?>) value).stream();
                    }
                    return Stream.of(value);
                })
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toSet());
    }
}
