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
import java.math.MathContext;
import java.math.RoundingMode;

@MappedSuperclass
public class PointsCompletable extends Deletable {

    @Nonnull
    public static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.UP);

    /**
     * Principle: countdown or research points to zero -> job done.<br>
     * {@link EResourceType#CONSTRUCTION}<br>
     * {@link EResourceType#ORBITAL_CONSTRUCTION}<br>
     * {@link EResourceType#RESEARCH}
     */
    @NotNull
    @Column(columnDefinition = "decimal(19, 0)")
    protected int pointsLeft;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickCompleted", referencedColumnName = "idTick")
    private Tick finished;

    public PointsCompletable() {
    }

    public int getPointsLeft() {
        return pointsLeft;
    }

    /**
     * A job is done at zero and needed to be counted down.
     *
     * @return the leftover points will be returned to be used elsewhere that tick
     */
    public int tick(final int reduceAbout) {
        if (reduceAbout > this.pointsLeft) {
            final int leftOver = reduceAbout - this.pointsLeft;
            this.pointsLeft = 0;
            return leftOver;
        }
        this.pointsLeft = this.pointsLeft - reduceAbout;
        return 0;
    }

    public void setFinished(@Nonnull final Tick finishedAt) {
        Preconditions.checkNotNull(finishedAt, "finishedAt must not be empty");

        this.finished = finishedAt;
        complete();
    }

    public void complete() {
        this.pointsLeft = 0;
        super.delete();
    }
}
