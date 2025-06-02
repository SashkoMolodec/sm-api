package com.sashkomusic.web.dto.create;

import com.sashkomusic.domain.model.ItemFormat;
import com.sashkomusic.web.dto.TagDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ItemCreateDto(
        @NotBlank
        String title,
        @NotNull
        Integer releaseYear,
        @NotNull
        ItemFormat format,
        @NotEmpty
        List<String> artists,
        @NotEmpty
        List<TrackCreateDto> tracks,
        List<TagDto> tags
) {
}