package persistence;

import persistence.entity.GameEntity;
import persistence.entity.GamePlayerScore;
import persistence.entity.PlayerEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

public class GameRepository {

    public PlayerEntity findOrCreatePlayer(EntityManager em, String name) {
        List<PlayerEntity> results = em
                .createQuery("SELECT p FROM PlayerEntity p WHERE p.name = :name", PlayerEntity.class)
                .setParameter("name", name)
                .getResultList();
        if (!results.isEmpty()) return results.get(0);
        PlayerEntity player = new PlayerEntity(name);
        em.persist(player);
        return player;
    }

    public void saveGameResult(List<String> playerNames, int winnerIndex, int[] scores,
                               int roundsPlayed, LocalDateTime startedAt) {
        EntityManager em = PersistenceUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            PlayerEntity[] players = new PlayerEntity[playerNames.size()];
            for (int i = 0; i < playerNames.size(); i++) {
                players[i] = findOrCreatePlayer(em, playerNames.get(i));
            }

            GameEntity game = new GameEntity();
            game.setStartedAt(startedAt);
            game.setEndedAt(LocalDateTime.now());
            game.setRoundsPlayed(roundsPlayed);
            game.setWinner(players[winnerIndex]);
            em.persist(game);

            for (int i = 0; i < playerNames.size(); i++) {
                GamePlayerScore gps = new GamePlayerScore(game, players[i], scores[i]);
                game.getPlayerScores().add(gps);
                em.persist(gps);
            }

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<GameEntity> findRecentGames(int limit) {
        EntityManager em = PersistenceUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT g FROM GameEntity g JOIN FETCH g.winner ORDER BY g.endedAt DESC",
                            GameEntity.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getPlayerWinCounts() {
        EntityManager em = PersistenceUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT g.winner.name, COUNT(g) FROM GameEntity g GROUP BY g.winner.name ORDER BY COUNT(g) DESC",
                            Object[].class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> getHighestScores(int limit) {
        EntityManager em = PersistenceUtil.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT s.player.name, s.score, s.game.endedAt FROM GamePlayerScore s ORDER BY s.score DESC",
                            Object[].class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
