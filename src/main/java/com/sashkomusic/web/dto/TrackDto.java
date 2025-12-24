package com.sashkomusic.web.dto;

public record TrackDto(Long id, String path, String title) {
    public static TrackDto of(Long id, String path, String title) {
        return new TrackDto(id, path, title);
    }
}
