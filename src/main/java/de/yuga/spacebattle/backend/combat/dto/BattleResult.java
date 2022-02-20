package de.yuga.spacebattle.backend.combat.dto;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BattleResult {

    @Nonnull
    private final FleetClash fleetClash;

    @Nonnull
    private final Set<WarShip> losses = new HashSet<>();

    @Nonnull
    private final List<FleetRoundState> roundStates;

    @Nonnull
    private final List<MovementAction> movements;

    @Nonnull
    private final List<BeamVolley> beamVolleys;

    @Nonnull
    private final List<MissileSalvo> missileSalvos;

    public BattleResult(@Nonnull final FleetClash fleetClash,
                        @Nonnull final List<FleetRoundState> roundStates,
                        @Nonnull final List<MovementAction> historyMovement,
                        @Nonnull final List<BeamVolley> historyOfBeamSalvos,
                        @Nonnull final List<MissileSalvo> historyOfMissileSalvos) {
        Preconditions.checkNotNull(fleetClash, "fleetClash shouldn't be null!");
        Preconditions.checkNotNull(roundStates, "roundStates shouldn't be null!");
        Preconditions.checkNotNull(historyMovement, "historyMovement shouldn't be null!");
        Preconditions.checkNotNull(historyOfBeamSalvos, "historyOfBeamSalvos shouldn't be null!");
        Preconditions.checkNotNull(historyOfMissileSalvos, "historyOfMissileSalvos shouldn't be null!");

        this.fleetClash = fleetClash;
        this.roundStates = roundStates.stream().sorted(Comparator.comparing(FleetRoundState::getCombatRound)).collect(Collectors.toList());
        this.movements = historyMovement;
        this.beamVolleys = historyOfBeamSalvos;
        this.missileSalvos = historyOfMissileSalvos;

        this.roundStates.forEach(fleetRoundState -> this.losses.addAll(fleetRoundState.getFleetHealthState().getLosses().keySet()));
    }

    @Nonnull
    public FleetClash getFleetClash() {
        return fleetClash;
    }

    @Nonnull
    public Set<WarShip> getLosses() {
        return losses;
    }

    @Nonnull
    public List<FleetRoundState> getRoundStates() {
        return roundStates;
    }

    @Nonnull
    public List<MovementAction> getMovements() {
        return movements;
    }

    @Nonnull
    public List<BeamVolley> getBeamVolleys() {
        return beamVolleys;
    }

    @Nonnull
    public List<MissileSalvo> getMissileSalvos() {
        return missileSalvos;
    }
}
