package edu.mu25.number_guess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.GameResponseDto;
import edu.mu25.number_guess.enums.GameStatus;
import edu.mu25.number_guess.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameService gameService;

    @Test
    void createGame_returnsOkAndGameResponse() throws Exception {
        CreateGameRequestDto request = createRequest(7);
        when(gameService.createGame(any(CreateGameRequestDto.class)))
                .thenReturn(new GameResponseDto(41, 7, GameStatus.ACTIVE));

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(41))
                .andExpect(jsonPath("$.playerId").value(7))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.secretNumber").doesNotExist());

        var requestCaptor = forClass(CreateGameRequestDto.class);
        verify(gameService).createGame(requestCaptor.capture());
        assertEquals(7, requestCaptor.getValue().getPlayerId());
    }

    @Test
    void createGame_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        when(gameService.createGame(any(CreateGameRequestDto.class)))
                .thenThrow(new RuntimeException("Player not found"));

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(99))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void getGame_returnsOkAndGameResponse() throws Exception {
        when(gameService.getGame(41)).thenReturn(new GameResponseDto(41, 7, GameStatus.ACTIVE));

        mockMvc.perform(get("/games/41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(41))
                .andExpect(jsonPath("$.playerId").value(7))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(gameService).getGame(41);
    }

    @Test
    void getGame_whenGameDoesNotExist_returnsNotFoundError() throws Exception {
        when(gameService.getGame(99)).thenThrow(new RuntimeException("Game not found"));

        mockMvc.perform(get("/games/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
    }

    @Test
    void deleteGame_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/games/41"))
                .andExpect(status().isNoContent());

        verify(gameService).deleteGame(41);
    }

    @Test
    void deleteGame_whenGameDoesNotExist_returnsNotFoundError() throws Exception {
        doThrow(new RuntimeException("Game not found"))
                .when(gameService).deleteGame(99);

        mockMvc.perform(delete("/games/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
    }

    private CreateGameRequestDto createRequest(Integer playerId) {
        CreateGameRequestDto request = new CreateGameRequestDto();
        request.setPlayerId(playerId);
        return request;
    }
}
