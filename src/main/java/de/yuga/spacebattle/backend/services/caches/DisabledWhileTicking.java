package de.yuga.spacebattle.backend.services.caches;

public abstract class DisabledWhileTicking {

    private boolean isActive = true;

    public void disable() {
        isActive = false;
    }

    public void enable() {
        isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }
}
