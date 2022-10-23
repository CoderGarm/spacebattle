package de.yuga.spacebattle.backend.enums;

public enum EJobPriority {

    NONE(0),
    PRIORITY(1);

    private final int priority;

    EJobPriority(final int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
