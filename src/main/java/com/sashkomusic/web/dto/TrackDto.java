package com.sashkomusic.web.dto;

public record TrackDto(
        int position,
        String name,
        String sourceUrl
) {
}
