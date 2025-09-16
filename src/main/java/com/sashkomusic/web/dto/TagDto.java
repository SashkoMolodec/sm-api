package com.sashkomusic.web.dto;

import com.sashkomusic.domain.model.tag.TagCategory;
import jakarta.validation.constraints.NotEmpty;

public record TagDto(
        TagCategory category,
        @NotEmpty
        String name,
        String shade
) {
}
