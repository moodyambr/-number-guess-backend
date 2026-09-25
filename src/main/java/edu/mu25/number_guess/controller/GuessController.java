package edu.mu25.number_guess.controller;

import edu.mu25.number_guess.dto.CreateGuessRequestDto;
import edu.mu25.number_guess.dto.GuessResponseDto;
import edu.mu25.number_guess.service.GuessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guesses")
public class GuessController {

    private final GuessService guessService;

    public GuessController(GuessService guessService) {
        this.guessService = guessService;
    }

    @PostMapping
    public ResponseEntity<GuessResponseDto> createGuess(@RequestBody CreateGuessRequestDto request) {
        return ResponseEntity.ok(guessService.createGuess(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuessResponseDto> getGuess(@PathVariable Integer id) {
        return ResponseEntity.ok(guessService.getGuess(id));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<GuessResponseDto>> getGuessesByGame(@PathVariable Integer gameId) {
        return ResponseEntity.ok(guessService.getGuessesByGame(gameId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGuess(@PathVariable Integer id) {
        guessService.deleteGuess(id);
        return ResponseEntity.noContent().build();
    }
}
