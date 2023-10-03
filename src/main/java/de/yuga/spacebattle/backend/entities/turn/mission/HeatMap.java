package de.yuga.spacebattle.backend.entities.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EMissionType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "heatMap")
@AttributeOverride(name = "id", column = @Column(name = "idHeatMap"))
public class HeatMap extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPlanet", updatable = false)
    private Planet planet;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EMissionType missionType;

    @Min(-30)
    @Max(30)
    private int heat;

    public HeatMap() {
    }

    public HeatMap(@Nonnull final Planet planet, @Nonnull final EMissionType missionType, final int heat) {
        this.planet = Preconditions.checkNotNull(planet, "planet must not be empty");
        this.missionType = Preconditions.checkNotNull(missionType, "missionType must not be empty");
        this.heat = Integer.signum(heat) * Math.max(30, Math.abs(heat));
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    @Nonnull
    public EMissionType getMissionType() {
        return missionType;
    }

    public int getHeat() {
        return heat;
    }

    /**
     * Adds the heat uop to a max of 30.
     */
    public void add(final int impact) {
        if (Math.signum(impact) < 0) {
            this.heat = Math.max(-30, this.heat + impact);
        } else {
            this.heat = Math.min(30, this.heat + impact);
        }
    }

    public void setMainPlanet() {
        this.heat = -15;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final HeatMap heatMap = (HeatMap) o;

        return new EqualsBuilder().append(planet, heatMap.planet).append(missionType, heatMap.missionType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(planet).append(missionType).toHashCode();
    }
}
