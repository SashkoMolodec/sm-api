package com.sashkomusic.domain.model;

public enum SearchEngine {
    MUSICBRAINZ,
    DISCOGS,
    BANDCAMP;

    public String getName() {
        return name().toLowerCase();
    }
}
