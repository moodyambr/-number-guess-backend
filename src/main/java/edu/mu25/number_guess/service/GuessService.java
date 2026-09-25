package edu.mu25.number_guess.service;

import edu.mu25.number_guess.mapper.Mapper;
import edu.mu25.number_guess.dto.CreateGuessRequestDto;
import edu.mu25.number_guess.dto.GuessResponseDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.entity.Guess;
import edu.mu25.number_guess.enums.GameStatus;
import edu.mu25.number_guess.enums.GuessResult;
import edu.mu25.number_guess.repository.GameRepository;
import edu.mu25.number_guess.repository.GuessRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuessService {

    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;

    public GuessService(GameRepository gameRepository, GuessRepository guessRepository) {
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
    }

    public GuessResponseDto createGuess(CreateGuessRequestDto request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (game.getStatus() == GameStatus.FINISHED) {
            throw new RuntimeException("Game is already finished");
        }

        Guess guess = new Guess(game, request.getGuessedNumber());

        if (request.getGuessedNumber() < game.getSecretNumber()) {
            guess.setResult(GuessResult.LOW);
        } else if (request.getGuessedNumber() > game.getSecretNumber()) {
            guess.setResult(GuessResult.HIGH);
        } else {
            guess.setResult(GuessResult.CORRECT);
        }

        if (guess.getResult() == GuessResult.CORRECT) {
            game.setStatus(GameStatus.FINISHED);
            gameRepository.save(game);
        }

        guessRepository.save(guess);
        return Mapper.toGuessResponseDto(guess);
    }

    public GuessResponseDto getGuess(Integer id) {
        Guess guess = guessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guess not found"));
        return Mapper.toGuessResponseDto(guess);
    }

    public List<GuessResponseDto> getGuessesByGame(Integer gameId) {
        return guessRepository.findByGame_Id(gameId)
                .stream()
                .map(Mapper::toGuessResponseDto)
                .collect(Collectors.toList());
    }

    public void deleteGuess(Integer id) {
        if (!guessRepository.existsById(id)) {
            throw new RuntimeException("Guess not found");
        }
        guessRepository.deleteById(id);
    }
}