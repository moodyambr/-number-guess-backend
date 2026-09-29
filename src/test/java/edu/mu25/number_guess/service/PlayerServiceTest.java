package edu.mu25.number_guess.service;

import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
import edu.mu25.number_guess.dto.PlayerResponseDto;
import edu.mu25.number_guess.entity.Player;
import edu.mu25.number_guess.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void createPlayer_savesPlayerAndReturnsResponse() {
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId(21);
            return player;
        });
        CreatePlayerRequestDto request = createRequest("Anna");

        PlayerResponseDto response = playerService.createPlayer(request);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player savedPlayer = playerCaptor.getValue();
        assertEquals("Anna", savedPlayer.getUsername());
        assertNotNull(savedPlayer.getCreatedAt());
        assertEquals(21, response.getId());
        assertEquals("Anna", response.getUsername());
        assertEquals(savedPlayer.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void getPlayer_whenPlayerExists_returnsResponse() {
        Player player = createPlayer(21, "Anna");
        when(playerRepository.findById(21)).thenReturn(Optional.of(player));

        PlayerResponseDto response = playerService.getPlayer(21);

        assertEquals(21, response.getId());
        assertEquals("Anna", response.getUsername());
        assertEquals(player.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void getPlayer_whenPlayerDoesNotExist_throwsNotFoundException() {
        when(playerRepository.findById(21)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerService.getPlayer(21)
        );

        assertEquals("Player not found", exception.getMessage());
    }

    @Test
    void updatePlayer_whenPlayerExists_updatesUsernameAndSaves() {
        Player player = createPlayer(21, "Old name");
        when(playerRepository.findById(21)).thenReturn(Optional.of(player));
        when(playerRepository.save(player)).thenReturn(player);

        PlayerResponseDto response = playerService.updatePlayer(21, createRequest("New name"));

        assertEquals("New name", player.getUsername());
        assertEquals(21, response.getId());
        assertEquals("New name", response.getUsername());
        verify(playerRepository).save(player);
    }

    @Test
    void updatePlayer_whenPlayerDoesNotExist_throwsAndDoesNotSave() {
        when(playerRepository.findById(21)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerService.updatePlayer(21, createRequest("New name"))
        );

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void deletePlayer_whenPlayerExists_deletesIt() {
        Player player = createPlayer(21, "Anna");
        when(playerRepository.findById(21)).thenReturn(Optional.of(player));

        playerService.deletePlayer(21);

        verify(playerRepository).deleteById(21);
    }

    @Test
    void deletePlayer_whenPlayerDoesNotExist_throwsAndDoesNotDelete() {
        when(playerRepository.findById(21)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> playerService.deletePlayer(21)
        );

        assertEquals("Player not found", exception.getMessage());
        verify(playerRepository, never()).deleteById(21);
    }

    private CreatePlayerRequestDto createRequest(String username) {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername(username);
        return request;
    }

    private Player createPlayer(Integer id, String username) {
        Player player = new Player(username);
        player.setId(id);
        player.setCreatedAt(Timestamp.valueOf("2026-08-28 10:15:30"));
        return player;
    }
}
