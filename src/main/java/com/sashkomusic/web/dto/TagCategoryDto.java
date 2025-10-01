package com.sashkomusic.web.dto;

public record TagCategoryDto(String name, String description) {

    public static TagCategoryDto of(String name, String description) {
        return new TagCategoryDto(name, description);
    }

}
