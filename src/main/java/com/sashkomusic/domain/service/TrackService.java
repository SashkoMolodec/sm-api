package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Artist;
import com.sashkomusic.domain.model.Track;
import com.sashkomusic.domain.model.TrackTag;
import com.sashkomusic.domain.repository.TrackRepository;
import com.sashkomusic.domain.repository.TrackTagRepository;
import com.sashkomusic.web.dto.TrackDto;
import com.sashkomusic.web.dto.TrackWithTagsDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        String djEnergy = trackTagRepository.findByTrackIdAndTagName(track.getId(), "DJ_ENERGY").orElse(null);
        String djFunction = trackTagRepository.findByTrackIdAndTagName(track.getId(), "DJ_FUNCTION").orElse(null);
        String comment = trackTagRepository.findByTrackIdAndTagName(track.getId(), "COMM").orElse(null);

        return TrackDto.of(track.getId(), track.getLocalPath(), track.getTitle(), artistName, rating,
                djEnergy, djFunction, comment);
    }

    public TrackDto findByArtistAndTitle(String artist, String title) {
        Track track = trackRepository.findByArtistAndTitle(artist, title)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        String artistName = track.getArtists().stream()
                .map(Artist::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String rating = trackTagRepository.findRatingByTrackId(track.getId()).orElse(null);
        String djEnergy = trackTagRepository.findByTrackIdAndTagName(track.getId(), "DJ_ENERGY").orElse(null);
        String djFunction = trackTagRepository.findByTrackIdAndTagName(track.getId(), "DJ_FUNCTION").orElse(null);
        String comment = trackTagRepository.findByTrackIdAndTagName(track.getId(), "COMM").orElse(null);

        return TrackDto.of(track.getId(), track.getLocalPath(), track.getTitle(), artistName, rating,
                djEnergy, djFunction, comment);
    }

    public List<TrackWithTagsDto> findAllByTags(Map<String, String> tagFilters) {
        List<Track> tracks = trackRepository.findAllByTags(tagFilters);

        if (tracks.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> trackIds = tracks.stream()
                .map(Track::getId)
                .toList();

        List<TrackTag> allTags = trackTagRepository.findAllByTrackIds(trackIds);

        Map<Long, Map<String, String>> tagsByTrackId = allTags.stream()
                .collect(Collectors.groupingBy(
                        TrackTag::getTrackId,
                        Collectors.toMap(
                                TrackTag::getTagName,
                                TrackTag::getTagValue,
                                (v1, v2) -> v1
                        )
                ));

        return toDto(tracks, tagsByTrackId);
    }

    private static @NonNull List<TrackWithTagsDto> toDto(List<Track> tracks, Map<Long, Map<String, String>> tagsByTrackId) {
        return tracks.stream()
                .map(track -> {
                    String artistName = track.getArtists().stream()
                            .map(Artist::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");

                    Map<String, String> tags = tagsByTrackId.getOrDefault(track.getId(), Collections.emptyMap());

                    return TrackWithTagsDto.of(
                            track.getId(),
                            track.getLocalPath(),
                            track.getTitle(),
                            artistName,
                            tags
                    );
                })
                .toList();
    }
}

