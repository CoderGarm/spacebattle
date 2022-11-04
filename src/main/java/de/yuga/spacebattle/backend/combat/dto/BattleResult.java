package de.yuga.spacebattle.backend.combat.dto;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class BattleResult {

    @Nonnull
    private final Cage cage;

    @Nonnull
    private final FleetClash fleetClash;

    @Nonnull
    private final Set<WarShip> losses = new HashSet<>();

    @Nonnull
    private final Map<WarShip, WarshipHealthState> warshipHealthStates = new HashMap<>();

    @Nonnull
    private final List<FleetRoundState> roundStates;

    @Nonnull
    private final List<MovementAction> movements;

    @Nonnull
    private final List<BeamVolley> beamVolleys;

    @Nonnull
    private final List<MissileSalvo> missileSalvos;

    public BattleResult(@Nonnull final Cage cage) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");

        this.cage = cage;
        this.fleetClash = cage.getFleetClash();
        this.roundStates = cage.getHistoryOfRounds().stream()
                .sorted(Comparator.comparing(FleetRoundState::getCombatRound))
                .collect(Collectors.toList());
        this.movements = cage.getHistoryMovement();
        this.beamVolleys = cage.getHistoryOfBeamSalvos();
        this.missileSalvos = cage.getHistoryOfMissileSalvos();

        final Map<WarShip, WarshipHealthState> losses = new HashMap<>();
        this.roundStates.forEach(fleetRoundState -> losses.putAll(fleetRoundState.getFleetHealthState().getLosses()));
        this.losses.addAll(losses.keySet());

        // add all losses - they have no "existing state" afterwards
        final Set<WarshipHealthState> warshipHealthStateSet = new HashSet<>(losses.values());
        // all others in reverse order to display the latest states
        final List<FleetRoundState> reverse = new ArrayList<>(this.roundStates);
        Collections.reverse(reverse);
        reverse.forEach(fleetRoundState -> warshipHealthStateSet.addAll(fleetRoundState.getFleetHealthState().getWarshipHealthStates().values()));
        warshipHealthStateSet.forEach(state -> warshipHealthStates.put(state.getWarShip(), state));
    }

    @Nonnull
    public Cage getCage() {
        return cage;
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
    public Map<WarShip, WarshipHealthState> getWarshipHealthStates() {
        return warshipHealthStates;
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
