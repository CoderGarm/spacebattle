package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ECombatPhase {

    MOVEMENT_PHASE(1, "Movement phase"),
    MISSILE_PHASE(2, "Missile phase"),
    INCOMING_WEAPON_FIRE_PHASE(3, "Incoming weapon fire phase"),
    FIRE_WEAPONS_PHASE(4, "Fire weapons phase");

    private final int orderNo;

    @Nonnull
    private final String title;

    ECombatPhase(final int orderNo, @Nonnull final String title) {
        Preconditions.checkNotNull(title, "title shouldn't be null!");

        this.orderNo = orderNo;
        this.title = title;
    }

    public int getOrderNo() {
        return orderNo;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    public enum ECombatSubPhase {

        MOVEMENT_PHASE(ECombatPhase.MOVEMENT_PHASE, 1, "Fleet movement sub-phase"),
        ELOKA_PHASE(MISSILE_PHASE, 1, "Counter missile Eloka sub-phase"),
        COUNTER_MISSILE_PHASE(MISSILE_PHASE, 2, "Counter missile Weapon sub-phase"),
        MISSILE_MOVEMENT_PHASE(MISSILE_PHASE, 3, "Missile movement sub-phase"),
        BEAM_FIRE_INCOMING_PHASE(INCOMING_WEAPON_FIRE_PHASE, 1, "Beam weapons damage application sub-phase"),
        MISSILE_FIRE_INCOMING_PHASE(INCOMING_WEAPON_FIRE_PHASE, 2, "Missile damage application sub-phase"),
        BEAM_FIRE_PHASE(FIRE_WEAPONS_PHASE, 1, "Fire beam weapons sub-phase"),
        MISSILE_FIRE_PHASE(FIRE_WEAPONS_PHASE, 2, "Fire missile salvos sub-phase"),
        ;

        @Nonnull
        private final ECombatPhase mainPhase;

        private final int orderNo;

        @Nonnull
        private final String title;

        ECombatSubPhase(@Nonnull final ECombatPhase mainPhase, final int orderNo, @Nonnull final String title) {
            Preconditions.checkNotNull(mainPhase, "mainPhase shouldn't be null!");
            Preconditions.checkNotNull(title, "title shouldn't be null!");

            this.mainPhase = mainPhase;
            this.orderNo = orderNo;
            this.title = title;
        }

        @Nonnull
        public ECombatPhase getMainPhase() {
            return mainPhase;
        }

        public int getOrderNo() {
            return orderNo;
        }

        @Nonnull
        public String getTitle() {
            return title;
        }

        @Nonnull
        public static List<ECombatSubPhase> getByCombatPhase(@Nonnull final ECombatPhase combatPhase) {
            Preconditions.checkNotNull(combatPhase, "combatPhase shouldn't be null!");

            return Arrays.stream(ECombatSubPhase.values()).filter(sub -> combatPhase == sub.getMainPhase()).collect(Collectors.toList());
        }
    }
}
