package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.PageWithActionTabsAndStats;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.research.ResearchDoneDisplay;
import de.yuga.spacebattle.gui.vaadin.research.ResearchLayout;
import de.yuga.spacebattle.gui.vaadin.research.ResearchSelectionEdit;
import de.yuga.spacebattle.gui.vaadin.research.ResearchTechTreeDisplay;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchEdit;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchLevelDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@SpringComponent
@UIScope
@Route(value = ResearchMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ResearchMainView.ROUTE, layout = MainView.class)
public class ResearchMainView extends PageWithActionTabsAndStats<User> {

    @Nonnull
    public static final String ROUTE = "researches";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private ResearchLayout<User> content;

    @Nonnull
    private final ResearchSelectionEdit researchSelectionEdit;

    @Nonnull
    private final ResearchDoneDisplay researchDoneDisplay;

    @Nonnull
    private final ResearchTechTreeDisplay researchTechTreeDisplay;

    @Autowired
    public ResearchMainView(@Nonnull final UserService userService,
                            @Nonnull final JobService jobService,
                            @Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.jobService = jobService;
        final User loggedIn = userService.getLoggedInUser();
        researchSelectionEdit = new ResearchSelectionEdit();
        researchDoneDisplay = new ResearchDoneDisplay();
        researchTechTreeDisplay = new ResearchTechTreeDisplay();
        createActionSelectorMenu();
        content = researchSelectionEdit;
        content.update(loggedIn);
        setContent(content);
        updateActionMenuUsability(null);
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.RESEARCH_JOB_START.name())) {
            final ResearchEdit researchEdit = (ResearchEdit) e.getSource();
            final ResearchLevelDTO researchLevelDTO = researchEdit.getValue();
            if (researchLevelDTO == null) {
                throw new NotifySBUserException("Something went wrong while communicate your research request. Call the admin.");
            }
            final User loggedInUser = userService.getLoggedInUser();
            jobService.createResearchJob(loggedInUser.getId(), researchLevelDTO.getResearch().getId());
        }
    }

    @Override
    protected void createActionSelectorMenu() {
        Tab availableResearches = new Tab("Available researches");
        addComponentForTabOfActionMenu(availableResearches, researchSelectionEdit);

        Tab completedResearches = new Tab("Completed researches");
        addComponentForTabOfActionMenu(completedResearches, researchDoneDisplay);

        Tab techTree = new Tab("Tech tree");
        addComponentForTabOfActionMenu(techTree, researchTechTreeDisplay);

        addActionListener();
    }

    @Override
    protected void addActionListener() {
        final User loggedInUser = userService.getLoggedInUser();
        actionSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            final StatsLayout<User> componentForTab = getComponentForTabOfActionMenu(selectedTab);
            componentForTab.update(loggedInUser);
            content = setContent((ResearchLayout<User>) componentForTab);
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap) {
    }
}
