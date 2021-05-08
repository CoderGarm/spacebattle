package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class StarSystemLayout extends VerticalLayout {

    /**
     * Must refresh the SVG canvas while this is obviously not stored in the view.
     */
    public abstract void refresh();
}
