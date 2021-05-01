package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.Circle;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.ViewBoxDefinition;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.OrbitCoordinatesDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.FLEET_SELECTOR_ID_PREFIX;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.PLANET_SELECTOR_ID_PREFIX;

/**
 * Home of a star system map and it's functionality.
 */
@CssImport("./styles/views/main/details/starMap.css")
public class StarSystemDisplay extends StarSystemLayout implements HasValue<AbstractField.ComponentValueChangeEvent<StarSystemDisplay, StarSystem>, StarSystem> {

    @Nonnull
    private final Binder<StarSystem> binder = new Binder<>(StarSystem.class);

    @Nonnull
    private final StarSystemService starsystemService = ViewHelper.getService(StarSystemService.class);

    @Nonnull
    private final Map<String, PlanetDisplay> planetMap = new HashMap<>();

    @Nonnull
    private final Map<String, Fleet> fleetMap = new HashMap<>();

    @Nonnull
    private final Scroller scroller = new Scroller();

    @Nonnull
    private final Scroller content = new Scroller();

    @Nonnull
    private Svg canvas = startCanvas();

    /**
     * This non-null is a prerequisite be cause this is an absolute requirement and a NPE is not a shame if this is not there.
     */
    @Nonnull
    private ViewBoxDefinition viewBoxDefinition;

    public StarSystemDisplay() {

        final VerticalLayout systemStats = new VerticalLayout();

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameText = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameText).bind(starSystem -> "Name: " + starSystem.getName(), null);

        final OrbitCoordinatesDisplay orbitCoordinatesDisplay = new OrbitCoordinatesDisplay();
        binder.forField(orbitCoordinatesDisplay).bind(StarSystem::getOrbit, null);

        final Label planetCount = new Label();
        final ReadOnlyHasValue<String> planetCountText = new ReadOnlyHasValue<>(planetCount::setText);
        binder.forField(planetCountText).bind(starSystem -> "Planet count: " + starSystem.getPlanets().size(), null);

        systemStats.add(name, orbitCoordinatesDisplay, planetCount);
        final HorizontalLayout mainStatsLayout = new HorizontalLayout();

        mainStatsLayout.add(systemStats, content);

        setHeightFull();
        setWidthFull();
        scroller.setWidthFull();
        scroller.setHeightFull();
        mainStatsLayout.setWidthFull();

        scroller.addClassName("mapScroller");
        add(/*mainStatsLayout,*/ scroller);
    }

    private Svg startCanvas() {
        final Svg canvas = ViewBoxDefinition.createStarMapCanvas("planetMapID");
        // todo known issue: drag listener sucks if no movement must be possible
        canvas.addDragStartListener(event -> {
            final SvgElement element = event.getElement();
            final String id = element.getId();
            if (id.startsWith(PLANET_SELECTOR_ID_PREFIX)) {
                final PlanetDisplay planetDisplay = planetMap.get(id);
                content.setContent(planetDisplay);
                final Orbit orbit = planetDisplay.getValue().getOrbit();
                viewBoxDefinition.dragListenerWorkaround(element, orbit);
            } else if (id.startsWith(FLEET_SELECTOR_ID_PREFIX)) {
                // todo replace dead end by some action possibilities
            }
        });
        scroller.setContent(canvas);
        return canvas;
    }

    @Override
    public void setValue(@Nullable final StarSystem value) {

        binder.setBean(value);
        canvas = startCanvas();
        if (value == null) {
            return;
        }

        final Set<Planet> planets = value.getPlanets();
        final Set<String> orbitIDs = planets.stream()
                .map(ViewBoxDefinition::createPlanetID).collect(Collectors.toSet());

        final Set<String> toRemove = planetMap.keySet().stream()
                .filter(id -> !orbitIDs.contains(id)).collect(Collectors.toSet());

        planetMap.keySet().removeAll(toRemove);

        planets.forEach(planet -> {
            final String orbitID = ViewBoxDefinition.createPlanetID(planet);
            PlanetDisplay planetDisplay = planetMap.get(orbitID);
            if (planetDisplay == null) {
                planetDisplay = new PlanetDisplay();
                planetMap.put(orbitID, planetDisplay);
            }
            planetDisplay.setValue(planet);
        });

        value.getFleets().forEach(fleet -> {
            final String fleetID = ViewBoxDefinition.createFleetID(fleet);
            fleetMap.putIfAbsent(fleetID, fleet);
        });
    }

        viewBoxDefinition = new ViewBoxDefinition(value, canvas);
    }

    @Override
    public StarSystem getValue() {
        return binder.getBean();
    }

    /**
     * Will refresh the {@link StarSystemDisplay#canvas} while this is obviously not stored.
     */
    @Override
    public void refresh() {

        StarSystem starSystem = getValue();
        if (starSystem != null) {
            starSystem = starsystemService.find(starSystem);
        }
        setValue(starSystem);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<StarSystemDisplay, StarSystem>> listener) {
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
}
