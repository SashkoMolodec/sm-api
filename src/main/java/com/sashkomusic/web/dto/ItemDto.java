package com.sashkomusic.web.dto;

import com.sashkomusic.domain.model.Item;
import com.sashkomusic.domain.model.ItemFormat;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemDto(
        Long id,
        String title,
        int releaseYear,
        ItemFormat format,
        List<String> images,
        List<String> artists,
        List<TrackDto> tracks,
        List<TagDto> tags
) {

    public static ItemDto of(Item item) {
        List<TagDto> tags = item.getTags().stream().map(
                tag -> new TagDto(tag.getCategory(), tag.getName(), tag.getShade())).toList();
        List<TrackDto> tracks = item.getTracks().stream().map(
                track -> new TrackDto(track.getPosition(), track.getName(), track.getSourceUrl())).toList();

        return ItemDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .releaseYear(item.getReleaseYear())
                .format(item.getFormat())
                .artists(item.getArtists())
                .images(item.getImages())
                .tags(tags)
                .tracks(tracks)
                .build();
    }
}
