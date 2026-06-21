package persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "game_player_scores")
public class GamePlayerScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    @Column(nullable = false)
    private int score;

    public GamePlayerScore() {}

    public GamePlayerScore(GameEntity game, PlayerEntity player, int score) {
        this.game = game;
        this.player = player;
        this.score = score;
    }

    public Long getId() { return id; }
    public GameEntity getGame() { return game; }
    public PlayerEntity getPlayer() { return player; }
    public int getScore() { return score; }
}
