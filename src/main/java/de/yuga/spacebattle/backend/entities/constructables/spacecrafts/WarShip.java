package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Detachment;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.backend.enums.EModuleType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "WarShip.getAll", query = "SELECT a FROM WarShip a WHERE a.isDeleted = false")
})
@Entity
@Table(name = "warShip",
        indexes = {
                @Index(name = "IO_WS", columnList = "isOperational"),
                @Index(name = "ID_WS", columnList = "isDeleted")
        }
)
@AttributeOverride(name = "id", column = @Column(name = "idWarShip"))
public class WarShip extends Operationable {

    @Nonnull
    @NotNull
    private String name;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idShipyard")
    private Planet shipyard;

    /**
     * A warship can be part of a fleet or part of a mission or at shore leave.
     */
    @Nullable
    @Embedded
    @SuppressWarnings("FieldMayBeFinal")
    private Detachment detachment;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idShipClass")
    private ShipClass shipClass;

    @Nonnull
    @NotNull
    @OneToOne(mappedBy = "warShip", cascade = CascadeType.ALL)
    @JoinColumn(name = "idWarshipHealthState", updatable = false)
    private WarshipHealthState warshipHealthState = new WarshipHealthState();

    public WarShip() {
    }

    public WarShip(@Nonnull final String name,
                   @Nonnull final Planet shipyard,
                   @Nonnull final Fleet fleet,
                   @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(shipyard, "shipyard shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.name = name;
        this.shipyard = shipyard;
        this.setFleet(fleet);
        this.shipClass = shipClass;
        this.warshipHealthState = new WarshipHealthState(this);
    }

    public WarShip(@Nonnull final String name,
                   @Nonnull final Planet shipyard,
                   @Nonnull final Mission mission,
                   @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(shipyard, "shipyard shouldn't be null!");
        Preconditions.checkNotNull(mission, "mission shouldn't be null!");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.name = name;
        this.shipyard = shipyard;
        this.setMission(mission);
        this.shipClass = shipClass;
        this.warshipHealthState = new WarshipHealthState(this);
    }

    public WarShip(@Nonnull final String name,
                   @Nonnull final Planet shipyard,
                   @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(shipyard, "shipyard shouldn't be null!");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.name = name;
        this.shipyard = shipyard;
        this.shipClass = shipClass;
        this.warshipHealthState = new WarshipHealthState(this);
    }

    public void upgrade(@Nonnull final Tick today, @Nonnull final Planet shipyard, @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(shipyard, "shipyard must not be empty");
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        this.shipyard = shipyard;
        this.shipClass = shipClass;
        this.warshipHealthState.repair(today);
    }

    public void setName(@Nonnull final String name) {
        this.name = Preconditions.checkNotNull(name, "name must not be empty");
    }

    @Nonnull
    public String getName() {
        String name = "";
        if (StringUtils.isNotBlank(getShipClass().getOwner().getRolePlaySetting().getShipPrefix())) {
            name += getShipClass().getOwner().getRolePlaySetting().getShipPrefix() + " ";
        }
        name += this.name;
        return name;
    }

    @Nonnull
    public Planet getShipyard() {
        return shipyard;
    }

    @Nullable
    public Fleet getFleet() {
        if (detachment == null) {
            return null;
        }
        return detachment.getFleet();
    }

    public void setFleet(@Nullable final Fleet fleet) {
        if (detachment == null) {
            this.detachment = new Detachment();
        }

        detachment.setTransportJob(null);
        detachment.setMothball(null);
        detachment.setMission(null);
        detachment.setFleet(fleet);
    }

    @Nullable
    public Mission getMission() {
        if (detachment == null) {
            return null;
        }
        return detachment.getMission();
    }

    @Nullable
    public TransportJob getTransportJob() {
        if (detachment == null) {
            return null;
        }
        return detachment.getTransportJob();
    }

    @Nullable
    public Planet getMothball() {
        return detachment != null ? detachment.getMothball() : null;
    }

    public void setMission(@Nullable final Mission mission) {
        if (detachment == null) {
            this.detachment = new Detachment();
        }
        detachment.setFleet(null);
        detachment.setTransportJob(null);
        detachment.setMothball(null);
        detachment.setMission(mission);
    }

    public void setMothball(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        if (detachment == null) {
            this.detachment = new Detachment();
        }
        detachment.setMission(null);
        detachment.setFleet(null);
        detachment.setTransportJob(null);
        detachment.setMothball(planet);
    }

    public void setTransportJob(@Nonnull final TransportJob transportJob) {
        Preconditions.checkNotNull(transportJob, "transportJob must not be empty");

        if (detachment == null) {
            this.detachment = new Detachment();
        }

        detachment.setMission(null);
        detachment.setFleet(null);
        detachment.setMothball(null);
        detachment.setTransportJob(transportJob);
    }

    @Nullable
    public Detachment getDetachment() {
        return detachment;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }

    @Nonnull
    public WarshipHealthState getWarshipHealthState() {
        return warshipHealthState;
    }

    public int getCapabilityValue(@Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(moduleType, "moduleType must not be empty");

        return warshipHealthState.getCapabilities().stream()
                .filter(c -> c.getModuleType() == moduleType).findFirst()
                .map(CapabilityValue::getValue)
                .map(BigDecimal::intValue)
                .orElse(0);
    }

    public boolean isActive() {
        return warshipHealthState.isFightingCapable();
    }

    public boolean isInactive() {
        return !warshipHealthState.isFightingCapable();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarShip)) return false;

        WarShip that = (WarShip) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 33;
    }

}
