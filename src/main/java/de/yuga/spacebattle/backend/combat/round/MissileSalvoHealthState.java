package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class MissileSalvoHealthState {

    /**
     * The initial composition of the salvo by missile type and amount.
     */
    @Nonnull
    private final MissileAmmunitionState initialAmountByType;

    /**
     * The current composition of the salvo by missile type and amount.
     */
    @Nonnull
    private final MissileAmmunitionState currentAmountByType;

    /**
     * The losses of this salvo during a {@link ECombatSubPhase} by missile type and amount.
     */
    @Nonnull
    private final List<MissileAmmunitionProfile> losses = new ArrayList<>();

    public MissileSalvoHealthState(@Nonnull final FleetRoundState actorsState,
                                   @Nonnull final Set<EWeaponAlignment> applicableAlignments,
                                   @Nonnull final Set<Missile> applicableMissiles) {
        Preconditions.checkNotNull(actorsState, "actorsState must not be empty");
        Preconditions.checkNotNull(applicableAlignments, "applicableAlignments must not be empty");
        Preconditions.checkNotNull(applicableMissiles, "applicableMissiles must not be empty");

        final Map<Missile, Integer> amountByType = new HashMap<>();

        actorsState.getFightingWarShips()
                .filter(WarshipHealthState::isFightingCapable)
                .forEach(w -> w.getFittings().entrySet().stream()
                        // filter active fittings
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .filter(a -> a.getWeaponType() == EWeaponType.MISSILE)
                        .filter(a -> a.getLauncher() != null)
                        .filter(f -> applicableAlignments.contains(f.getWeaponAlignment()))
                        .forEach(alignedFitting -> {
                            final Launcher launcher = alignedFitting.getLauncher();
                            final int amountOfLauncher = alignedFitting.getAmount();

                            final Set<Missile> allowedMissiles = launcher.getAllowedMissiles();
                            final HashSet<Missile> missiles = new HashSet<>(allowedMissiles);
                            missiles.removeIf(m -> !applicableMissiles.contains(m));
                            for (final Missile missile : missiles) {
                                final MissileAmmunitionState missileAmmunitionState = w.getMissileAmmunitionState();
                                final int remainingShots = missileAmmunitionState.getRemainingShots(missile);
                                if (remainingShots <= 0) {
                                    return;
                                }

                                // setting missiles to the salvo
                                if (remainingShots >= amountOfLauncher) {
                                    amountByType.merge(missile, amountOfLauncher, Integer::sum);
                                    missileAmmunitionState.reduce(missile, amountOfLauncher);
                                } else {
                                    amountByType.merge(missile, remainingShots, Integer::sum);
                                    missileAmmunitionState.reduce(missile, remainingShots);
                                }
                            }
                        }));

        this.initialAmountByType = new MissileAmmunitionState(amountByType);
        this.currentAmountByType = new MissileAmmunitionState(amountByType);
    }

    @Nonnull
    public Map<Missile, Integer> getInitialAmountByType() {
        return initialAmountByType.getRemainingShots();
    }

    @Nonnull
    public Map<Missile, Integer> getCurrentAmountByType() {
        return currentAmountByType.getRemainingShots();
    }

    @Nonnull
    public List<MissileAmmunitionProfile> getLosses() {
        return losses;
    }

    @Nonnull
    public Map<Missile, Integer> getLossesByType(@Nonnull final CombatRound combatRound,
                                                 @Nonnull final ECombatSubPhase combatSubPhase) {
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");
        Preconditions.checkNotNull(combatSubPhase, "combatSubPhase must not be empty");

        return losses.stream()
                .filter(p -> p.getCombatRound().equals(combatRound) && p.getCombatSubPhase() == combatSubPhase)
                .map(MissileAmmunitionProfile::getAmmunitionState)
                .map(MissileAmmunitionState::getRemainingShots)
                .findFirst()
                .orElse(new HashMap<>());
    }

    @Nonnull
    public Map<Missile, Integer> getAmountByTypeAtEndOfCombatRound(@Nonnull final CombatRound combatRound) {
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");

        final MissileAmmunitionState missileAmmunitionState = new MissileAmmunitionState(initialAmountByType.getRemainingShots());

        losses.stream()
                .filter(p -> p.getCombatRound().getNo() <= combatRound.getNo())
                .forEach(missileAmmunitionProfile -> missileAmmunitionProfile.getAmmunitionState().getRemainingShots()
                        .forEach(missileAmmunitionState::reduce));

        return missileAmmunitionState.getRemainingShots();
    }

    /**
     * Checks if the salvo has missiles in the air.
     *
     * @return <code>true</code> if the salvo is action and dangerous, <code>false</code> otherwise
     */
    public boolean isActive() {
        return getCurrentAmountByType().values().stream().anyMatch(amount -> amount > 0);
    }

    /**
     * Calculates the salvos attack range.
     *
     * @return the warheads range
     */
    @Nonnull
    public Distance getAttackRange() {
        if (!isActive()) {
            return Distance.ZERO.clone();
        }
        final List<Distance> damageProjectionRanges = getCurrentAmountByType().keySet().stream()
                .map(missile -> missile.getWarhead().getDamageProjectionRange())
                .sorted()
                .collect(Collectors.toList());
        // get longest damage projection range to detect if it is in range
        return damageProjectionRanges.get(damageProjectionRanges.size() - 1);
    }

    @Nonnull
    public Distance getRangePerCombatRound(@Nonnull final Velocity initialVelocity, final int endurance) {
        Preconditions.checkNotNull(initialVelocity, "initialVelocity must not be empty");

        if (!isActive()) {
            return Distance.ZERO;
        }
        final List<Distance> rangesPerCombatRoundAsc = getCurrentAmountByType().keySet().stream()
                .map(m -> m.getRangeOverEndurance(initialVelocity, endurance))
                .sorted()
                .collect(Collectors.toList());
        return rangesPerCombatRoundAsc.get(0);
    }

    public void addLostMissiles(@Nonnull final ECombatSubPhase combatSubPhase,
                                @Nonnull final CombatRound combatRound,
                                @Nonnull final Missile missile,
                                final int lostAmount) {
        Preconditions.checkNotNull(combatSubPhase, "combatSubPhase must not be empty");
        Preconditions.checkNotNull(combatRound, "combatRound must not be empty");
        Preconditions.checkNotNull(missile, "missile must not be empty");

        currentAmountByType.reduce(missile, lostAmount);

        MissileAmmunitionProfile missileAmmunitionProfile = losses.stream()
                .filter(p -> p.getCombatRound().equals(combatRound) && p.getCombatSubPhase() == combatSubPhase)
                .findFirst()
                .orElse(null);
        if (missileAmmunitionProfile == null) {
            missileAmmunitionProfile = new MissileAmmunitionProfile(combatRound, combatSubPhase);
            losses.add(missileAmmunitionProfile);
        }
        missileAmmunitionProfile.getAmmunitionState().increase(missile, lostAmount);
    }
}
