package de.yuga.spacebattle.gui.vaadin.misc;

import com.vaadin.flow.component.Component;

public interface StatsLayout<T> {

    Component getStatisticsComponent();

    void update(T value);
}
