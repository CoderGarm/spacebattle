package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;

import javax.annotation.Nonnull;
import java.util.Set;

public class ConstructionEditMulti extends VerticalLayout {

    public ConstructionEditMulti(@Nonnull final Set<Construction> constructions) {
        Preconditions.checkNotNull(constructions, "constructions shouldn't be null!");

        constructions.forEach(construction -> {
            add(new ConstructionEdit(construction));
        });
    }
}
