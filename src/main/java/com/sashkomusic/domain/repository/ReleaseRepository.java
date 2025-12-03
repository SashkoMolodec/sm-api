package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReleaseRepository extends JpaRepository<Release, Long> {

    Optional<Release> findBySourceId(String sourceId);

    boolean existsBySourceId(String sourceId);
}
