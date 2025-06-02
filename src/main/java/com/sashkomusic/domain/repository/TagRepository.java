package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.Tag;
import com.sashkomusic.domain.model.TagCategory;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TagRepository extends CrudRepository<Tag, Long> {
    Optional<Tag> findByCategoryAndName(TagCategory category, String name);
}
