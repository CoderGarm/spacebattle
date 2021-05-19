package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassStatDisplay;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.StarShipSvgHelper;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public abstract class ShipClassLayout<T> extends VerticalLayout implements StatsLayout<T>, BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassLayout.class);

    private final ShipClassStatDisplay shipClassStatDisplay = new ShipClassStatDisplay();

    protected StarShipSvgHelper starShipSvgHelper;

    protected Svg canvas;

    @Nonnull
    public ShipClassStatDisplay getShipClassStatDisplay() {
        return shipClassStatDisplay;
    }

    @Nonnull
    @Override
    public Component getStatisticsComponent() {
        return shipClassStatDisplay;
    }

    /**
     * Just to create the drag start listener after rebuild the canvas
     */
    protected abstract void addDragStartListener();

    protected void createHullSvg() {
        if (starShipSvgHelper != null) {
            remove(canvas);
            starShipSvgHelper.createStarShipHull();
        } else {
            starShipSvgHelper = new StarShipSvgHelper();
        }
        canvas = starShipSvgHelper.getCanvas();

        addComponentAtIndex(0, canvas);
        addDragStartListener();
    }

    /**
     * Must refresh the SVG canvas while this is obviously not stored in the view.
     */
    public void refresh() {
        createHullSvg();
    }

    /**
     * This refreshing is necessary because a {@link Svg} will clear it's own content if out of scope.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        refresh();
    }
}
