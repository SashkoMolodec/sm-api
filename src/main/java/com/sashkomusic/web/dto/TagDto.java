package com.sashkomusic.web.dto;

import com.sashkomusic.domain.model.tag.TagCategory;
import jakarta.validation.constraints.NotEmpty;

public record TagDto(
        TagCategory category,
        @NotEmpty
        String name,
        String shade
) {
    public static TagDto of(TagCategory category, String name, String shade) {
        return new TagDto(category, name, shade);
    }

    public static TagDto of(TagCategory category, String name) {
        return new TagDto(category, name, null);
    }
}
