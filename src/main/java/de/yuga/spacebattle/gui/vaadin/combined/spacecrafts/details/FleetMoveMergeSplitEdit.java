package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit.FleetSplitDTO;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit.FleetSplitEdit;
import de.yuga.spacebattle.gui.vaadin.misc.SBDialog;
import de.yuga.spacebattle.gui.vaadin.misc.details.SBValueChangeEvent;
import de.yuga.spacebattle.gui.vaadin.turn.action.MoveDTO;
import de.yuga.spacebattle.gui.vaadin.turn.action.MoveEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This view consolidates all relevant displays and edits for simple fleet actions.
 */
@CssImport("./styles/views/main/details/fleet-multi.css")
public class FleetMoveMergeSplitEdit extends VerticalLayout implements BeforeLeaveObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FleetMoveMergeSplitEdit.class);

    @Nonnull
    private final FleetDisplay fleetDisplay = new FleetDisplay();

    @Nonnull
    private final MoveEdit moveEdit = new MoveEdit();

    @Nonnull
    private final Details fleetMoveDetails = new Details();

    @Nonnull
    private final FleetMergeEdit fleetMergeEdit = new FleetMergeEdit();

    @Nonnull
    private final Details fleetMergeDetails = new Details();

    @Nonnull
    private final FleetSplitEdit fleetSplitEdit = new FleetSplitEdit();

    @Nonnull
    private final Details fleetSplitDetails = new Details();

    @Nonnull
    private final List<SBDialog> openDialogs = new ArrayList<>();

    /**
     * Holds every registered listener for this and it's children component. But you have to register them manually.
     * Every {@link Registration} will be removed if it's part of this list and the component will be left.
     */
    @Nonnull
    private final List<Registration> registrationList = new ArrayList<>();

    @Nonnull
    private final Div active = new Div();

    @Nonnull
    private final Div inactive = new Div();

    public FleetMoveMergeSplitEdit() {

        active.setClassName("active");
        inactive.setClassName("inactive");

        fleetMergeDetails.setSummaryText("Merge fleet");
        fleetMergeDetails.setEnabled(false);
        fleetMergeDetails.setId("merge-detail");
        fleetMergeDetails.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

        fleetSplitDetails.setSummaryText("Split fleet");
        fleetSplitDetails.setEnabled(false);
        fleetSplitDetails.setId("split-detail");
        fleetSplitDetails.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

        fleetMoveDetails.setSummaryText("Movement");
        fleetMoveDetails.setEnabled(false);
        fleetMoveDetails.setId("move-detail");
        fleetMoveDetails.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

        add(fleetDisplay, active, inactive);
    }

    /**
     * Detects if a @Route-ed page has left and fires the {@link BeforeLeaveEvent}.
     *
     * @param event the event
     */
    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        closeDialogs();
    }

    /**
     * Closes and deletes all dialogs.
     */
    public void closeDialogs() {
        openDialogs.forEach(SBDialog::close);
        openDialogs.clear();
        registrationList.forEach(Registration::remove);
    }

    /**
     * Just returns the payload.
     *
     * @return the payload
     */
    @Nullable
    public Fleet getDisplayedFleet() {
        return fleetDisplay.getValue();
    }

    /**
     * Just returns the payload.
     *
     * @return the payload
     */
    @Nullable
    public MoveDTO getSelectedMove() {
        return moveEdit.getValue();
    }

    /**
     * Just returns the payload.
     *
     * @return the payload
     */
    @Nonnull
    public Set<Fleet> getFleetsToMerge() {
        Set<Fleet> value = fleetMergeEdit.getValue();
        if (value == null) {
            value = new HashSet<>();
        }
        return value;
    }

    @Nullable
    public FleetSplitDTO getFleetsToSplit() {
        return fleetSplitEdit.getValue();
    }

    /**
     * Calculates the state of the views in order of the defined values of their content.
     */
    private void calculateState() {

        final Fleet fleetDisplayValue = fleetDisplay.getValue();
        final MoveDTO moveDTO = moveEdit.getValue();
        final Set<Fleet> mergeFleets = fleetMergeEdit.getValue();

        inactivate(fleetSplitDetails);

        if (fleetDisplayValue != null) {
            boolean fleetInMotion = moveDTO != null && moveDTO.isInMotion();
            boolean shipsPresent = !fleetDisplayValue.getShips().isEmpty();
            boolean active = shipsPresent && !fleetInMotion;
            if (active) {
                activate(fleetSplitDetails, fleetSplitEdit);
            }
        }

        if (moveDTO != null) {
            fleetMoveDetails.setOpened(true);
            activate(fleetMoveDetails, moveEdit);
        } else {
            inactivate(fleetMoveDetails);
        }

        if (mergeFleets != null && !mergeFleets.isEmpty()) {
            activate(fleetMergeDetails, fleetMergeEdit);
        } else {
            inactivate(fleetMergeDetails);
        }
    }

    /**
     * Sets a view and it's detail to usable and viewable for the user.
     *
     * @param details the details
     * @param content the content
     */
    private void activate(@Nonnull final Details details, @Nonnull final Component content) {
        Preconditions.checkNotNull(details, "details shouldn't be null!");
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        details.setEnabled(true);
        details.setContent(content);
        this.active.add(details);
    }

    /**
     * Sets the detail in a not-usable state for the user.
     *
     * @param details the details
     */
    private void inactivate(@Nonnull final Details details) {
        Preconditions.checkNotNull(details, "details shouldn't be null!");

        details.setEnabled(false);
        details.setContent(null);
        details.setOpened(false);
        this.inactive.add(details);
    }

    /**
     * Sets the value to the view and calculates the display state of this component.
     *
     * @param value the value
     */
    public void setValueMoveDisplay(@Nullable final MoveDTO value) {
        moveEdit.setValue(value);
        calculateState();
    }

    /**
     * Sets the value to the view and calculates the display state of this component.
     *
     * @param value the value
     */
    public void setValueFleetMergeEdit(@Nullable final Set<Fleet> value) {
        fleetMergeEdit.setValue(value);
        calculateState();
    }

    /**
     * Sets the value to the view and calculates the display state of this component.
     *
     * @param value the value
     */
    public void setValueFleetDisplayAndSplit(@Nullable final Fleet value) {
        fleetDisplay.setValue(value);
        fleetSplitEdit.setValue(value);
        calculateState();
    }

    /**
     * Checks if this view could accuse an action, e.g. to check if a submit button should be enabled or not.
     *
     * @return <code>true</code> if an action is possible, <code>false</code> otherwise
     */
    public boolean isActionPossible() {
        if (fleetMergeEdit.getValue() != null && !fleetMergeEdit.getValue().isEmpty()) {
            return true;
        }

        final MoveDTO moveDTO = moveEdit.getValue();
        if (moveDTO != null) {
            boolean inMotion = moveDTO.isInMotion();
            boolean cancelFlight = moveDTO.isCancelFlight();
            if (!inMotion || cancelFlight) {
                return true;
            }
        }

        return fleetSplitEdit.isValid();
    }

    /**
     * Adds listeners to all changeable components and sends a {@link SBValueChangeEvent} if something was clicked.
     *
     * @param listener the listener to add
     * @return the registration to remove the listener
     */
    public Registration addChangeListener(@Nonnull final ValueChangeListener<SBValueChangeEvent> listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        final Registration r1 = moveEdit.addChangeListener(listener);
        final Registration r2 = fleetMergeEdit.addChangeListener(listener);
        final Registration r3 = fleetSplitEdit.addValueChangeListener(listener);
        final Registration combine = Registration.combine(r1, r2, r3);
        registrationList.add(combine);
        return combine;
    }
}
