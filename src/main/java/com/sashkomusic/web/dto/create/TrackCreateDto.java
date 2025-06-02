package com.sashkomusic.web.dto.create;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TrackCreateDto(
        @NotNull
        String name,
        List<String> artists
) {
}
