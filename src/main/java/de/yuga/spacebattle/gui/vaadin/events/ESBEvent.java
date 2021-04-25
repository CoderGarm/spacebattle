package de.yuga.spacebattle.gui.vaadin.events;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public enum ESBEvent {

    TICK_DONE(0), // if a tick was processed
    LOGIN(1), // user has logged in
    LOGOUT(2), // user has logged out
    USER_COMPLETE(3), // creating a new user - all data collected
    USER_INCOMPLETE(4), // creating a new user - NOT all data collected
    CONSTRUCTION_JOB_BUILDING_START(5), // when a construction job should start
    CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED(6), // when a construction job has started
    SHIP_CLASS_SUBMITTED(7), // when a ship class is ready to be stored
    SHIP_CLASS_DELETION(8), // when a ship should be deleted
    ORBITAL_CONSTRUCTION_JOB_BUILDING_START(9), // when a ship construction job should start
    ORBITAL_CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED(10), // when a ship construction job has started
    RESEARCH_JOB_START(11), // when a research job should start
    RESEARCH_JOB_FEEDBACK_STARTED(12), // when a research job has started
    ;

    int sequence;

    ESBEvent(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public String getName() {
        return name();
    }

    @Nullable
    public static ESBEvent getByName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        return Arrays.stream(ESBEvent.values()).filter(esbEvent -> esbEvent.getName().equals(name)).findFirst().orElse(null);
    }
}
