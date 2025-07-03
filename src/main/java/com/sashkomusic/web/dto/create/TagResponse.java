package com.sashkomusic.web.dto.create;

import com.sashkomusic.domain.model.tag.TagCategory;

public record TagResponse(TagCategory category,
                          boolean exists,
                          String name,
                          String shade) {
}
