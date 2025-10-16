package com.game.crashgamev2;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "crash_point", precision = 10, scale = 2)
    private BigDecimal crashPoint;

    @Column(name = "server_seed_hash", nullable = false)
    private String serverSeedHash;

    @Column(name = "server_seed")
    private String serverSeed;

    // --- අලුතෙන් එකතු කරන කොටස ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    // --- අලුත් කොටස අවසානයි ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    public enum Status {
        WAITING,
        PENDING,
        RUNNING,
        COMPLETED
    }
}