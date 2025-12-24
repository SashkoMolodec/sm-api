package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Track;
import com.sashkomusic.domain.repository.TrackRepository;
import com.sashkomusic.web.dto.TrackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;

    public TrackDto findByTitle(String title) {
        Track track = trackRepository.findByTitle(title).orElseThrow(() -> new IllegalArgumentException("Track not found"));
        return TrackDto.of(track.getId(), track.getLocalPath(), track.getTitle());
    }
}

