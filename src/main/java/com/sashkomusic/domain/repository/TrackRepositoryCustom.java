package com.sashkomusic.domain.repository;

import com.sashkomusic.domain.model.Track;

import java.util.List;
import java.util.Map;

public interface TrackRepositoryCustom {
    List<Track> findAllByTags(Map<String, String> tagFilters);
}
