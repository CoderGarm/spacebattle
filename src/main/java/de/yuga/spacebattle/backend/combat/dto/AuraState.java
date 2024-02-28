package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.MissileAmmunitionState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AuraState {

    @Nonnull
    private final Fleet actor;

    @Nonnull
    private final CombatRound combatRound;

    @Nonnull
    private final Map<EWeaponAlignment, AlignedAuraState> alignedAuraStates = new HashMap<>();

    public AuraState(@Nonnull final Cage cage, @Nonnull final Fleet actor, @Nonnull final FleetRoundState actorsRoundState) {
        Preconditions.checkNotNull(cage, "cage must not be empty");
        Preconditions.checkNotNull(actor, "actor must not be empty");
        Preconditions.checkNotNull(actorsRoundState, "actorsRoundState must not be empty");

        this.actor = actor;
        this.combatRound = cage.getCurrentCombatRound().clone();

        alignedAuraStates.putAll(Arrays.stream(EWeaponAlignment.values()).collect(Collectors.toMap(ewa -> ewa, AlignedAuraState::new)));

        final boolean isFirstRound = this.combatRound.getNo() == 1;
        final Fleet opponent = cage.getParticipatingFleets().stream().filter(f -> !f.equals(actor)).findFirst().orElseThrow();
        final FleetRoundState opponentsState = isFirstRound ? null : cage.getCurrentStateByFleet(opponent);

        actorsRoundState.getFleetHealthState().getWarshipHealthStates().values().forEach(w -> {
            final List<AlignedFitting> activeFittings = w.getActiveFittings();
            final MissileAmmunitionState missileAmmunitionState = w.getMissileAmmunitionState();

            for (final AlignedFitting activeFitting : activeFittings) {
                final EWeaponAlignment weaponAlignment = activeFitting.getWeaponAlignment();
                final AlignedAuraState orDefault = alignedAuraStates.getOrDefault(weaponAlignment, new AlignedAuraState(weaponAlignment));

                final Launcher launcher = activeFitting.getLauncher();
                if (launcher != null) {
                    final Function<Missile, Distance> distanceFunction = getMissileDistanceFunction(actorsRoundState, weaponAlignment, opponentsState);
                    final Distance missileRange = launcher.getAllowedMissiles().stream()
                            .filter(missileAmmunitionState::hasShotsLeft)
                            .map(distanceFunction)
                            .reduce((o1, o2) -> o1.compareTo(o2) < 0 ? o1 : o2)
                            .orElse(null);


                    final EWeaponType weaponType = launcher.getWeaponType();
                    switch (weaponType) {
                        case MISSILE:
                            orDefault.setAntiShipMissileRange(missileRange);
                            break;
                        case COUNTER_MISSILE:
                            orDefault.setAntiMissileMissileRange(missileRange);
                            break;

                    }
                }
                final Weapon weapon = activeFitting.getWeapon();
                if (weapon != null) {
                    orDefault.setWeaponRange(weapon.getDamageProjectionRange());
                }
                alignedAuraStates.put(weaponAlignment, orDefault);
            }

        });
    }

    @Nonnull
    private static Function<Missile, Distance> getMissileDistanceFunction(@Nonnull final FleetRoundState actorsRoundState,
                                                                          @Nonnull final EWeaponAlignment weaponAlignment,
                                                                          @Nullable final FleetRoundState opponentsState) {
        Preconditions.checkNotNull(actorsRoundState, "actorsRoundState must not be empty");
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment must not be empty");

        final Function<Missile, Distance> distanceFunction;
        switch (weaponAlignment) {
            case BOW:
                distanceFunction = getMissileDistanceFunction(actorsRoundState, opponentsState);
                break;
            case STERN:
                distanceFunction = getMissileDistanceFunction(actorsRoundState);
                break;
            default:
            case BROADSIDE:
                distanceFunction = getMissileDistanceFunction();
                break;
        }
        return distanceFunction;
    }

    @Nonnull
    private static Function<Missile, Distance> getMissileDistanceFunction(@Nonnull final FleetRoundState actorsRoundState, @Nullable final FleetRoundState opponentsState) {
        Preconditions.checkNotNull(actorsRoundState, "actorsRoundState must not be empty");

        return m -> opponentsState != null ? m.getMaximumMissileRange(actorsRoundState, opponentsState) : m.getMaximumMissileRange();
    }

    @Nonnull
    private static Function<Missile, Distance> getMissileDistanceFunction(@Nonnull final FleetRoundState actorsRoundState) {
        Preconditions.checkNotNull(actorsRoundState, "actorsRoundState must not be empty");

        return m -> m.getRearMissileRange(actorsRoundState);
    }

    @Nonnull
    private static Function<Missile, Distance> getMissileDistanceFunction() {
        return Missile::getMaximumMissileRange;
    }

    @Nonnull
    public Fleet getActor() {
        return actor;
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public Map<EWeaponAlignment, AlignedAuraState> getAlignedAuraStates() {
        return alignedAuraStates;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final AuraState auraState = (AuraState) o;

        return new EqualsBuilder().append(actor, auraState.actor).append(combatRound, auraState.combatRound).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(actor).append(combatRound).toHashCode();
    }
}
