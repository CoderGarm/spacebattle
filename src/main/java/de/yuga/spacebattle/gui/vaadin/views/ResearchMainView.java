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
import de.yuga.spacebattle.gui.vaadin.misc.SBPageActionSelectorStatsLayout;
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
public class ResearchMainView extends SBPageActionSelectorStatsLayout<User> {

    @Nonnull
    public static final String ROUTE = "researches";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private User user;

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
        User loggedIn = userService.getLoggedInUser();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;
        researchSelectionEdit = new ResearchSelectionEdit();
        researchDoneDisplay = new ResearchDoneDisplay();
        researchTechTreeDisplay = new ResearchTechTreeDisplay();
        createActionSelectorMenu();
        content = researchSelectionEdit;
        update(user);
        setContent(content);
        updateActionMenuUsability(null);
    }

    /**
     * Updates every view component with the current logged in user.
     *
     * @param user the user
     */
    private void update(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        researchSelectionEdit.update(user);
        researchDoneDisplay.update(user);
        researchTechTreeDisplay.update(user);
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
            jobService.createResearchJob(user.getId(), researchLevelDTO.getResearch().getId());
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
        actionSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            final StatsLayout<User> componentForTab = getComponentForTabOfActionMenu(selectedTab);
            user = userService.find(user).orElseThrow(NotifySBUserException::new);
            update(user);
            componentForTab.update(user);
            content = setContent((ResearchLayout<User>) componentForTab);
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap) {
    }
}
