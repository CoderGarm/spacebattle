package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassStatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.StarShipSvgHelper;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public abstract class ShipClassLayout<GenericSubject> extends VerticalLayout implements StatsLayout<GenericSubject>, BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassLayout.class);

    private final ShipClassStatisticsDisplay shipClassStatisticsDisplay = new ShipClassStatisticsDisplay();

    protected StarShipSvgHelper starShipSvgHelper;

    protected Svg canvas;

    @Nonnull
    public ShipClassStatisticsDisplay getShipClassStatDisplay() {
        return shipClassStatisticsDisplay;
    }

    @Nonnull
    @Override
    public StatisticsDisplay getStatisticsComponent() {
        return shipClassStatisticsDisplay;
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
