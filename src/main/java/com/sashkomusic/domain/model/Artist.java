package com.sashkomusic.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name = "artists")
@Entity
public class Artist {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "artists_sequence"
    )
    @SequenceGenerator(
            name = "artists_sequence",
            sequenceName = "artists_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    public Artist(String name) {
        this.name = name;
    }
}
