package com.sashkomusic.web.controller;

import com.sashkomusic.domain.service.TrackService;
import com.sashkomusic.web.dto.TrackDto;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
