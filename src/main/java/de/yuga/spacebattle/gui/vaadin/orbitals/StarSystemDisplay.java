package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.SvgElement;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.FleetMoveMergeSplitEdit;
import de.yuga.spacebattle.gui.vaadin.misc.SBDialog;
import de.yuga.spacebattle.gui.vaadin.misc.details.SBConfirmationDialog;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.OrbitCoordinatesHorizontalDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition;
import de.yuga.spacebattle.gui.vaadin.turn.action.MoveDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.misc.SBDialog.Position.INITIAL_TOP_LEFT;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.FLEET_SELECTOR_ID_PREFIX;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.PLANET_SELECTOR_ID_PREFIX;

/**
 * Home of a star system map and it's functionality.
 */
@CssImport("./styles/views/main/details/starMap.css")
public class StarSystemDisplay extends StarSystemLayout implements HasValue<AbstractField.ComponentValueChangeEvent<StarSystemDisplay, StarSystem>, StarSystem>, BeforeLeaveObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(StarSystemDisplay.class);

    @Nonnull
    private final Binder<StarSystem> binder = new Binder<>(StarSystem.class);

    @Nonnull
    private final StarSystemService starsystemService = ViewHelper.getService(StarSystemService.class);

    @Nonnull
    private final FleetService fleetService = ViewHelper.getService(FleetService.class);

    @Nonnull
    private final Map<String, PlanetDisplay> planetMap = new HashMap<>();

    @Nonnull
    private final Map<String, Fleet> fleetMap = new HashMap<>();

    @Nonnull
    private final Scroller mapScroller = new Scroller();

    @Nonnull
    private Svg canvas;

    /**
     * This non-null is a prerequisite be cause this is an absolute requirement and a NPE is not a shame if this is not there.
     */
    @Nonnull
    private ViewBoxDefinition viewBoxDefinition;

    /**
     * Holds every created dialog by the identifier of the dialog's subject.
     */
    private final Map<String, SBDialog> openDialogs = new HashMap<>();

    /**
     * Holds every registered listener for this and it's children component. But you have to register them manually.
     * Every {@link Registration} will be removed if it's part of this list and the component will be left.
     */
    private final List<Registration> registrationList = new ArrayList<>();

    public StarSystemDisplay() {
        canvas = startCanvas();
        final HorizontalLayout systemStats = new HorizontalLayout();

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameText = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameText).bind(starSystem -> "Name: " + starSystem.getName(), null);

        final OrbitCoordinatesHorizontalDisplay orbitCoordinatesHorizontalDisplay = new OrbitCoordinatesHorizontalDisplay();
        binder.forField(orbitCoordinatesHorizontalDisplay).bind(StarSystem::getOrbit, null);

        final Label planetCount = new Label();
        final ReadOnlyHasValue<String> planetCountText = new ReadOnlyHasValue<>(planetCount::setText);
        binder.forField(planetCountText).bind(starSystem -> "Planet count: " + starSystem.getPlanets().size(), null);

        systemStats.add(name, orbitCoordinatesHorizontalDisplay, planetCount);
        systemStats.setWidthFull();

        setHeight("90%");
        setWidthFull();
        mapScroller.setWidthFull();
        mapScroller.setHeightFull();

        mapScroller.addClassName("mapScroller");
        add(systemStats, mapScroller);
    }

    /**
     * Closes and deletes all dialogs.
     */
    public void closeDialogs() {
        removeRegisteredListeners();
        openDialogs.values().forEach(SBDialog::close);
        openDialogs.clear();
    }

    private Svg startCanvas() {
        final Svg canvas = ViewBoxDefinition.createStarMapCanvas("planetMapID");
        // todo known issue: drag listener sucks if no movement must be possible
        registrationList.add(attachDragStartListener(canvas));
        registrationList.add(addDragEndListener(canvas));

        mapScroller.setContent(canvas);
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

    /**
     * Attaches the drag start listener to the canvas and defined their behavior.
     *
     * <b>Attention:</b> The drag start listener interferes heavily with the drag end listener - events has to be separated by user interaction.
     *
     * @param canvas the canvas to attach the listener
     * @return
     */
    private Registration attachDragStartListener(@Nonnull final Svg canvas) {
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        return canvas.addDragStartListener(event -> {
            final SvgElement element = event.getElement();
            final String id = element.getId();

            if (id.startsWith(PLANET_SELECTOR_ID_PREFIX)) {
                final PlanetDisplay planetDisplay = planetMap.get(id);
                final Planet planet = planetDisplay.getValue();
                viewBoxDefinition.resetPositionOfSvgElement(element);

                SBDialog sbDialog = openDialogs.get(id);
                if (sbDialog != null) {
                    ((PlanetDisplay) sbDialog.getContent()).setValue(planet);
                    if (!sbDialog.isOpened()) {
                        sbDialog.open(INITIAL_TOP_LEFT);
                    }
                } else {
                    final PlanetDisplay fleetDisplay = new PlanetDisplay();
                    fleetDisplay.setValue(planet);
                    sbDialog = new SBDialog(fleetDisplay);
                    sbDialog.open(INITIAL_TOP_LEFT);
                    openDialogs.put(id, sbDialog);
                }
            } else if (id.startsWith(FLEET_SELECTOR_ID_PREFIX)) {
                final Fleet fleet = fleetMap.get(id);

                Set<Fleet> allFleetsInOrbit = null;
                if (fleet.getOrbit() != null && fleet.getMove() == null) {
                    final Planet location = fleet.getOrbit().getPlanet();
                    allFleetsInOrbit = viewBoxDefinition.getAllFleetsInOrbit(location);
                    if (!allFleetsInOrbit.isEmpty()) {
                        allFleetsInOrbit.remove(fleet);
                    }
                }

                MoveDTO moveDTO = null;
                if (fleet.getMove() != null) {
                    moveDTO = new MoveDTO(fleet, fleet.getMove().getTargetOrbit().getPlanet());
                    // reset fleet position is fleet is in motion - must be non-movable for the user
                    viewBoxDefinition.resetPositionOfSvgElement(element);
                }
                createOrUpdateFleetDialog(id, fleet, allFleetsInOrbit, moveDTO, element);
            }
        });
    }

    /**
     * Attaches the drag end listener to the canvas and defined their behavior.
     *
     * <b>Attention:</b> The drag end listener interferes heavily with the drag start listener - events has to be separated by user interaction.
     *
     * @param canvas the canvas to attach the listener
     * @return
     */
    private Registration addDragEndListener(Svg canvas) {
        return canvas.addDragEndListener(event -> {
            final SvgElement element = event.getElement();
            final String id = element.getId();
            if (id.startsWith(FLEET_SELECTOR_ID_PREFIX)) {
                final Fleet fleet = fleetMap.get(id);

                final Double elementX = event.getElementX();
                final Double elementY = event.getElementY();
                final Planet occupyingPlanet = viewBoxDefinition.getOccupyingPlanet(elementX, elementY);
                if (fleet.getMove() != null || (fleet.getOrbit() != null && occupyingPlanet != null && occupyingPlanet.equals(fleet.getOrbit().getPlanet()))) {
                    // do nothing if fleet is in move or set to it's current planetary orbit
                    return;
                }
                if (occupyingPlanet == null) {
                    // do not reset position of fleet shark
                    NotificationHelper.notify("May be you should move the fleet to a valid target", 3000);
                    return;
                }

                final MoveDTO moveDTO = new MoveDTO(fleet, occupyingPlanet);
                // no merge possible in other planetary orbits
                createOrUpdateFleetDialog(id, fleet, null, moveDTO, element);
            }
        });
    }

    /**
     * Creates or updates the fleet dialog which is used for simple fleet actions here.
     *
     * @param id               the css selector of the fleet shark
     * @param fleet            the fleet itself
     * @param allFleetsInOrbit the fleets in the present orbit of the fleet
     * @param moveDTO          the dto which represents a movement of a fleet, a possible or a existing one
     * @param element          the specific element which triggers the event chain
     */
    private void createOrUpdateFleetDialog(@Nonnull final String id,
                                           @Nonnull final Fleet fleet,
                                           @Nullable final Set<Fleet> allFleetsInOrbit,
                                           @Nullable final MoveDTO moveDTO,
                                           @Nonnull final SvgElement element) {
        Preconditions.checkNotNull(id, "id shouldn't be null!");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(element, "element shouldn't be null!");

        SBConfirmationDialog sbDialog = (SBConfirmationDialog) openDialogs.get(id);
        if (sbDialog != null) {
            ((FleetMoveMergeSplitEdit) sbDialog.getContent()).setValueFleetDisplay(fleet);
            ((FleetMoveMergeSplitEdit) sbDialog.getContent()).setValueMoveDisplay(moveDTO);
            ((FleetMoveMergeSplitEdit) sbDialog.getContent()).setValueFleetMergeEdit(allFleetsInOrbit);
            if (!sbDialog.isOpened()) {
                sbDialog.open(INITIAL_TOP_LEFT);
            }
        } else {
            final FleetMoveMergeSplitEdit fleetMoveMergeSplitEdit = new FleetMoveMergeSplitEdit();
            fleetMoveMergeSplitEdit.setValueFleetDisplay(fleet);
            fleetMoveMergeSplitEdit.setValueMoveDisplay(moveDTO);
            fleetMoveMergeSplitEdit.setValueFleetMergeEdit(allFleetsInOrbit);
            sbDialog = new SBConfirmationDialog(fleetMoveMergeSplitEdit);
            sbDialog.open(INITIAL_TOP_LEFT);
            openDialogs.put(id, sbDialog);

            Registration submitListener = sbDialog.addSubmitListener(submitEvent -> {
                final Set<Fleet> fleetsToMerge = fleetMoveMergeSplitEdit.getFleetsToMerge();
                // merge some fleets
                if (!fleetsToMerge.isEmpty()) {
                    final Fleet mergedFleet = fleetService.mergeFleets(fleet, fleetsToMerge);
                    // update display after merging fleets
                    fleetMoveMergeSplitEdit.setValueFleetDisplay(mergedFleet);
                    // update canvas and merge display after merging fleets
                    final StarSystem starSystem = starsystemService.find(getValue());
                    if (starSystem == null) {
                        throw new NotifySBUserException("You found an easter egg - good shot, buddy.");
                    }
                    final FleetOrbit fleetOrbit = fleet.getOrbit();
                    if (fleetOrbit != null) {
                        final Planet planet = fleetOrbit.getPlanet();
                        final Set<Fleet> allFleetsInOrbit2 = starSystem.getFleets().stream()
                                .filter(fleet1 -> fleet1.getOrbit() != null && planet.equals(fleet1.getOrbit().getPlanet()))
                                .collect(Collectors.toSet());

                        fleetMoveMergeSplitEdit.setValueFleetMergeEdit(allFleetsInOrbit2);
                        viewBoxDefinition.removeFleetSharksFromOrbits(fleetsToMerge);
                        NotificationHelper.notify("merge accepted", 3000);
                    }
                } else if (fleetMoveMergeSplitEdit.getSelectedMove() != null) {
                    final Planet target = fleetMoveMergeSplitEdit.getSelectedMove().getTarget();
                    final Fleet fleetInMotion = fleetService.moveFleet(fleet, target);

                    final MoveDTO moveDTO1 = new MoveDTO(fleetInMotion, target);
                    fleetMoveMergeSplitEdit.setValueMoveDisplay(moveDTO1);
                    Set<Fleet> fleetsToRemove = new HashSet<>();
                    fleetsToRemove.add(fleet);
                    viewBoxDefinition.removeFleetSharksFromOrbits(fleetsToRemove);
                    viewBoxDefinition.createMovingFleet(fleetInMotion);
                    NotificationHelper.notify("move accepted", 3000);

                    // remove fleet in motion from all constructed dialogs
                    openDialogs.values().forEach(sbDialog1 -> {
                        if (sbDialog1.getContent() instanceof FleetMoveMergeSplitEdit) {
                            final FleetMoveMergeSplitEdit content = (FleetMoveMergeSplitEdit) sbDialog1.getContent();
                            final Set<Fleet> fleetsToMerge1 = content.getFleetsToMerge();
                            fleetsToMerge1.remove(fleetInMotion);
                            content.setValueFleetMergeEdit(fleetsToMerge1);
                        }
                    });
                } else if (fleetMoveMergeSplitEdit.getFleetsToSplit() != null) {
                    final Fleet[] splitResult = fleetMoveMergeSplitEdit.getFleetsToSplit().getSplitResult();
                    final Fleet baseFleet = splitResult[0];
                    Fleet splitFleet = splitResult[1];
                    fleetService.saveAndFlush(baseFleet);
                    splitFleet = fleetService.saveAndFlush(splitFleet);
                    
                    fleetMap.put(ViewBoxDefinition.createFleetID(splitFleet), splitFleet);
                    viewBoxDefinition.createFleetPolygonInOrbit(splitFleet);
                }

                openDialogs.get(id).close();
                openDialogs.remove(id);
            });
            registrationList.add(submitListener);

            Registration cancelListener = sbDialog.addCancelListener(cancelEvent -> {
                openDialogs.get(id).close();
                openDialogs.remove(id);
                viewBoxDefinition.resetPositionOfSvgElement(element);
            });
            registrationList.add(cancelListener);

            final SBConfirmationDialog finalSbDialog = sbDialog;
            final Registration changeListener = fleetMoveMergeSplitEdit.addChangeListener(event1 -> {
                finalSbDialog.enableSubmitButton(fleetMoveMergeSplitEdit.isActionPossible());
            });
            registrationList.add(changeListener);
        }
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

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        removeRegisteredListeners();
    }

    /**
     * Removes all registered listeners from this and it's children component. As far as they are registered, obviously.
     */
    private void removeRegisteredListeners() {
        registrationList.forEach(Registration::remove);
    }
}
