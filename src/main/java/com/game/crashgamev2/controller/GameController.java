package com.game.crashgamev2.controller;

import com.game.crashgamev2.dto.CashOutRequestDto;
import com.game.crashgamev2.dto.PlaceBetDto;
import com.game.crashgamev2.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/api/game/history")
    public ResponseEntity<List<BigDecimal>> getGameHistory() {
        return ResponseEntity.ok(gameService.getGameHistory());
    }

    // GameController.java
    @MessageMapping("/game/bet")
    public void placeBet(PlaceBetDto betDto, Principal principal) {
        try {
            String email = principal.getName();
            gameService.placeBet(email, betDto.getBetAmount(), betDto.getPanelId(), betDto.getAutoCashOutAt());
        } catch (Exception e) {
            gameService.sendErrorToUser(principal.getName(), e.getMessage());
        }
    }

    @MessageMapping("/game/cashout")
    public void cashOut(CashOutRequestDto cashOutDto, Principal principal) {
        try {
            String email = principal.getName();
            gameService.cashOut(email, cashOutDto.getBetId());
        } catch (Exception e) {
            gameService.sendErrorToUser(principal.getName(), e.getMessage());
        }
    }
}