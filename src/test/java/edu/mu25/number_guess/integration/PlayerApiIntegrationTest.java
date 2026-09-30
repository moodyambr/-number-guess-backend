package edu.mu25.number_guess.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlayerApiIntegrationTest {

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
    void createPlayer_persistsPlayerAndAllowsFollowUpGet() throws Exception {
        String createResponse = createPlayer("Anna");
        Integer playerId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/players/" + playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId))
                .andExpect(jsonPath("$.username").value("Anna"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getPlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(get("/players/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void updatePlayer_persistsNewUsername() throws Exception {
        String createResponse = createPlayer("Old name");
        Integer playerId = objectMapper.readTree(createResponse).get("id").asInt();

        CreatePlayerRequestDto updateRequest = new CreatePlayerRequestDto();
        updateRequest.setUsername("New name");

        mockMvc.perform(put("/players/" + playerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("New name"));

        mockMvc.perform(get("/players/" + playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("New name"));
    }

    @Test
    void updatePlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        CreatePlayerRequestDto updateRequest = new CreatePlayerRequestDto();
        updateRequest.setUsername("New name");

        mockMvc.perform(put("/players/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void deletePlayer_removesPlayerFromDatabase() throws Exception {
        String createResponse = createPlayer("Ska tas bort");
        Integer playerId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(delete("/players/" + playerId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/players/" + playerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    @Test
    void deletePlayer_whenPlayerDoesNotExist_returnsNotFoundError() throws Exception {
        mockMvc.perform(delete("/players/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Player not found"));
    }

    private String createPlayer(String username) throws Exception {
        CreatePlayerRequestDto request = new CreatePlayerRequestDto();
        request.setUsername(username);

        return mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
