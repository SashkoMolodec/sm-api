package com.sashkomusic.web.dto;

public record TrackDto(Long id, String path, String title, String artistName, String rating) {
    public static TrackDto of(Long id, String path, String title, String artistName, String rating) {
        return new TrackDto(id, path, title, artistName, rating);
    }
}
