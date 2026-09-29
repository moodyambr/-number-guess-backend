package edu.mu25.number_guess.service;

import edu.mu25.number_guess.dto.CreateGuessRequestDto;
import edu.mu25.number_guess.dto.GuessResponseDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.entity.Guess;
import edu.mu25.number_guess.entity.Player;
import edu.mu25.number_guess.enums.GameStatus;
import edu.mu25.number_guess.enums.GuessResult;
import edu.mu25.number_guess.repository.GameRepository;
import edu.mu25.number_guess.repository.GuessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuessServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GuessRepository guessRepository;

    @InjectMocks
    private GuessService guessService;

    private Game activeGame;

    @BeforeEach
    void setUp() {
        Player player = new Player("Anna");
        player.setId(7);
        activeGame = new Game(player, 3);
        activeGame.setId(12);
    }

    @Test
    void createGuess_whenNumberIsLower_returnsLowAndKeepsGameActive() {
        when(gameRepository.findById(12)).thenReturn(Optional.of(activeGame));

        GuessResponseDto response = guessService.createGuess(createRequest(12, 2));

        assertEquals(GuessResult.LOW, response.getResult());
        assertEquals(2, response.getGuessedNumber());
        assertEquals(12, response.getGameId());
        assertEquals(GameStatus.ACTIVE, activeGame.getStatus());
        verify(guessRepository).save(argThat(guess ->
                guess.getResult() == GuessResult.LOW &&
                        guess.getGame() == activeGame &&
                        guess.getGuessedNumber() == 2));
        verify(gameRepository, never()).save(activeGame);
    }

    @Test
    void createGuess_whenNumberIsHigher_returnsHighAndKeepsGameActive() {
        when(gameRepository.findById(12)).thenReturn(Optional.of(activeGame));

        GuessResponseDto response = guessService.createGuess(createRequest(12, 4));

        assertEquals(GuessResult.HIGH, response.getResult());
        assertEquals(GameStatus.ACTIVE, activeGame.getStatus());
        verify(guessRepository).save(argThat(guess ->
                guess.getResult() == GuessResult.HIGH &&
                        guess.getGame() == activeGame &&
                        guess.getGuessedNumber() == 4));
        verify(gameRepository, never()).save(activeGame);
    }

    @Test
    void createGuess_whenNumberIsCorrect_finishesGameAndSavesGuess() {
        when(gameRepository.findById(12)).thenReturn(Optional.of(activeGame));

        GuessResponseDto response = guessService.createGuess(createRequest(12, 3));

        assertEquals(GuessResult.CORRECT, response.getResult());
        assertEquals(GameStatus.FINISHED, activeGame.getStatus());

        InOrder saveOrder = inOrder(gameRepository, guessRepository);
        saveOrder.verify(gameRepository).save(activeGame);
        saveOrder.verify(guessRepository).save(argThat(guess ->
                guess.getResult() == GuessResult.CORRECT &&
                        guess.getGame() == activeGame &&
                        guess.getGuessedNumber() == 3));
    }

    @Test
    void createGuess_whenGameIsAlreadyFinished_throwsAndDoesNotSaveGuess() {
        activeGame.setStatus(GameStatus.FINISHED);
        when(gameRepository.findById(12)).thenReturn(Optional.of(activeGame));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> guessService.createGuess(createRequest(12, 3))
        );

        assertEquals("Game is already finished", exception.getMessage());
        verifyNoInteractions(guessRepository);
        verify(gameRepository, never()).save(activeGame);
    }

    @Test
    void createGuess_whenGameDoesNotExist_throwsAndDoesNotSaveGuess() {
        when(gameRepository.findById(12)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> guessService.createGuess(createRequest(12, 3))
        );

        assertEquals("Game not found", exception.getMessage());
        verifyNoInteractions(guessRepository);
    }

    @Test
    void getGuess_whenGuessExists_returnsResponse() {
        Guess guess = createGuess(activeGame, 2, GuessResult.LOW);
        guess.setId(31);
        when(guessRepository.findById(31)).thenReturn(Optional.of(guess));

        GuessResponseDto response = guessService.getGuess(31);

        assertEquals(31, response.getId());
        assertEquals(12, response.getGameId());
        assertEquals(2, response.getGuessedNumber());
        assertEquals(GuessResult.LOW, response.getResult());
    }

    @Test
    void getGuess_whenGuessDoesNotExist_throwsNotFoundException() {
        when(guessRepository.findById(31)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> guessService.getGuess(31)
        );

        assertEquals("Guess not found", exception.getMessage());
    }

    @Test
    void getGuessesByGame_returnsMappedGuessList() {
        Guess lowGuess = createGuess(activeGame, 2, GuessResult.LOW);
        Guess highGuess = createGuess(activeGame, 4, GuessResult.HIGH);
        when(guessRepository.findByGame_Id(12)).thenReturn(List.of(lowGuess, highGuess));

        List<GuessResponseDto> responses = guessService.getGuessesByGame(12);

        assertEquals(2, responses.size());
        assertEquals(GuessResult.LOW, responses.get(0).getResult());
        assertEquals(GuessResult.HIGH, responses.get(1).getResult());
    }

    @Test
    void getGuessesByGame_whenNoGuesses_returnsEmptyList() {
        when(guessRepository.findByGame_Id(12)).thenReturn(List.of());

        List<GuessResponseDto> responses = guessService.getGuessesByGame(12);

        assertEquals(List.of(), responses);
    }

    @Test
    void deleteGuess_whenGuessExists_deletesIt() {
        when(guessRepository.existsById(31)).thenReturn(true);

        guessService.deleteGuess(31);

        verify(guessRepository).deleteById(31);
    }

    @Test
    void deleteGuess_whenGuessDoesNotExist_throwsAndDoesNotDelete() {
        when(guessRepository.existsById(31)).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> guessService.deleteGuess(31)
        );

        assertEquals("Guess not found", exception.getMessage());
        verify(guessRepository, never()).deleteById(31);
    }

    private CreateGuessRequestDto createRequest(Integer gameId, Integer guessedNumber) {
        CreateGuessRequestDto request = new CreateGuessRequestDto();
        request.setGameId(gameId);
        request.setGuessedNumber(guessedNumber);
        return request;
    }

    private Guess createGuess(Game game, int guessedNumber, GuessResult result) {
        Guess guess = new Guess(game, guessedNumber);
        guess.setResult(result);
        return guess;
    }
}
