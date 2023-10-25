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

@MappedSuperclass
public class PointsCompletable extends Deletable {

    /**
     * Principle: countdown or research points to zero -> job done.<br>
     * {@link EResourceType#CONSTRUCTION}<br>
     * {@link EResourceType#ORBITAL_CONSTRUCTION}<br>
     * {@link EResourceType#RESEARCH}
     */
    @NotNull
    @Column(columnDefinition = "decimal(19, 0)")
    protected long pointsLeft;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickCompleted", referencedColumnName = "idTick")
    private Tick finished;

    public PointsCompletable() {
    }

    public long getPointsLeft() {
        return pointsLeft;
    }

    /**
     * A job is done at zero and needed to be counted down.
     *
     * @return the used points will be returned
     */
    public long tick(final long reduceAbout) {
        long usedPoints = this.pointsLeft;
        if (reduceAbout >= this.pointsLeft) {
            this.pointsLeft = 0;
        } else {
            this.pointsLeft -= reduceAbout;
            usedPoints = reduceAbout;
        }
        return usedPoints;
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
