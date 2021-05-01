package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit.FleetSplitDTO;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit.FleetSplitEdit;
import de.yuga.spacebattle.gui.vaadin.misc.SBDialog;
import de.yuga.spacebattle.gui.vaadin.turn.action.MoveDTO;
import de.yuga.spacebattle.gui.vaadin.turn.action.MoveDisplay;
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
public class FleetMoveMergeSplitEdit extends VerticalLayout implements BeforeLeaveObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FleetMoveMergeSplitEdit.class);

    @Nonnull
    private final FleetDisplay fleetDisplay = new FleetDisplay();

    @Nonnull
    private final MoveDisplay moveDisplay = new MoveDisplay();

    @Nonnull
    private final FleetMergeEdit fleetMergeEdit = new FleetMergeEdit();

    @Nonnull
    private final FleetSplitEdit fleetSplitEdit = new FleetSplitEdit();

    @Nonnull
    private final Button splitFleet;

    @Nonnull
    private final List<SBDialog> openDialogs = new ArrayList<>();

    /**
     * Holds every registered listener for this and it's children component. But you have to register them manually.
     * Every {@link Registration} will be removed if it's part of this list and the component will be left.
     */
    @Nonnull
    private final List<Registration> registrationList = new ArrayList<>();

    public FleetMoveMergeSplitEdit() {
        splitFleet = new Button("Split fleet", event -> {
            final Fleet displayedFleet = getDisplayedFleet();
            if (displayedFleet == null) {
                throw new NotifySBUserException("nice hack, buddy!");
            }
            setValueFleetSplit(displayedFleet);
        });
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

    @Nullable
    public Fleet getDisplayedFleet() {
        return fleetDisplay.getValue();
    }

    @Nullable
    public MoveDTO getSelectedMove() {
        return moveDisplay.getValue();
    }

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

    public void setValueFleetDisplay(@Nullable final Fleet value) {
        fleetDisplay.setValue(value);

        if (value != null) {
            boolean fleetInMotion = moveDisplay.getValue() != null && moveDisplay.getValue().isInMotion();
            boolean shipsPresent = !value.getShips().isEmpty();
            splitFleet.setEnabled(shipsPresent && !fleetInMotion);
        }

        boolean isAnchored = getChildren().anyMatch(fleetDisplay::equals);
        if (value != null && !isAnchored) {
            add(fleetDisplay, splitFleet);
        } else if (value == null && isAnchored) {
            remove(fleetDisplay, splitFleet);
        }
    }

    public void setValueFleetSplit(@Nonnull final Fleet value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        if (moveDisplay.getValue() != null && moveDisplay.getValue().isInMotion()) {
            return;
        }

        if (getChildren().anyMatch(splitFleet::equals)) {
            remove(splitFleet);
        }
        if (getChildren().anyMatch(moveDisplay::equals)) {
            remove(moveDisplay);
        }
        if (getChildren().anyMatch(fleetMergeEdit::equals)) {
            remove(fleetMergeEdit);
        }
        fleetSplitEdit.setValue(value);
        add(fleetSplitEdit);
    }

    public void setValueMoveDisplay(@Nullable final MoveDTO value) {
        moveDisplay.setValue(value);

        boolean isAnchored = getChildren().anyMatch(moveDisplay::equals);
        if (value != null && !isAnchored) {
            // remove split button if fleet is in motion
            if (moveDisplay.getValue() != null && moveDisplay.getValue().isInMotion() && getChildren().anyMatch(splitFleet::equals)) {
                remove(splitFleet);
            }
            add(moveDisplay);
        } else if (value == null && isAnchored) {
            remove(moveDisplay);
        }
    }

    public void setValueFleetMergeEdit(@Nullable final Set<Fleet> value) {
        fleetMergeEdit.setValue(value);

        boolean isAnchored = getChildren().anyMatch(fleetMergeEdit::equals);
        if ((value != null && !value.isEmpty()) && !isAnchored) {
            add(fleetMergeEdit);
        } else if ((value == null || value.isEmpty()) && isAnchored) {
            remove(fleetMergeEdit);
        }
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
        if (moveDisplay.getValue() != null && !moveDisplay.getValue().isInMotion()) {
            return true;
        }

        if (fleetSplitEdit.isValid()) {
            return true;
        }

        return false;
    }

    public Registration addChangeListener(@Nonnull final ValueChangeListener listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        final Registration r1 = moveDisplay.addChangeListener(listener);
        final Registration r2 = fleetMergeEdit.addChangeListener(listener);
        final Registration r3 = fleetSplitEdit.addValueChangeListener(listener);
        final Registration combine = Registration.combine(r1, r2, r3);
        registrationList.add(combine);
        return combine;
    }
}
