package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleMultiDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ShipClassDisplay extends ShipClassLayout<ShipClass> {

    @Nonnull
    private final Binder<ShipClass> binderShipClass = new Binder<>(ShipClass.class);

    public ShipClassDisplay() {
        Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderShipClass.forField(nameReadOnly).bind(ShipClass::getName, null);

        binderShipClass.forField(getShipClassStatDisplay()).bind(shipClass -> shipClass, null);

        HullDisplay hullDisplay = new HullDisplay();
        final ReadOnlyHasValue<Hull> hullReadOnly = new ReadOnlyHasValue<>(hullDisplay::update);
        binderShipClass.forField(hullReadOnly).bind(ShipClass::getHull, null);

        ModuleMultiDisplay moduleMultiDisplay = new ModuleMultiDisplay();
        final ReadOnlyHasValue<Map<Module, Integer>> moduleReadOnly = new ReadOnlyHasValue<>(moduleMultiDisplay::update);
        binderShipClass.forField(moduleReadOnly).bind(ShipClass::getModules, null);

        add(name, hullDisplay, moduleMultiDisplay);
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClass the ship class to display
     */
    @Override
    public void update(@Nullable final ShipClass shipClass) {
        binderShipClass.readBean(shipClass);
    }
}
