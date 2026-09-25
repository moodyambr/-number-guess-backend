package edu.mu25.number_guess.controller;

import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
import edu.mu25.number_guess.dto.PlayerResponseDto;
import edu.mu25.number_guess.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponseDto> createPlayer(@RequestBody CreatePlayerRequestDto request) {
        return ResponseEntity.ok(playerService.createPlayer(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDto> getPlayer(@PathVariable Integer id) {
        return ResponseEntity.ok(playerService.getPlayer(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponseDto> updatePlayer(@PathVariable Integer id, @RequestBody CreatePlayerRequestDto request) {
        return ResponseEntity.ok(playerService.updatePlayer(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Integer id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
