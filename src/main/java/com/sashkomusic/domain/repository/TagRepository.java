package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.model.tag.TagCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByCategoryAndName(TagCategory category, String name);
}
