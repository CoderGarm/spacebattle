package de.yuga.spacebattle.backend.entities.events;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import de.yuga.spacebattle.backend.enums.events.ERankingCategory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "eventRanking",
        uniqueConstraints = @UniqueConstraint(name = "POINTS_UK", columnNames = {"idUser", "gameEvent", "rankingCategory"}))
@AttributeOverride(name = "id", column = @Column(name = "idEventRanking"))
public class EventRanking extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUser")
    private Owner user;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EGameEvent gameEvent;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ERankingCategory rankingCategory;

    private int points = 0;

    public EventRanking() {
    }

    public EventRanking(@Nonnull final Owner user, @Nonnull final EGameEvent gameEvent, @Nonnull final ERankingCategory rankingCategory) {
        this.user = Preconditions.checkNotNull(user, "owner must not be empty");
        this.gameEvent = Preconditions.checkNotNull(gameEvent, "gameEvent must not be empty");
        this.rankingCategory = Preconditions.checkNotNull(rankingCategory, "rankingCategory must not be empty");
    }

    @Nonnull
    public Owner getUser() {
        return user;
    }

    @Nonnull
    public EGameEvent getGameEvent() {
        return gameEvent;
    }

    @Nonnull
    public ERankingCategory getRankingCategory() {
        return rankingCategory;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(final int points) {
        this.points += points;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final EventRanking that = (EventRanking) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
