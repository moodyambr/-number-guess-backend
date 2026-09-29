package edu.mu25.number_guess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
import edu.mu25.number_guess.dto.PlayerResponseDto;
import edu.mu25.number_guess.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(edu.mu25.number_guess.controller.PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void createPlayer_returnsOkAndPlayerResponse() throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername("Anna");

        PlayerResponseDto response = new PlayerResponseDto(
                1,
                "Anna",
                Timestamp.valueOf("2026-08-28 10:15:30")
        );

        when(playerService.createPlayer(any(CreatePlayerRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Anna"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(playerService, times(1)).createPlayer(any(CreatePlayerRequestDto.class));
    }

    @Test
    void getPlayer_returnsOkAndPlayerResponse() throws Exception {
        when(playerService.getPlayer(1)).thenReturn(playerResponse(1, "Anna"));

        mockMvc.perform(get("/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Anna"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(playerService).getPlayer(1);
    }

    @Test
    void getPlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        when(playerService.getPlayer(99)).thenThrow(new RuntimeException("Player not found"));

        mockMvc.perform(get("/players/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void updatePlayer_returnsOkAndUpdatedPlayerResponse() throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername("New name");
        when(playerService.updatePlayer(eq(1),
                any(CreatePlayerRequestDto.class)))
                .thenReturn(playerResponse(1, "New name"));

        mockMvc.perform(put("/players/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("New name"));

        verify(playerService).updatePlayer(eq(1),
                any(CreatePlayerRequestDto.class));
    }

    @Test
    void updatePlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername("New name");
        when(playerService.updatePlayer(eq(99),
                any(CreatePlayerRequestDto.class)))
                .thenThrow(new RuntimeException("Player not found"));

        mockMvc.perform(put("/players/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void deletePlayer_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/players/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(playerService).deletePlayer(1);
    }

    @Test
    void deletePlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        doThrow(new RuntimeException("Player not found"))
                .when(playerService).deletePlayer(99);

        mockMvc.perform(delete("/players/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    private PlayerResponseDto playerResponse(Integer id, String username) {
        return new PlayerResponseDto(
                id,
                username,
                Timestamp.valueOf("2026-08-28 10:15:30")
        );
    }
}
