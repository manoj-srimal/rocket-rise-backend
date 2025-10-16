package com.game.crashgamev2.service;

import com.game.crashgamev2.Bet;
import com.game.crashgamev2.Game;
import com.game.crashgamev2.User;
import com.game.crashgamev2.dto.BetConfirmationDto;
import com.game.crashgamev2.dto.GameUpdateDto;
import com.game.crashgamev2.dto.LiveBetDto;
import com.game.crashgamev2.repository.BetRepository;
import com.game.crashgamev2.repository.GameRepository;
import com.game.crashgamev2.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BetRepository betRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private GameRepository gameRepository;

    private Game currentGame;
    private Game.Status gameStatus = Game.Status.COMPLETED;
    private double currentMultiplier = 1.00;
    private long waitUntilTime;
    private long roundStartTime;

    private static final long WAIT_TIME_MS = 10000;

    @Scheduled(fixedRate = 100)
    @Transactional
    public void gameLoop() {
        switch (gameStatus) {
            case WAITING:
                long remainingTime = waitUntilTime - System.currentTimeMillis();
                if (remainingTime <= 0) {
                    startNewRound();
                } else {
                    GameUpdateDto waitUpdate = new GameUpdateDto(gameStatus.name(), 1.00, (int) Math.ceil(remainingTime / 1000.0));
                    messagingTemplate.convertAndSend("/topic/game-updates", waitUpdate);
                }
                break;
            case RUNNING:
                long elapsedTime = System.currentTimeMillis() - roundStartTime;
                double elapsedSeconds = elapsedTime / 1000.0;
                currentMultiplier = Math.pow(1.06, elapsedSeconds);

                checkForAutoCashOuts();

                if (BigDecimal.valueOf(currentMultiplier).compareTo(currentGame.getCrashPoint()) >= 0) {
                    endRound();
                } else {
                    GameUpdateDto runningUpdate = new GameUpdateDto(gameStatus.name(), currentMultiplier, 0);
                    messagingTemplate.convertAndSend("/topic/game-updates", runningUpdate);
                }
                break;
            case COMPLETED:
                gameStatus = Game.Status.WAITING;
                waitUntilTime = System.currentTimeMillis() + WAIT_TIME_MS;
                break;
        }
    }

    private void startNewRound() {
        gameRepository.findFirstByStatusOrderByIdAsc(Game.Status.PENDING).ifPresentOrElse(
                game -> {
                    System.out.println("Starting new round #" + game.getId() + " with crash point: " + game.getCrashPoint());
                    currentGame = game;
                    currentGame.setStatus(Game.Status.RUNNING);
                    gameRepository.save(currentGame);

                    currentMultiplier = 1.00;
                    gameStatus = Game.Status.RUNNING;
                    roundStartTime = System.currentTimeMillis();

                    GameUpdateDto startUpdate = new GameUpdateDto(gameStatus.name(), currentMultiplier, 0);
                    messagingTemplate.convertAndSend("/topic/game-updates", startUpdate);
                },
                () -> {
                    System.err.println("No more pending rounds to play!");
                    gameStatus = Game.Status.COMPLETED;
                }
        );
    }

    private void endRound() {
        System.out.println("Round #" + currentGame.getId() + " crashed at " + currentGame.getCrashPoint());
        currentGame.setStatus(Game.Status.COMPLETED);
        gameRepository.save(currentGame);
        gameStatus = Game.Status.COMPLETED;
        GameUpdateDto endUpdate = new GameUpdateDto(gameStatus.name(), currentGame.getCrashPoint().doubleValue(), 0);
        messagingTemplate.convertAndSend("/topic/game-updates", endUpdate);
    }

    public List<BigDecimal> getGameHistory() {
        return gameRepository.findTop20ByStatusOrderByIdDesc(Game.Status.COMPLETED)
                .stream()
                .map(Game::getCrashPoint)
                .collect(Collectors.toList());
    }

    @Transactional
    public void placeBet(String email, BigDecimal betAmount, String panelId,BigDecimal autoCashOutAt) {
        if (gameStatus != Game.Status.WAITING) {
            throw new IllegalStateException("You can only place bets between rounds.");
        }
        User user = userRepository.findAndLockByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        if (user.getBalance().compareTo(betAmount) < 0) {
            throw new IllegalStateException("Insufficient balance.");
        }

        Game nextGame = gameRepository.findFirstByStatusOrderByIdAsc(Game.Status.PENDING)
                .orElseThrow(() -> new IllegalStateException("No upcoming games found."));

        user.setBalance(user.getBalance().subtract(betAmount));
        userRepository.save(user);

        Bet bet = new Bet();
        bet.setUser(user);
        bet.setGame(nextGame);
        bet.setBetAmount(betAmount);
        bet.setStatus(Bet.Status.PLACED);
        bet.setAutoCashOutAt(autoCashOutAt);
        Bet savedBet = betRepository.save(bet);

        messagingTemplate.convertAndSendToUser(email, "/queue/balance", user.getBalance());
        messagingTemplate.convertAndSendToUser(email, "/queue/bet-confirmations",
                new BetConfirmationDto(savedBet.getId(), savedBet.getBetAmount(), panelId));

        System.out.println("Bet placed for user: " + email + " with Bet ID: " + savedBet.getId() + " from panel: " + panelId);

        LiveBetDto liveBet = new LiveBetDto();
        liveBet.setUsername(user.getFirstName());
        liveBet.setBetAmount(savedBet.getBetAmount());
        liveBet.setStatus("PLACED");
        messagingTemplate.convertAndSend("/topic/live-bets", liveBet);
    }

    @Transactional
    public void cashOut(String email, Integer betId) {
        if (gameStatus != Game.Status.RUNNING) {
            throw new IllegalStateException("You can only cash out during a running game.");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("User not found."));

        Bet activeBet = betRepository.findById(betId)
                .orElseThrow(() -> new IllegalStateException("Bet not found."));

        if (!activeBet.getUser().getId().equals(user.getId()) ||
                (currentGame != null && !activeBet.getGame().getId().equals(currentGame.getId())) ||
                activeBet.getStatus() != Bet.Status.PLACED) {
            throw new IllegalStateException("This bet is not valid for cash out.");
        }

        BigDecimal cashOutMultiplier = BigDecimal.valueOf(currentMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal winnings = activeBet.getBetAmount().multiply(cashOutMultiplier);
        user.setBalance(user.getBalance().add(winnings));

        activeBet.setStatus(Bet.Status.WON);
        activeBet.setCashOutAt(cashOutMultiplier);

        userRepository.save(user);
        betRepository.save(activeBet);

        messagingTemplate.convertAndSendToUser(email, "/queue/balance", user.getBalance());
        String successMessage = "Cashed out Bet #" + activeBet.getId() + " at " + cashOutMultiplier + "x! You won $" + winnings.setScale(2, RoundingMode.HALF_UP);
        messagingTemplate.convertAndSendToUser(email, "/queue/notifications", successMessage);

        LiveBetDto liveCashOut = new LiveBetDto();
        liveCashOut.setUsername(user.getFirstName());
        liveCashOut.setBetAmount(activeBet.getBetAmount());
        liveCashOut.setStatus("WON");
        liveCashOut.setCashOutAt(cashOutMultiplier);
        liveCashOut.setWinnings(winnings);
        messagingTemplate.convertAndSend("/topic/live-bets", liveCashOut);
    }


    public void sendErrorToUser(String email, String errorMessage) {
        messagingTemplate.convertAndSendToUser(email, "/queue/errors", errorMessage);
    }


    @Scheduled(fixedRate = 60000)
    public void checkForPendingRoundsAndGenerate() {
        long pendingRoundsCount = gameRepository.countByStatus(Game.Status.PENDING);
        System.out.println("SCHEDULER: Checking for pending rounds. Found: " + pendingRoundsCount);

        if (pendingRoundsCount < 100) {
            System.out.println("SCHEDULER: Pending rounds count is low. Generating 500 new rounds...");
            generateNewRounds(500);
        }
    }

    public void generateNewRounds(int count) {
        for (int i = 0; i < count; i++) {
            Game newGame = new Game();

            String serverSeed = generateServerSeed();
            String serverSeedHash = DigestUtils.sha256Hex(serverSeed);

            BigDecimal crashPoint = calculateCrashPoint(serverSeed);

            if (crashPoint.doubleValue() > 100.0) {
                crashPoint = BigDecimal.valueOf(100.0);
            }

            newGame.setCrashPoint(crashPoint);
            newGame.setServerSeedHash(serverSeedHash);
            newGame.setServerSeed(serverSeed);
            newGame.setStatus(Game.Status.PENDING);

            gameRepository.save(newGame);
        }
        System.out.println("SCHEDULER: Successfully generated " + count + " new rounds.");
    }

    private String generateServerSeed() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private BigDecimal calculateCrashPoint(String serverSeed) {
        try {
            byte[] hashBytes = HexFormat.of().parseHex(serverSeed.substring(0, 16));

            long hash = java.nio.ByteBuffer.wrap(hashBytes).getLong();

            long h = Math.abs(hash % 10000);
            double e = h / 10000.0;

            double crash = Math.floor(100 / (1 - e)) / 100;

            return BigDecimal.valueOf(Math.max(1.00, crash)).setScale(2, RoundingMode.HALF_UP);

        } catch (Exception ex) {
            System.err.println("Could not calculate crash point, returning failsafe. Error: " + ex.getMessage());
            return BigDecimal.valueOf(1.00);
        }
    }

    private void checkForAutoCashOuts() {
        // මේ round එකේ, auto cash out set කරපු, තවම cash out නොකරපු bets හොයනවා
        List<Bet> betsToCashOut = betRepository.findAllByGame_IdAndStatusAndAutoCashOutAtIsNotNull(
                currentGame.getId(), Bet.Status.PLACED
        );

        for (Bet bet : betsToCashOut) {
            // වත්මන් multiplier එක, user ගේ target එකට ආවොත් හෝ වැඩිවුණොත්
            if (BigDecimal.valueOf(currentMultiplier).compareTo(bet.getAutoCashOutAt()) >= 0) {
                // ඒ bet එක cash out කරනවා
                cashOut(bet.getUser().getEmail(), bet.getId());
            }
        }
    }
}