package persistence.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Column(name = "rounds_played", nullable = false)
    private int roundsPlayed;

    @ManyToOne(optional = false)
    @JoinColumn(name = "winner_id")
    private PlayerEntity winner;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayerScore> playerScores = new ArrayList<>();

    public GameEntity() {}

    public Long getId() { return id; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public int getRoundsPlayed() { return roundsPlayed; }
    public void setRoundsPlayed(int roundsPlayed) { this.roundsPlayed = roundsPlayed; }
    public PlayerEntity getWinner() { return winner; }
    public void setWinner(PlayerEntity winner) { this.winner = winner; }
    public List<GamePlayerScore> getPlayerScores() { return playerScores; }
}
