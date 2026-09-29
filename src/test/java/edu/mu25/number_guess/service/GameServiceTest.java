package edu.mu25.number_guess.service;

import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.GameResponseDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.entity.Player;
import edu.mu25.number_guess.enums.GameStatus;
import edu.mu25.number_guess.repository.GameRepository;
import edu.mu25.number_guess.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameService gameService;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Anna");
        player.setId(7);
    }

    @Test
    void createGame_whenPlayerExists_savesActiveGameWithSecretNumberInRange() {
        when(playerRepository.findById(7)).thenReturn(Optional.of(player));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game game = invocation.getArgument(0);
            game.setId(41);
            return game;
        });

        GameResponseDto response = gameService.createGame(createRequest(7));

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();

        assertEquals(player, savedGame.getPlayer());
        assertTrue(savedGame.getSecretNumber() >= 1 && savedGame.getSecretNumber() <= 5);
        assertEquals(GameStatus.ACTIVE, savedGame.getStatus());
        assertNotNull(savedGame.getCreatedAt());
        assertEquals(41, response.getId());
        assertEquals(7, response.getPlayerId());
        assertEquals(GameStatus.ACTIVE, response.getStatus());
    }

    @Test
    void createGame_whenPlayerDoesNotExist_throwsAndDoesNotSaveGame() {
        when(playerRepository.findById(7)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> gameService.createGame(createRequest(7))
        );

        assertEquals("Player not found", exception.getMessage());
        verifyNoInteractions(gameRepository);
    }

    @Test
    void getGame_whenGameExists_returnsMappedResponse() {
        Game game = new Game(player, 3);
        game.setId(41);
        when(gameRepository.findById(41)).thenReturn(Optional.of(game));

        GameResponseDto response = gameService.getGame(41);

        assertEquals(41, response.getId());
        assertEquals(7, response.getPlayerId());
        assertEquals(GameStatus.ACTIVE, response.getStatus());
    }

    @Test
    void getGame_whenGameDoesNotExist_throwsNotFoundException() {
        when(gameRepository.findById(41)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> gameService.getGame(41)
        );

        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void deleteGame_whenGameExists_deletesIt() {
        Game game = new Game(player, 3);
        game.setId(41);
        when(gameRepository.findById(41)).thenReturn(Optional.of(game));

        gameService.deleteGame(41);

        verify(gameRepository).deleteById(41);
    }

    @Test
    void deleteGame_whenGameDoesNotExist_throwsAndDoesNotDelete() {
        when(gameRepository.findById(41)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> gameService.deleteGame(41)
        );

        assertEquals("Game not found", exception.getMessage());
        verify(gameRepository, never()).deleteById(41);
    }

    private CreateGameRequestDto createRequest(Integer playerId) {
        CreateGameRequestDto request = new CreateGameRequestDto();
        request.setPlayerId(playerId);
        return request;
    }
}
