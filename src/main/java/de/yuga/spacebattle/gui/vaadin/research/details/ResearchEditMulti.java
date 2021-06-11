package de.yuga.spacebattle.gui.vaadin.research.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Vaadin component to group multiple {@link ResearchEdit} and to start a particular research {@link Job}.
 */
public class ResearchEditMulti extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchEditMulti, Map<Research, Integer>>, Map<Research, Integer>> {

    @Nonnull
    final private Map<Research, ResearchEdit> componentMap = new HashMap<>();

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final PlanetService planetService = ViewHelper.getService(PlanetService.class);

    Label title = new Label("Research edit multi");

    public ResearchEditMulti() {

        add(title);
        uiEventBus.subscribe(this);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.RESEARCH_JOB_FEEDBACK_STARTED.name())) {
            componentMap.values().forEach(researchEdit -> researchEdit.setReadOnly(true));
        }
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

        final User loggedIn = userService.getLoggedInUser();
        final Planet researchPlanet = planetService.findResearchPlanet(loggedIn);
        if (researchPlanet != null) {
            Construction facility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH);
            if (facility == null) {
                throw new NotifySBUserException("You can't research without a lab.");
            }
            componentMap.keySet().stream()
                    .filter(research -> !researches.containsKey(research))
                    .map(componentMap::get)
                    .forEach(this::remove);

            componentMap.keySet().removeIf(research -> !researches.containsKey(research));

            researches.forEach((research, level) -> {
                final ResearchEdit researchEdit;
                if (!componentMap.containsKey(research)) {
                    researchEdit = new ResearchEdit();
                    componentMap.put(research, researchEdit);
                    researchEdit.addValueChangeListener(event -> uiEventBus.publish(researchEdit, ESBEvent.RESEARCH_JOB_START.name()));
                    add(researchEdit);
                } else {
                    researchEdit = componentMap.get(research);
                }
                researchEdit.setValue(new ResearchLevelDTO(research, level));
                researchEdit.setReadOnly(!facility.getJobs().isEmpty());
            });
        }
    }

    @Override
    public Map<Research, Integer> getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchEditMulti, Map<Research, Integer>>> listener) {
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
