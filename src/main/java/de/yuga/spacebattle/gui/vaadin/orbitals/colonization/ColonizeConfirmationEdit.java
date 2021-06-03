package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.EResourceAmountDTO;

import javax.annotation.Nonnull;

/**
 * It simply displays the costs of a colonization task and a button to do the action.
 */
public class ColonizeConfirmationEdit extends HorizontalLayout {

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final Binder<Planet> binder = new Binder<>();

    /**
     * The button will be enabled is there are enough credits to pay this.
     */
    @Nonnull
    private final Button submit;

    public ColonizeConfirmationEdit() {
        final User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new NotifySBUserException("You should hold a login here.");
        }

        final Label costs = new Label();
        final ReadOnlyHasValue<String> costsText = new ReadOnlyHasValue<>(costs::setText);
        binder.forField(costsText)
                .bind(planet -> {
                            final EResourceAmountDTO costsDTO = ColonizationCostCalculator.calculateColonizationCost(planet);
                            final EResourceType resourceType = costsDTO.getResourceType();
                            final String amountWithDiff = costsDTO.getAmountWithDiff();
                            return amountWithDiff + " " + resourceType.getPluralName();
                        },
                        null);

        submit = new Button("Colonize it");
        submit.setEnabled(false);

        add(costs, submit);
    }

    /**
     * Attaches the listener for the submit button.
     *
     * @param clickListener the listener to attach
     * @return the registration
     */
    public Registration addSubmitListener(@Nonnull final ComponentEventListener<ClickEvent<Button>> clickListener) {
        Preconditions.checkNotNull(clickListener, "clickListener shouldn't be null!");

        return submit.addClickListener(clickListener);
    }

    @Nonnull
    public Button getSubmitButton() {
        return submit;
    }

    public void setValue(Planet value) {
        binder.setBean(value);
        if (value != null) {
            final User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser != null) {
                if (loggedInUser.getOwnedPlanets().contains(value)) {
                    submit.setText("It's my, yeah");
                    return;
                }
                final Planet mainPlanet = loggedInUser.getMainPlanet();
                final EResourceAmountDTO costs = ColonizationCostCalculator.calculateColonizationCost(value);
                if (mainPlanet.getResourceDeposit().getResourceAmountByType(costs.getResourceType()).compareTo(costs.getAmount()) >= 0) {
                    submit.setEnabled(true);
                }
            }
        }
    }

    public Planet getValue() {
        return binder.getBean();
    }
}
