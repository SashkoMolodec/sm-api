package com.sashkomusic.web.controller;

import com.sashkomusic.domain.service.TrackService;
import com.sashkomusic.web.dto.TrackDto;
import com.sashkomusic.web.dto.TrackFilterRequest;
import com.sashkomusic.web.dto.TrackWithTagsDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
@AllArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @GetMapping("")
    public ResponseEntity<TrackDto> findTrack(@RequestParam String title) {
        return ResponseEntity.ok(trackService.findByTitle(title));
    }

    @GetMapping("/search")
    public ResponseEntity<TrackDto> findTrackByArtistAndTitle(
            @RequestParam String artist,
            @RequestParam String title) {
        return ResponseEntity.ok(trackService.findByArtistAndTitle(artist, title));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<TrackWithTagsDto>> findAllTracks(
            @RequestBody TrackFilterRequest request) {
        return ResponseEntity.ok(trackService.findAllByTags(request.tags()));
    }
}
