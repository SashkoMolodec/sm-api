package com.sashkomusic.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "track_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"track_id", "tag_name"}))
@Getter
@Setter
public class TrackTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "tag_name", nullable = false, length = 100)
    private String tagName;

    @Column(name = "tag_value", nullable = false, columnDefinition = "TEXT")
    private String tagValue;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    public TrackTag() {
    }
}
