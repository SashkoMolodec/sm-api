package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.model.tag.TagCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByCategoryAndName(TagCategory category, String name);

    // Original method (L2 distance). Kept for backward compatibility if needed.
    @Query(value = "SELECT * FROM tags ORDER BY embedding <-> cast(:queryVector as vector) LIMIT :limit", nativeQuery = true)
    List<Tag> findMostSimilar(@Param("queryVector") String queryVector, @Param("limit") int limit);

    // Cosine distance with a threshold and null-safety. Smaller distance is better (0 means identical).
    @Query(value = """
            SELECT *
            FROM tags
            WHERE embedding IS NOT NULL
              AND (embedding <=> cast(:queryVector as vector)) <= :maxDistance
            ORDER BY embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Tag> findMostSimilarWithinDistance(@Param("queryVector") String queryVector,
                                            @Param("maxDistance") double maxDistance,
                                            @Param("limit") int limit);
}
