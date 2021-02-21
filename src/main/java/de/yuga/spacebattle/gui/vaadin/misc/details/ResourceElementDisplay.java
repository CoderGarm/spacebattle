package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;

public class ResourceElementDisplay extends VerticalLayout {

    @Nonnull
    private final Binder<EResourceType> binderTitle = new Binder<>(EResourceType.class);

    @Nonnull
    private final Binder<ResourceAmountWrapper> binderAmount = new Binder<>(ResourceAmountWrapper.class);

    public ResourceElementDisplay() {

        final Label titleDisplay = new Label();
        final ReadOnlyHasValue<String> titleDisplayText = new ReadOnlyHasValue<>(titleDisplay::setText);
        binderTitle.forField(titleDisplayText).bind(EResourceType::getSingularName, null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binderAmount.forField(amountDisplayText).bind(ResourceAmountWrapper::getAmountWithDiff, null);

        add(titleDisplay, amountDisplay);
    }

    public void updateTitle(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        binderTitle.readBean(resourceType);
    }

    public void updateAmount(@Nonnull final ResourceAmountWrapper amount) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        binderAmount.readBean(amount);
    }
}
