package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Artist;
import com.sashkomusic.domain.model.Track;
import com.sashkomusic.domain.repository.TrackRepository;
import com.sashkomusic.domain.repository.TrackTagRepository;
import com.sashkomusic.web.dto.TrackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;

    public TrackDto findByTitle(String title) {
        Track track = trackRepository.findByTitle(title)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        String artistName = track.getArtists().stream()
                .map(Artist::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String rating = trackTagRepository.findRatingByTrackId(track.getId()).orElse(null);

        return TrackDto.of(track.getId(), track.getLocalPath(), track.getTitle(), artistName, rating);
    }

    public TrackDto findByArtistAndTitle(String artist, String title) {
        Track track = trackRepository.findByArtistAndTitle(artist, title)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        String artistName = track.getArtists().stream()
                .map(Artist::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String rating = trackTagRepository.findRatingByTrackId(track.getId()).orElse(null);

        return TrackDto.of(track.getId(), track.getLocalPath(), track.getTitle(), artistName, rating);
    }
}

