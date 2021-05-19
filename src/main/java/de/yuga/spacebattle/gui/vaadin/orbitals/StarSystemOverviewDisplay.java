package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.SvgElement;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition;
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

@CssImport("./styles/views/main/details/starMap.css")
public class StarSystemOverviewDisplay extends StarSystemLayout implements HasValue<AbstractField.ComponentValueChangeEvent<StarSystemOverviewDisplay, Set<StarSystem>>, Set<StarSystem>> {

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final StarSystemService starsystemService = ViewHelper.getService(StarSystemService.class);

    @Nonnull
    private Map<String, StarSystem> starSystemDisplayMap = new HashMap<>();

    @Nonnull
    private final Scroller mapScroller = new Scroller();

    @Nonnull
    private Svg canvas = startCanvas();

    /**
     * This non-null is a prerequisite be cause this is an absolute requirement and a NPE is not a shame if this is not there.
     */
    @Nonnull
    private ViewBoxDefinition viewBoxDefinition;

    public StarSystemOverviewDisplay() {

        uiEventBus.subscribe(this);

        setHeightFull();
        setWidthFull();
        ViewHelper.setWidth(mapScroller, "100%");
        ViewHelper.setHeight(mapScroller, "100%");

        mapScroller.addClassName("mapScroller");
        add(mapScroller);
    }

    private Svg startCanvas() {
        final Svg canvas = ViewBoxDefinition.createStarMapCanvas("universeMapID");
        canvas.addDragStartListener(event -> {
            SvgElement element = event.getElement();
            String id = element.getId();
            final StarSystem starSystem = starSystemDisplayMap.get(id);
            viewBoxDefinition.resetPositionOfSvgElement(element);
            uiEventBus.publish(starSystem, ESBEvent.DISPLAY_PLANETARY_SYSTEM.name());
        });
        mapScroller.setContent(canvas);
        return canvas;
    }

    @Override
    public void setValue(@Nullable final Set<StarSystem> starSystems) {

        starSystemDisplayMap.clear();
        canvas = startCanvas();
        if (starSystems == null || starSystems.isEmpty()) {
            return;
        }
        starSystemDisplayMap = starSystems.stream().collect(Collectors.toMap(o -> ViewBoxDefinition.idCreateOrbitID(o.getOrbit()), Function.identity()));
        viewBoxDefinition = new ViewBoxDefinition(starSystems, canvas);
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
