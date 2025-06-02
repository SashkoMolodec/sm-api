package com.sashkomusic.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parent;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Embeddable
public class Track {
    @Parent
    private Item item;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String name;

    @Column
    private String sourceUrl;

    @Column(name = "track_artists", nullable = false)
    private List<String> artists = new ArrayList<>();

    public Track(int position, String name, String sourceUrl, List<String> artists) {
        this.position = position;
        this.name = name;
        this.sourceUrl = sourceUrl;
        this.artists = artists;
    }
}
