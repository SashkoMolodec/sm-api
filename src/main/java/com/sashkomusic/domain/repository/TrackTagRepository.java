package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.TrackTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackTagRepository extends JpaRepository<TrackTag, Long> {

    @Query("SELECT t.tagValue FROM TrackTag t WHERE t.trackId = :trackId AND t.tagName = 'RATING'")
    Optional<String> findRatingByTrackId(@Param("trackId") Long trackId);

    @Query("SELECT t FROM TrackTag t WHERE t.trackId IN :trackIds")
    List<TrackTag> findAllByTrackIds(@Param("trackIds") List<Long> trackIds);
}
