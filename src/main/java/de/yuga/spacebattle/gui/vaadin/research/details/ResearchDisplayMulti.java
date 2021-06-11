package de.yuga.spacebattle.gui.vaadin.research.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.HasNameAndDescription;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EEntityType;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vaadin component to display an amount of researches, ordered by their IDs.
 */
public class ResearchDisplayMulti extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchDisplayMulti, Map<Research, Integer>>, Map<Research, Integer>> {

    @Nonnull
    final private Grid<ResearchLevelDTO> grid = new Grid<>();

    public ResearchDisplayMulti() {
        grid.setHeightByRows(true);
        grid.setColumnReorderingAllowed(true);
        grid.addColumn(dto -> dto.getResearch().getName())
                .setComparator((o1, o2) -> {
                    // create sorting by distance to either universe center or the selected planet
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.getResearch().getName().compareTo(o2.getResearch().getName());
                })
                .setHeader("Research");

        grid.addColumn(ResearchLevelDTO::getLevel)
                .setComparator((o1, o2) -> {
                    // create sorting by distance to either universe center or the selected planet
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.getLevel().compareTo(o2.getLevel());
                })
                .setHeader("Level");

        grid.addColumn(dto -> dto.getResearch().getLevelCap())
                .setComparator((o1, o2) -> {
                    // create sorting by distance to either universe center or the selected planet
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return Integer.compare(o1.getResearch().getLevelCap(), o2.getResearch().getLevelCap());
                })
                .setHeader("Level cap");

        grid.addColumn(dto -> dto.getResearch().getDescription()).setHeader("Description");

        final ComponentRenderer<Component, ResearchLevelDTO> detailRenderer = new ComponentRenderer<>(dto -> {
            final Grid<HasNameAndDescription> inlineGrid = new Grid<>();
            inlineGrid.setColumnReorderingAllowed(true);
            inlineGrid.setHeightByRows(true);
            inlineGrid.addColumn(HasNameAndDescription::getName)
                    .setComparator((o1, o2) -> {
                        // create sorting by distance to either universe center or the selected planet
                        if (o1 == null && o2 == null) {
                            return 0;
                        }
                        if (o1 == null) {
                            return -1;
                        }
                        if (o2 == null) {
                            return 1;
                        }
                        return o1.getName().compareTo(o2.getName());
                    }).setHeader("Name");

            inlineGrid.addColumn(nad -> EEntityType.getTypeByClazz(nad.getClass()).getType())
                    .setComparator((o1, o2) -> {
                        // create sorting by distance to either universe center or the selected planet
                        if (o1 == null && o2 == null) {
                            return 0;
                        }
                        if (o1 == null) {
                            return -1;
                        }
                        if (o2 == null) {
                            return 1;
                        }
                        return o1.getName().compareTo(o2.getName());
                    }).setHeader("Type");

            inlineGrid.addColumn(HasNameAndDescription::getDescription).setHeader("Description");

            final Set<HasNameAndDescription> unlocks = dto.getResearch().getUnlocks();
            inlineGrid.setItems(unlocks);
            return inlineGrid;
        });
        grid.setItemDetailsRenderer(detailRenderer);

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        add(grid);
    }

    @Override
    public void setValue(@Nonnull final Map<Research, Integer> researches) {

        if (researches.isEmpty()) {
            grid.setItems(new HashSet<>());
            grid.getDataProvider().refreshAll();
            return;
        }
        final Set<ResearchLevelDTO> researchLevelDTOS = researches.entrySet().stream()
                .map(e -> new ResearchLevelDTO(e.getKey(), e.getValue())).collect(Collectors.toSet());
        grid.setItems(researchLevelDTOS);
        grid.getDataProvider().refreshAll();
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
