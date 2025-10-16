package com.game.crashgamev2.repository;

import com.game.crashgamev2.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    // Status එක 'PENDING' තියෙන, ID එකෙන් පිළිවෙලට ගත්තම පළවෙනියටම එන record එක හොයනවා
    Optional<Game> findFirstByStatusOrderByIdAsc(Game.Status status);
    // GameRepository.java එකට මේ method එක එකතු කරන්න
    List<Game> findTop20ByStatusOrderByIdDesc(Game.Status status);
    // GameRepository.java එකට මේ method එක එකතු කරන්න
    long countByStatus(Game.Status status);

    List<Game> findTop10ByStatusOrderByIdAsc(Game.Status status);
}