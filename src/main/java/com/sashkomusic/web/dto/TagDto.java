package com.sashkomusic.web.dto;

import com.sashkomusic.domain.model.TagCategory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TagDto(
        @NotNull
        TagCategory category,
        @NotEmpty
        String name,
        @NotEmpty
        String shade
) {
}
