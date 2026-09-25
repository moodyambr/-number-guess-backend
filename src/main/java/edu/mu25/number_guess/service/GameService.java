package edu.mu25.number_guess.service;

import edu.mu25.number_guess.mapper.Mapper;
import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.GameResponseDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.entity.Player;
import edu.mu25.number_guess.repository.GameRepository;
import edu.mu25.number_guess.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class GameService {

    private static final Random RANDOM = new Random();

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;

    public GameService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public GameResponseDto createGame(CreateGameRequestDto request) {
        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found"));
        int secretNumber = RANDOM.nextInt(5) + 1;
        Game game = new Game(player, secretNumber);
        Game savedGame = gameRepository.save(game);
        return Mapper.toGameResponseDto(savedGame);
    }

    public GameResponseDto getGame(Integer id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        return Mapper.toGameResponseDto(game);
    }

    public void deleteGame(Integer id) {
        gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        gameRepository.deleteById(id);
    }
}
