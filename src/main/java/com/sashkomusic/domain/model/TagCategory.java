package com.sashkomusic.domain.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum TagCategory {
    GENRE,
    PLACE,
    TIME,
    MOOD,
    HEAVINESS,
    OTHER;

    public static String concatenated() {
        return Arrays.stream(TagCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

