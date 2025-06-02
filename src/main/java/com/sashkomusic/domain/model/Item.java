package com.sashkomusic.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "items")
@Entity
public class Item {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "items_sequence"
    )
    @SequenceGenerator(
            name = "items_sequence",
            sequenceName = "items_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private int releaseYear;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemFormat format;

    @Column(nullable = false)
    private List<String> images = new ArrayList<>();

    @Column(nullable = false)
    private List<String> artists = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "item_tracks")
    @OrderBy("position")
    private List<Track> tracks = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        for (Track track : tracks) {
            track.setItem(this);
        }
    }
}
