package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseModuleSelectorDisplay extends VerticalLayout {

    @Nonnull
    private final Binder<BaseModule> baseModuleBinder = new Binder<>(BaseModule.class);

    public BaseModuleSelectorDisplay() {

        HorizontalLayout payload = new HorizontalLayout();
        final Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        baseModuleBinder.forField(nameReadOnly).bind(BaseModule::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> descriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        baseModuleBinder.forField(descriptionReadOnly).bind(BaseModule::getDescription, null);

        final Label constructionCapacity = new Label();
        final ReadOnlyHasValue<String> constructionCapacityReadOnly = new ReadOnlyHasValue<>(constructionCapacity::setText);
        baseModuleBinder.forField(constructionCapacityReadOnly).bind(baseModule -> "Construction capacity: " + baseModule.getUseCapacity(), null);

        payload.add(description, constructionCapacity);
        add(name, payload);
    }

    /**
     * Will update the display or clear all fields.
     *
     * @param baseModule the baseModule to display
     */
    public void update(@Nullable final BaseModule baseModule) {

        baseModuleBinder.readBean(baseModule);
    }
}
