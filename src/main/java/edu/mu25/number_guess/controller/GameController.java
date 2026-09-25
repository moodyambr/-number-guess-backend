package edu.mu25.number_guess.controller;

import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.GameResponseDto;
import edu.mu25.number_guess.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@RequestBody CreateGameRequestDto request) {
        return ResponseEntity.ok(gameService.createGame(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDto> getGame(@PathVariable Integer id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
