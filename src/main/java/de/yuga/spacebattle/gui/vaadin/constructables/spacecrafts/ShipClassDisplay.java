package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleMultiDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShipClassDisplay extends ShipClassLayout<ShipClass> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassDisplay.class);

    @Nonnull
    private final Binder<ShipClass> binderShipClass = new Binder<>(ShipClass.class);


    public ShipClassDisplay() {

        setClassName("module-display");

        createHullSvg();

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderShipClass.forField(nameReadOnly).bind(ShipClass::getName, null);

        binderShipClass.forField(getShipClassStatDisplay()).bind(shipClass -> shipClass, null);

        final HullDisplay hullDisplay = new HullDisplay();
        final ReadOnlyHasValue<Hull> hullReadOnly = new ReadOnlyHasValue<>(hullDisplay::update);
        binderShipClass.forField(hullReadOnly).bind(ShipClass::getHull, null);

        final ModuleMultiDisplay moduleMultiDisplay = new ModuleMultiDisplay(starShipSvgHelper);
        final ReadOnlyHasValue<ShipClass> moduleReadOnly = new ReadOnlyHasValue<>(moduleMultiDisplay::setValue);
        binderShipClass.forField(moduleReadOnly).bind(shipClass -> shipClass, null);

        add(name, hullDisplay, moduleMultiDisplay);
    }


    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClass the ship class to display
     */
    @Override
    public void update(@Nullable final ShipClass shipClass) {
        binderShipClass.setBean(shipClass);
    }

    @Override
    protected void addDragStartListener() {

    }
}
