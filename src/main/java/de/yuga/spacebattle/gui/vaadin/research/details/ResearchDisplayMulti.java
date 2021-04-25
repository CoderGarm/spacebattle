package de.yuga.spacebattle.gui.vaadin.research.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Vaadin component to display an amount of researches, ordered by their IDs.
 * todo: ID-ordering is primitive and will be obsolete in usability when there is a more-then-one-dimensional tech tree
 */
public class ResearchDisplayMulti extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchDisplayMulti, Map<Research, Integer>>, Map<Research, Integer>> {

    @Nonnull
    final private Map<Research, ResearchDisplay> componentMap = new HashMap<>();

    Label title = new Label("Research display multi");

    public ResearchDisplayMulti() {

        add(title);
    }

    @Override
    public void clear() {
        removeAll();
        componentMap.clear();
        add(title);
    }

    @Override
    public void setValue(@Nonnull final Map<Research, Integer> researches) {

        if (researches.isEmpty()) {
            clear();
            return;
        }

        componentMap.keySet().stream()
                .filter(research -> !researches.containsKey(research))
                .map(componentMap::get)
                .forEach(this::remove);

        componentMap.keySet().removeIf(research -> !researches.containsKey(research));

        researches.keySet().stream().sorted(Comparator.comparingInt(AbstractEntityKey::getId)).forEach(research -> {
            final Integer level = researches.get(research);
            ResearchDisplay researchDisplay = componentMap.get(research);
            if (researchDisplay == null) {
                researchDisplay = new ResearchDisplay();
                componentMap.put(research, researchDisplay);
                add(researchDisplay);
            }
            researchDisplay.setValue(new ResearchLevelDTO(research, level));
        });

    }

    @Override
    public Map<Research, Integer> getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchDisplayMulti, Map<Research, Integer>>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
