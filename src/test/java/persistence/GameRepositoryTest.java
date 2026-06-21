package persistence;

import org.junit.jupiter.api.*;
import persistence.entity.GameEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameRepositoryTest {

    private GameRepository repo;

    @BeforeAll
    static void initPersistence() {
        PersistenceUtil.init("uno-test");
    }

    @AfterAll
    static void closePersistence() {
        PersistenceUtil.close();
    }

    @BeforeEach
    void setUp() {
        repo = new GameRepository();
    }

    @Test
    void testSaveAndFindRecentGames() {
        List<String> players = Arrays.asList("Alice", "Bob", "Carol");
        int[] scores = {0, 42, 0};
        repo.saveGameResult(players, 1, scores, 15, LocalDateTime.now().minusMinutes(1));

        List<GameEntity> recent = repo.findRecentGames(10);
        assertFalse(recent.isEmpty());

        GameEntity last = recent.get(0);
        assertEquals("Bob", last.getWinner().getName());
        assertEquals(15, last.getRoundsPlayed());
        assertNotNull(last.getStartedAt());
        assertNotNull(last.getEndedAt());
    }

    @Test
    void testPlayerWinCounts() {
        List<String> players = Arrays.asList("Dave", "Eve");
        repo.saveGameResult(players, 0, new int[]{50, 0}, 10, LocalDateTime.now());
        repo.saveGameResult(players, 0, new int[]{30, 0}, 8, LocalDateTime.now());
        repo.saveGameResult(players, 1, new int[]{0, 20}, 12, LocalDateTime.now());

        List<Object[]> counts = repo.getPlayerWinCounts();
        assertFalse(counts.isEmpty());

        boolean foundDave = false;
        for (Object[] row : counts) {
            if ("Dave".equals(row[0])) {
                assertEquals(2L, row[1]);
                foundDave = true;
            }
        }
        assertTrue(foundDave);
    }

    @Test
    void testHighestScores() {
        List<String> players = Arrays.asList("Frank", "Grace");
        repo.saveGameResult(players, 0, new int[]{100, 0}, 20, LocalDateTime.now());
        repo.saveGameResult(players, 1, new int[]{0, 200}, 25, LocalDateTime.now());

        List<Object[]> top = repo.getHighestScores(5);
        assertFalse(top.isEmpty());
        assertEquals("Grace", top.get(0)[0]);
        assertEquals(200, top.get(0)[1]);
    }

    @Test
    void testFindOrCreatePlayerReusesExisting() {
        List<String> players = Arrays.asList("Hank", "Ivy");
        repo.saveGameResult(players, 0, new int[]{10, 0}, 5, LocalDateTime.now());
        repo.saveGameResult(players, 1, new int[]{0, 20}, 7, LocalDateTime.now());

        var em = PersistenceUtil.createEntityManager();
        long count = em.createQuery("SELECT COUNT(p) FROM PlayerEntity p WHERE p.name = 'Hank'", Long.class)
                .getSingleResult();
        em.close();
        assertEquals(1L, count);
    }
}
