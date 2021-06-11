package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.misc.details.SimpleValueChangeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Should allow a filtration between different constructions.
 */
public class ConstructionFilterEdit extends HorizontalLayout {

    @Nonnull
    private final ProductCategorySelect productCategorySelect = new ProductCategorySelect();

    @Nonnull
    private final ResourceTypeSelect resourceTypeSelect = new ResourceTypeSelect();

    @Nullable
    private HasValue.ValueChangeListener<SimpleValueChangeEvent> valueChangeListener;

    public ConstructionFilterEdit() {
        // todo tooltip doesn't opened again on re-attached component: https://github.com/vaadin-component-factory/tooltip/issues/13
        productCategorySelect.setEmptySelectionAllowed(true);
        productCategorySelect.setItems(EProductionCategory.values());

        resourceTypeSelect.setEmptySelectionAllowed(true);
        resourceTypeSelect.setItems(EResourceType.values());

        productCategorySelect.addValueChangeListener(event -> {
            if (valueChangeListener == null) {
                return;
            }
            valueChangeListener.valueChanged(new SimpleValueChangeEvent());
        });
        resourceTypeSelect.addValueChangeListener(event -> {
            if (valueChangeListener == null) {
                return;
            }
            valueChangeListener.valueChanged(new SimpleValueChangeEvent());
        });
        add(resourceTypeSelect, productCategorySelect);
    }

    public Registration addValueChangeListener(@Nonnull final HasValue.ValueChangeListener<SimpleValueChangeEvent> listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        valueChangeListener = listener;
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
    }

    public ConstructionFilterDTO getValue() {
        final EResourceType resourceType = resourceTypeSelect.getValue();
        final EProductionCategory productionCategory = productCategorySelect.getValue();
        return new ConstructionFilterDTO(productionCategory, resourceType);
    }
}
