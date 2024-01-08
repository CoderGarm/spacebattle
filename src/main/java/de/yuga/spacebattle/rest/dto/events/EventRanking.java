package de.yuga.spacebattle.rest.dto.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import de.yuga.spacebattle.backend.enums.events.ERankingCategory;
import de.yuga.spacebattle.rest.dto.account.Player;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EventRanking {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user.")
    private Player user;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The events the points are for.")
    private EGameEvent gameEvent;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ranking category.")
    private ERankingCategory rankingCategory;

    @JsonProperty
    @Schema(required = true, description = "The points.")
    private int points = 0;

    public EventRanking(@Nonnull final de.yuga.spacebattle.backend.entities.events.EventRanking ranking) {
        Preconditions.checkNotNull(ranking, "ranking must not be empty");

        this.user = new Player(ranking.getUser());
        this.gameEvent = ranking.getGameEvent();
        this.rankingCategory = ranking.getRankingCategory();
        this.points = ranking.getPoints();
    }
}
