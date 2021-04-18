package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.Circle;
import com.vaadin.flow.component.svg.elements.SvgElement;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.ViewBoxDefinition;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.misc.ViewBoxDefinition.EViewBoxType.UNIVERSE;

/**
 * Home of the universe map and it's functionality.
 */
@CssImport("./styles/views/main/details/starMap.css")
public class StarSystemOverviewDisplay extends StarSystemLayout implements HasValue<AbstractField.ComponentValueChangeEvent<StarSystemOverviewDisplay, Set<StarSystem>>, Set<StarSystem>> {

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final StarSystemService starsystemService = ViewHelper.getService(StarSystemService.class);

    @Nonnull
    private Map<String, StarSystem> starSystemDisplayMap = new HashMap<>();

    @Nonnull
    private final Scroller scroller = new Scroller();

    @Nonnull
    private Svg canvas = startCanvas();

    public StarSystemOverviewDisplay() {

        uiEventBus.subscribe(this);

        setHeightFull();
        setWidthFull();
        ViewHelper.setWidth(scroller, "100%");
        ViewHelper.setHeight(scroller, "100%");

        scroller.addClassName("mapScroller");
        add(scroller);
    }

    private Svg startCanvas() {
        final Svg canvas = ViewBoxDefinition.createMapCanvas("universeMapID");
        // todo known issue: drag listener sucks if no movement must be possible
        canvas.addDragStartListener(event -> {
            SvgElement element = event.getElement();
            String id = element.getId();
            final StarSystem starSystem = starSystemDisplayMap.get(id);
            NotificationHelper.notify("yeah, its not a feature", 500);
            uiEventBus.publish(starSystem, ESBEvent.DISPLAY_PLANETARY_SYSTEM.name());
        });
        scroller.setContent(canvas);
        return canvas;
    }

    @Override
    public void setValue(@Nullable final Set<StarSystem> starSystems) {

        starSystemDisplayMap.clear();
        canvas = startCanvas();
        if (starSystems == null || starSystems.isEmpty()) {
            return;
        }
        starSystemDisplayMap = starSystems.stream().collect(Collectors.toMap(o -> o.getOrbit().getOrbitID(), Function.identity()));

        final Set<Orbit> orbits = starSystems.stream()
                .map(StarSystem::getOrbit)
                .collect(Collectors.toSet());

        new ViewBoxDefinition(UNIVERSE, orbits, canvas);
        starSystems.forEach(starSystem -> {
            final Orbit orbit = starSystem.getOrbit();
            final String circleID = orbit.getOrbitID();
            final Circle circle = new Circle(circleID, ViewBoxDefinition.SYSTEM_RADIUS);
            circle.center(orbit.getXCoordinate(), orbit.getYCoordinate());
            circle.setFillColor("red");
            circle.setDraggable(true);

            canvas.add(circle);
        });
    }

    @Override
    public Set<StarSystem> getValue() {
        return new HashSet<>(starSystemDisplayMap.values());
    }

    /**
     * Will refresh the {@link StarSystemOverviewDisplay#canvas} while this is obviously not stored.
     */
    @Override
    public void refresh() {
        setValue(new HashSet<>(starsystemService.findAll()));
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<StarSystemOverviewDisplay, Set<StarSystem>>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {

    }
}
