package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Artist;
import com.sashkomusic.domain.repository.ArtistRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    public String create(String name) {
        Optional<Artist> artist = artistRepository.findByName(name);
        if (artist.isPresent()) {
            return artist.get().getName();
        }
        return artistRepository.save(new Artist(name)).getName();
    }
}
