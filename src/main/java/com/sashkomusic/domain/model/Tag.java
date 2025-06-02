package com.sashkomusic.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Table(name = "tags")
@Entity
public class Tag {
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "tags_sequence"
    )
    @SequenceGenerator(
            name = "tags_sequence",
            sequenceName = "tags_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TagCategory category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String shade;

    public Tag(String name, TagCategory category, String shade) {
        this.name = name;
        this.category = category;
        this.shade = shade;
    }
}
