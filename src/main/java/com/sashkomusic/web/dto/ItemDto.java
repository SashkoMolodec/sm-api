package com.sashkomusic.web.dto;

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

}
