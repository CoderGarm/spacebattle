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
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceCostAmountDTO;

import javax.annotation.Nonnull;

/**
 * It simply displays the costs to buy star systems' data and a button to do the action.
 */
public class BuyColonizationDataConfirmationEdit extends HorizontalLayout {

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final PlanetService planetService = ViewHelper.getService(PlanetService.class);

    @Nonnull
    private final Binder<StarSystem> binder = new Binder<>();

    /**
     * The button will be enabled is there are enough credits to pay this.
     */
    @Nonnull
    private final Button submit;

    public BuyColonizationDataConfirmationEdit() {
        final Label costs = new Label();
        final ReadOnlyHasValue<String> costsText = new ReadOnlyHasValue<>(costs::setText);
        binder.forField(costsText)
                .bind(system -> {
                            final ResourceCostAmountDTO costsDTO = ColonizationCostCalculator.calculateInformationCost(system);
                            final EResourceType resourceType = costsDTO.getResourceType();
                            final String amountWithDiff = costsDTO.getAmountAsString();
                            return amountWithDiff + " " + resourceType.getPluralName();
                        },
                        null);

        submit = new Button("Buy system's information");
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

    public void setValue(StarSystem value) {
        binder.setBean(value);
        if (value != null) {
            final User loggedInUser = userService.getLoggedInUser();
            final Planet mainPlanet = planetService.findMainPlanet(loggedInUser);
            final ResourceCostAmountDTO costs = ColonizationCostCalculator.calculateInformationCost(value);
            if (mainPlanet.getResourceDeposit().isReducingResourcePossible(costs.getResourceType(), costs.getAmount())) {
                submit.setEnabled(true);
            }

        }
    }

    public StarSystem getValue() {
        return binder.getBean();
    }
}
