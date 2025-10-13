package com.sashkomusic.domain.service;

import com.sashkomusic.domain.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AiTools {

    private final ItemRepository itemRepository;

    @Tool(name = "isItemPresentInDatabase",
            description = "Check if item exists in database given the album's title/artists.")
    public boolean isItemPresentInDatabase(
            @ToolParam(description = "album title") String albumTitle,
            @ToolParam(description = "artists list") List<String> artists) {
        return existsByTitleAndAnyArtist(albumTitle, artists);
    }

    public boolean existsByTitleAndAnyArtist(String title, List<String> artists) {
        if (title == null || title.isBlank() || artists == null || artists.isEmpty()) {
            return false;
        }
        List<String> artistsLower = artists.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
        if (artistsLower.isEmpty()) return false;
        String[] artistsArray = artistsLower.toArray(new String[0]);
        return !itemRepository
                .findByTitleContainingIgnoreCaseAndAnyArtistIn(title, artistsArray)
                .isEmpty();
    }

}
