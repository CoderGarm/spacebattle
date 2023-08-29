package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class Completable extends Deletable {

    private static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.UP);

    /**
     * Principle: Countdown ticks to zero -> job done.<br>
     * Explanation change to tick: You cannot improve the production while you build a constructable.<br>
     * "Construction points based" (construction yard, shipyard or laboratories) for<br>
     * {@link EResourceType#CONSTRUCTION}<br>
     * {@link EResourceType#ORBITAL_CONSTRUCTION}<br>
     * {@link EResourceType#RESEARCH}
     */
    @NotNull
    @Column(columnDefinition = "decimal(19, 0)")
    protected int ticksLeft;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickCompleted", referencedColumnName = "idTick")
    private Tick finished;

    public Completable() {
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    /**
     * A job is done at zero and needed to be counted down.
     */
    public void tick() {
        this.ticksLeft--;
    }

    public void reduceRemainingTicksByLevelUpgrade(@Nonnull final BigDecimal increasingFactorPerLevel) {
        Preconditions.checkNotNull(increasingFactorPerLevel, "increasingFactorPerLevel must not be empty");

        final int reduceAbout = BigDecimal.valueOf(this.ticksLeft).multiply(increasingFactorPerLevel, MATH_CONTEXT).intValue();
        for (int i = 0; i < reduceAbout; i++) {
            if (ticksLeft > 0) {
                ticksLeft--;
            }
        }
    }

    public void setFinished(@Nonnull final Tick finishedAt) {
        Preconditions.checkNotNull(finishedAt, "finishedAt must not be empty");

        this.finished = finishedAt;
        complete();
    }

    public void complete() {
        this.ticksLeft = 0;
        super.delete();
    }
}
