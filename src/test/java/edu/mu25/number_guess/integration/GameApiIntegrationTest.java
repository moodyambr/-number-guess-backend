package edu.mu25.number_guess.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mu25.number_guess.dto.CreateGameRequestDto;
import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GameApiIntegrationTest {

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

    @Test
    void createGame_persistsGameLinkedToRealPlayer() throws Exception {
        Integer playerId = createPlayer("Anna");

        String createResponse = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest(playerId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(playerId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.secretNumber").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer gameId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.playerId").value(playerId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createGame_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest(999999))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void getGame_whenGameDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(get("/games/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
    }

    @Test
    void deleteGame_removesGameFromDatabase() throws Exception {
        Integer playerId = createPlayer("Ska ha spel som tas bort");
        Integer gameId = createGame(playerId);

        mockMvc.perform(delete("/games/" + gameId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/games/" + gameId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
    }

    @Test
    void deleteGame_whenGameDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(delete("/games/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Game not found"));
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

    private Integer createGame(Integer playerId) throws Exception {
        String response = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameRequest(playerId))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asInt();
    }

    private CreateGameRequestDto createGameRequest(Integer playerId) {
        CreateGameRequestDto request = new CreateGameRequestDto();
        request.setPlayerId(playerId);
        return request;
    }
}
