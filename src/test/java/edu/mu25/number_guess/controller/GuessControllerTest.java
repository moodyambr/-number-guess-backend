package edu.mu25.number_guess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mu25.number_guess.dto.CreateGuessRequestDto;
import edu.mu25.number_guess.dto.GuessResponseDto;
import edu.mu25.number_guess.enums.GuessResult;
import edu.mu25.number_guess.service.GuessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

@WebMvcTest(GuessController.class)
class GuessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GuessService guessService;

    @Test
    void createGuess_returnsOkAndGuessResponse() throws Exception {
        CreateGuessRequestDto request = createRequest(12, 3);
        when(guessService.createGuess(any(CreateGuessRequestDto.class)))
                .thenReturn(new GuessResponseDto(31, 12, 3, GuessResult.CORRECT));

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31))
                .andExpect(jsonPath("$.gameId").value(12))
                .andExpect(jsonPath("$.guessedNumber").value(3))
                .andExpect(jsonPath("$.result").value("CORRECT"));

        var requestCaptor = forClass(CreateGuessRequestDto.class);
        verify(guessService).createGuess(requestCaptor.capture());
        assertEquals(12, requestCaptor.getValue().getGameId());
        assertEquals(3, requestCaptor.getValue().getGuessedNumber());
    }

    @Test
    void createGuess_whenServiceRejectsFinishedGame_returnsNotFoundError() throws Exception {
        when(guessService.createGuess(any(CreateGuessRequestDto.class)))
                .thenThrow(new RuntimeException("Game is already finished"));

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest(12, 3))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game is already finished"));
    }

    @Test
    void getGuess_returnsOkAndGuessResponse() throws Exception {
        when(guessService.getGuess(31))
                .thenReturn(new GuessResponseDto(31, 12, 2, GuessResult.LOW));

        mockMvc.perform(get("/guesses/31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(31))
                .andExpect(jsonPath("$.gameId").value(12))
                .andExpect(jsonPath("$.guessedNumber").value(2))
                .andExpect(jsonPath("$.result").value("LOW"));

        verify(guessService).getGuess(31);
    }

    @Test
    void getGuess_whenGuessDoesNotExist_returnsNotFoundError() throws Exception {
        when(guessService.getGuess(99)).thenThrow(new RuntimeException("Guess not found"));

        mockMvc.perform(get("/guesses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guess not found"));
    }

    @Test
    void getGuessesByGame_returnsOkAndGuessList() throws Exception {
        when(guessService.getGuessesByGame(12)).thenReturn(List.of(
                new GuessResponseDto(31, 12, 2, GuessResult.LOW),
                new GuessResponseDto(32, 12, 3, GuessResult.CORRECT)
        ));

        mockMvc.perform(get("/guesses/game/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(31))
                .andExpect(jsonPath("$[0].result").value("LOW"))
                .andExpect(jsonPath("$[1].id").value(32))
                .andExpect(jsonPath("$[1].result").value("CORRECT"));

        verify(guessService).getGuessesByGame(12);
    }

    @Test
    void getGuessesByGame_whenNoGuesses_returnsEmptyArray() throws Exception {
        when(guessService.getGuessesByGame(12)).thenReturn(List.of());

        mockMvc.perform(get("/guesses/game/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deleteGuess_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/guesses/31"))
                .andExpect(status().isNoContent());

        verify(guessService).deleteGuess(31);
    }

    @Test
    void deleteGuess_whenGuessDoesNotExist_returnsNotFoundError() throws Exception {
        doThrow(new RuntimeException("Guess not found"))
                .when(guessService).deleteGuess(99);

        mockMvc.perform(delete("/guesses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guess not found"));
    }

    private CreateGuessRequestDto createRequest(Integer gameId, Integer guessedNumber) {
        CreateGuessRequestDto request = new CreateGuessRequestDto();
        request.setGameId(gameId);
        request.setGuessedNumber(guessedNumber);
        return request;
    }
}
