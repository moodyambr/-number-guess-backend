package edu.mu25.number_guess.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.CreateGuessRequestDto;
import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GuessApiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("localdb")
            .withUsername("root")
            .withPassword("password");

    @DynamicPropertySource
    static void registerMysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void createGuess_correctGuess_finishesGameAndPersistsResult() throws Exception {
        Integer gameId = createGame("Anna");
        int secretNumber = readSecretNumber(gameId);

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(gameId, secretNumber))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.guessedNumber").value(secretNumber))
                .andExpect(jsonPath("$.result").value("CORRECT"));

        Game finishedGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(finishedGame.getStatus().name()).isEqualTo("FINISHED");
    }

    @Test
    void createGuess_wrongGuess_doesNotFinishGame() throws Exception {
        Integer gameId = createGame("Bertil");
        int secretNumber = readSecretNumber(gameId);
        int wrongGuess = secretNumber < 5 ? secretNumber + 1 : secretNumber - 1;
        String expectedResult = wrongGuess > secretNumber ? "HIGH" : "LOW";

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(gameId, wrongGuess))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(expectedResult));

        Game activeGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(activeGame.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void createGuess_whenGameAlreadyFinished_rejectsFurtherGuesses() throws Exception {
        Integer gameId = createGame("Cecilia");
        int secretNumber = readSecretNumber(gameId);

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(gameId, secretNumber))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("CORRECT"));

        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(gameId, secretNumber))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game is already finished"));
    }

    @Test
    void createGuess_whenGameDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(999999, 3))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
    }

    @Test
    void getGuess_returnsPersistedGuess() throws Exception {
        Integer gameId = createGame("David");
        int secretNumber = readSecretNumber(gameId);
        int wrongGuess = secretNumber < 5 ? secretNumber + 1 : secretNumber - 1;

        Integer guessId = createGuess(gameId, wrongGuess);

        mockMvc.perform(get("/guesses/" + guessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guessId))
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.guessedNumber").value(wrongGuess));
    }

    @Test
    void getGuess_whenGuessDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(get("/guesses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guess not found"));
    }

    @Test
    void getGuessesByGame_returnsAllGuessesForThatGame() throws Exception {
        Integer gameId = createGame("Erik");
        int secretNumber = readSecretNumber(gameId);
        int wrongGuess = secretNumber < 5 ? secretNumber + 1 : secretNumber - 1;

        createGuess(gameId, wrongGuess);
        createGuess(gameId, secretNumber);

        mockMvc.perform(get("/guesses/game/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].gameId").value(gameId))
                .andExpect(jsonPath("$[1].gameId").value(gameId));
    }

    @Test
    void getGuessesByGame_whenNoGuessesExist_returnsEmptyList() throws Exception {
        Integer gameId = createGame("Frida");

        mockMvc.perform(get("/guesses/game/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteGuess_removesGuessFromDatabase() throws Exception {
        Integer gameId = createGame("Gustav");
        int secretNumber = readSecretNumber(gameId);
        int wrongGuess = secretNumber < 5 ? secretNumber + 1 : secretNumber - 1;
        Integer guessId = createGuess(gameId, wrongGuess);

        mockMvc.perform(delete("/guesses/" + guessId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/guesses/" + guessId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guess not found"));
    }

    @Test
    void deleteGuess_whenGuessDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(delete("/guesses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guess not found"));
    }

    private Integer createPlayer(String username) throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername(username);

        String response = mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asInt();
    }

    private Integer createGame(String username) throws Exception {
        Integer playerId = createPlayer(username);
        CreateGameRequestDto request = new CreateGameRequestDto();
        request.setPlayerId(playerId);

        String response = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asInt();
    }

    private Integer createGuess(Integer gameId, int guessedNumber) throws Exception {
        String response = mockMvc.perform(post("/guesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGuessRequest(gameId, guessedNumber))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asInt();
    }

    private CreateGuessRequestDto createGuessRequest(Integer gameId, int guessedNumber) {
        CreateGuessRequestDto request = new CreateGuessRequestDto();
        request.setGameId(gameId);
        request.setGuessedNumber(guessedNumber);
        return request;
    }

    /**
     * Reads the secret number directly from the database for test arrangement only.
     * The secret number is never asserted on via the public API responses.
     */
    private int readSecretNumber(Integer gameId) {
        return gameRepository.findById(gameId).orElseThrow().getSecretNumber();
    }
}
