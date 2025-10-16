package com.game.crashgamev2;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "bets")
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "bet_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal betAmount;

    @Column(name = "cash_out_at", precision = 10, scale = 2)
    private BigDecimal cashOutAt;

    @Column(name = "auto_cash_out_at", precision = 10, scale = 2)
    private BigDecimal autoCashOutAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "placed_at", insertable = false, updatable = false)
    private Timestamp placedAt;

    public enum Status {
        PLACED,
        WON,
        LOST
    }
}