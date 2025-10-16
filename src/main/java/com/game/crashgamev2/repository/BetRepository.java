package com.game.crashgamev2.repository;

import com.game.crashgamev2.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {
    Optional<Bet> findByUser_IdAndGame_IdAndStatus(Integer userId, Integer gameId, Bet.Status status);
    // BetRepository.java
    List<Bet> findAllByGame_IdAndStatusAndAutoCashOutAtIsNotNull(Integer gameId, Bet.Status status);
}