package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ShipClassWrapper {

    @Nonnull
    private final ShipClass shipClass;

    @Nonnull
    private Map<Module, Integer> possibleModules = new HashMap<>();

    @Nonnull
    private Collection<Hull> possibleHulls = new HashSet<>();

    public ShipClassWrapper(@Nonnull ShipClass shipClass) {
        this.shipClass = shipClass;
    }

    @Nonnull
    public Map<Module, Integer> getPossibleModules() {
        return possibleModules;
    }

    public void setPossibleModules(@Nullable final Map<Module, Integer> possibleModules) {
        if (possibleModules == null) {
            return;
        }
        this.possibleModules = possibleModules;
    }

    @Nonnull
    public Collection<Hull> getPossibleHulls() {
        return possibleHulls;
    }

    public void setPossibleHulls(@Nonnull final Collection<Hull> possibleHulls) {
        this.possibleHulls = possibleHulls;
    }

    public String getName() {
        return shipClass.getName();
    }

    public void setName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        shipClass.setName(name);
    }

    public void setModules(@Nonnull final Map<Module, Integer> moduleIntegerMap) {
        Preconditions.checkNotNull(moduleIntegerMap, "moduleIntegerMap shouldn't be null!");

        shipClass.setModules(moduleIntegerMap);
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }

    public Hull getHull() {
        return shipClass.getHull();
    }

    public Map<Module, Integer> getModules() {
        return shipClass.getModules();
    }

    public void setHull(@Nonnull final Collection<Hull> hulls) {
        Preconditions.checkNotNull(hulls, "hulls shouldn't be null!");

        shipClass.setHull(new ArrayList<>(hulls).get(0));
    }
}
