package de.yuga.spacebattle.backend.combat;

import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.enums.EHitArea;

import java.math.BigDecimal;

public class BattleStaticLogger {

    private static final boolean logIt = false;

    public static void logMovement(final CombatRound combatRound, final Fleet actor, final Orbit origin, final Orbit destination) {
        if (!logIt) {
            return;
        }
        final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " moves from " + origin + " to " + destination;
        System.out.println(msg);
    }

    public static void logMessageWithPretendingCombatRoundS(final CombatRound currentCombatRound, final String msg) {
        if (!logIt) {
            return;
        }
        final String x = "#" + currentCombatRound.getNo() + ": " + msg;
        System.out.println(x);
    }

    public static void logCombatRoundDone(final CombatRound currentCombatRound, final boolean isDone) {
        if (!logIt) {
            return;
        }
        final String msg = "current round '" + currentCombatRound.getNo() + "' - " + (isDone ? " ends battle " : " goes on");
        System.out.println(msg);
    }

    public static void logMissileDetonation(final CombatRound combatRound, final Fleet actor, final Fleet target, final Missile missile, final int missileAmount) {
        if (!logIt) {
            return;
        }
        final long damageValue = missile.getWarhead().getDamageValue() * missileAmount;
        final String msg = "#" + combatRound.getNo() + " salvo from " + actor.getName() + " detonates " + missileAmount + " of " + missile.getTypeName() + " with damage of " + damageValue + " at " + target.getName();
        System.out.println(msg);
    }


    public static void logElokaHitMissile(final CombatRound combatRound, final Fleet actor, final Fleet target, final Missile missile, final Integer lostAmount, final int newValue) {
        if (!logIt) {
            return;
        }
        final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " eloka down " + lostAmount + " missiles of " + missile.getTypeName() + " from " + target.getName() + " - left over " + newValue + " missiles.";
        System.out.println(msg);
    }


    public static void logCounterMissileHitMissile(final CombatRound combatRound, final Fleet actor, final Missile missile, final Integer lostAmount, final int newValue) {
        if (!logIt) {
            return;
        }
        final String msg = "#" + combatRound.getNo() + " " + actor.getName() + " shots down " + lostAmount + " missiles of " + missile.getTypeName() + " - left over " + newValue + " missiles.";
        System.out.println(msg);
    }

    public static void logBeamVolleyHit(final CombatRound combatRound, final Fleet actor, final Fleet target, final ShipClass shipClass, final Long amount) {
        if (!logIt) {
            return;
        }
        final String msg = "#" + combatRound.getNo() + " beam volley from " + actor.getName() + " attacks " + target.getName() + " with " + amount + " of " + shipClass.getName();
        System.out.println(msg);
    }

    public static void logMissileRelease(final CombatRound combatRound, final Orbit initialPosition, final Fleet actor, final Fleet target, final BigDecimal distance, final int amount) {
        if (!logIt) {
            return;
        }
        final String distanceString = DistanceCalculator.getDistanceAsStringWithUnit(distance);
        final String msg = "#" + combatRound.getNo() + " release missile salvo from " + actor.getName() + " against " + target.getName() + " with " + amount + " missiles over " + distanceString;
        System.out.println(msg);
    }

    public static void logBeamVolleyRelease(final CombatRound combatRound, final Fleet actor, final ShipClass shipClass, final Integer amount) {
        if (!logIt) {
            return;
        }
        final String msg = "#" + combatRound.getNo() + " release beam volley from " + actor.getName() + " attacks with " + amount + " of " + shipClass.getName();
        System.out.println(msg);
    }

    public static void startBattleAtDistance(final BigDecimal initialCageDiameter) {
        if (!logIt) {
            return;
        }
        final String msg = "Start battle at distance: " + DistanceCalculator.getDistanceAsStringWithUnit(initialCageDiameter);
        System.out.println(msg);
    }

    public static void elokaHits(final CombatRound combatRound, final Missile missile, final BigDecimal distance, final boolean isLost) {
        if (!logIt) {
            return;
        }
        final String hits = isLost ? "hits" : "misses";
        final String msg = "\tEloka " + hits + " missile at " + DistanceCalculator.getDistanceAsStringWithUnit(distance);
        System.out.println(msg);
    }

    public static void logLoss(final WarShip warShip) {
        if (!logIt) {
            return;
        }
        final String msg = "\tLoss of " + warShip.getName() + " of user " + warShip.getShipClass().getOwner().getUsername();
        System.out.println(msg);
    }

    public static void logHit(final WarshipHealthState warshipHealthState, final long damageValue, final int state, final EHitArea attackedPart, final boolean isAlive, final boolean isFightingCapable) {
        if (!logIt) {
            return;
        }
        final WarShip warShip = warshipHealthState.getWarShip();
        final String killMarker = isAlive ? (isFightingCapable ? "\tHit" : "\tFinal hit") : "\tKill hit";
        final String msg = killMarker + " at " + warShip.getName() + " of user " + warShip.getShipClass().getOwner().getUsername() + " onto " + attackedPart + " with damage of " + damageValue + " current state: " + state
                + "\n\t\t " + warshipHealthState.asString();
        System.out.println(msg);
    }

    public static void logEnterBattleField(final WarshipHealthState warshipHealthState) {
        if (!logIt) {
            return;
        }
        final String msg = warshipHealthState.asString();
        System.out.println(msg);
    }

    public static void logHandleMissileMovementS(final MissileSalvo volley) {
        if (!logIt) {
            return;
        }
        final CombatRound combatRound = volley.getCombatRound();
        final Fleet actor = volley.getActor();
        final Orbit position = volley.getLastPosition();
        final Orbit newPos = volley.getPosition();
        final int amount = volley.getMissileSalvoHealthState().getCurrentAmountByType().values().stream().mapToInt(Integer::intValue).sum();
        final Orbit targetPosition = volley.getTargetPosition();
        final BigDecimal currentDistance = targetPosition.getDistance(newPos);
        final BigDecimal initialDistance = volley.getInitialDistance();
        final BigDecimal rangePerCombatRound = volley.getRangePerCombatRound();
        final int toTravel = initialDistance.subtract(currentDistance).divide(rangePerCombatRound, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION).abs().intValue();
        final String msg = "#" + combatRound.getNo() + " salvo of " + actor.getName() + " containing of " + amount + " goes from " + position + " to " + newPos + " with target " + targetPosition + " and still have to travel " + toTravel + " ticks.";
        System.out.println(msg);
    }

}
