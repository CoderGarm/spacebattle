package de.yuga.spacebattle.gui.vaadin.account.details;

import com.vaadin.flow.component.select.Select;
import de.yuga.spacebattle.backend.enums.ERaceType;

import javax.annotation.Nullable;

public class RaceSelect extends Select<ERaceType> {

    public RaceSelect(@Nullable final ERaceType raceType) {
        this();
        setValue(raceType);
    }

    public RaceSelect() {
        setItems(ERaceType.values());
        setLabel("Species");
        setEmptySelectionAllowed(false);
    }
}
