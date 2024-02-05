package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.CombatRoundConverter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "maneuver")
@AttributeOverride(name = "id", column = @Column(name = "idManeuver"))
public class Maneuver extends CombatRoundKey {

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idActor", nullable = false, updatable = false)
    private Fleet actor;

    @NotNull
    @Nonnull
    @ManyToOne(optional = false)
    @JoinColumn(name = "idTarget", nullable = false, updatable = false)
    private Fleet target;

    @NotNull
    @Nonnull
    private String name;

    @NotNull
    @Nonnull
    @Convert(converter = CombatRoundConverter.class)
    private CombatRound designatedEnd;

    @NotNull
    @Nonnull
    @Convert(converter = CombatRoundConverter.class)
    private CombatRound end;

    @Nonnull
    @NotNull
    @JoinColumn(name = "idManeuver")
    @OneToMany(fetch = FetchType.EAGER)
    private final Set<ManeuverElement> maneuverElements = new HashSet<>();

    public Maneuver() {
    }

    public Maneuver(@Nonnull final de.yuga.spacebattle.backend.combat.maneuver.Maneuver maneuver) {
        super(maneuver.getStart(), ECombatPhase.ECombatSubPhase.MOVEMENT_PHASE);

        this.actor = maneuver.getAgent();
        this.target = maneuver.getTarget();
        this.name = maneuver.getManeuverName();
        this.designatedEnd = maneuver.getDesignatedEnd();
        this.end = maneuver.getEnd();
        maneuverElements.addAll(maneuver.getManeuverElements().getManeuverElements()
                .stream().map(m -> new ManeuverElement(this, m)).collect(Collectors.toSet()));
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public Fleet getTarget() {
        return target;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public CombatRound getDesignatedEnd() {
        return designatedEnd;
    }

    @Nonnull
    public CombatRound getEnd() {
        return end;
    }

    @Nonnull
    public Set<ManeuverElement> getManeuverElements() {
        return maneuverElements;
    }

    public void setManeuverElements(@Nonnull final Collection<ManeuverElement> elements) {
        Preconditions.checkNotNull(elements, "elements must not be empty");

        maneuverElements.clear();
        elements.forEach(e -> e.setManeuver(this));
        maneuverElements.addAll(elements);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Maneuver maneuver = (Maneuver) o;

        return new EqualsBuilder().append(actor, maneuver.actor).append(target, maneuver.target).append(name, maneuver.name).append(getCombatRound(), maneuver.getCombatRound()).append(end, maneuver.end).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(actor).append(target).append(name).append(getCombatRound()).append(end).toHashCode();
    }

    public void drop() {
        // fixme by the sake of whatever, please make it better
        maneuverElements.clear();
    }
}
