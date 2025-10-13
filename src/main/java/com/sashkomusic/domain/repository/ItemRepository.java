package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i, COUNT(t) as tagCount " +
            "FROM Item i " +
            "JOIN i.tags t " +
            "WHERE t.name IN :tags " +
            "GROUP BY i " +
            "ORDER BY tagCount DESC")
    List<Item> findByTags(@Param("tags") Set<String> tags);

    @Query(value = "SELECT DISTINCT i.* FROM items i " +
            "WHERE lower(i.title) LIKE lower(CONCAT('%', :title, '%')) " +
            "AND EXISTS (SELECT 1 FROM unnest(i.artists) a WHERE lower(a) = ANY (:artistsLower))",
            nativeQuery = true)
    List<Item> findByTitleContainingIgnoreCaseAndAnyArtistIn(@Param("title") String title,
                                                             @Param("artistsLower") String[] artistsLower);
}
