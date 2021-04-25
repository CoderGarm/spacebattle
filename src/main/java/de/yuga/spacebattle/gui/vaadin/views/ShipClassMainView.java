package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassCreate;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassDisplay;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassEdit;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassLayout;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.SBPageSubjectSelectorLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringComponent
@UIScope
@Route(value = ShipClassMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ShipClassMainView.ROUTE, layout = MainView.class)
public class ShipClassMainView extends SBPageSubjectSelectorLayout<ShipClass> {

    @Nonnull
    public static final String ROUTE = "shipClass";

    @Nonnull
    private static final String CREATE_NEW_CLASS_SUBJECT_TITLE = "Create new Class";

    @Nonnull
    private static final String STATS_ACTION_TITLE = "Class data";

    @Nonnull
    private static final String MODIFY_ACTION_TITLE = "Modify class";

    @Nonnull
    private static final String CREATE_ACTION_TITLE = "Create new class";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final User user;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final HullService hullService;

    @Nonnull
    private final ModuleService moduleService;

    @Nullable
    private ShipClass shipClass;

    @Nonnull
    private ShipClassLayout<ShipClass> content;

    @Nonnull
    private final ShipClassDisplay shipClassDisplay;

    @Nonnull
    private final ShipClassEdit shipClassEdit;

    @Nonnull
    private final ShipClassCreate shipClassCreate;

    /**
     * If this flag is true the action menu bar is visible.
     */
    private boolean visibleFlag;

    @Autowired
    public ShipClassMainView(@Nonnull final UserService userService,
                             @Nonnull final ShipClassService shipClassService,
                             @Nonnull final EventBus.UIEventBus uiEventBus,
                             @Nonnull final HullService hullService,
                             @Nonnull final ModuleService moduleService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userService = userService;
        this.shipClassService = shipClassService;
        this.hullService = hullService;
        this.moduleService = moduleService;
        User loggedIn = userService.getLoggedInUser();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;

        shipClassDisplay = new ShipClassDisplay();
        shipClassEdit = new ShipClassEdit();
        shipClassCreate = new ShipClassCreate();
        createSubjectSelectorMenu();
        createActionSelectorMenu();
        content = shipClassDisplay;
        setContent(content);
        updateActionMenuUsability(null);
        fetchBaseData();
    }

    /**
     * Retrieves the user's current research base for modules and hulls and sets them to them are needed.
     */
    private void fetchBaseData() {
        List<Hull> hulls = hullService.findAllByUser(user);
        List<Module> modules = moduleService.findAllByUser(user);
        shipClassEdit.setModules(modules);
        shipClassCreate.setBaseData(hulls, modules);
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.SHIP_CLASS_SUBMITTED.name())) {
            ShipClass shipClass = null;
            if (content instanceof ShipClassCreate) {
                shipClass = shipClassCreate.getShipClass();
                shipClass.setOwner(user);
            } else if (content instanceof ShipClassEdit) {
                shipClass = shipClassEdit.getShipClass();
            }
            if (shipClass == null) {
                throw new NotifySBUserException("The ship class shouldn't be empty here.");
            }
            this.shipClass = shipClassService.save(shipClass);
            NotificationHelper.notify("Class defined", 3000);
            updateSubjectMenu();
        } else if (e.getPayload().equals(ESBEvent.SHIP_CLASS_DELETION.name())) {
            final ShipClass shipClass = ((ShipClassEdit) content).getShipClass();
            shipClassService.delete(shipClass);
            this.shipClass = null;
            NotificationHelper.notify("Class deleted", 3000);
            updateSubjectMenu();
            shipClassCreate.update(shipClass);
            content = setContent(shipClassCreate);
        } else if (e.getPayload().equals(ESBEvent.TICK_DONE.name())) {
            fetchBaseData();
        }
    }

    @Override
    protected void createActionSelectorMenu() {
        // Stats
        Tab tabStats = new Tab(STATS_ACTION_TITLE);
        addComponentForTabOfActionMenu(tabStats, shipClassDisplay);
        // Modify ShipClasses
        Tab tabShipClasses = new Tab(MODIFY_ACTION_TITLE);
        addComponentForTabOfActionMenu(tabShipClasses, shipClassEdit);
        // Create ShipClasses
        Tab tabShipClassesCreate = new Tab(CREATE_ACTION_TITLE);
        tabShipClassesCreate.setVisible(false); // todo ugly hack to hide create tab at
        addComponentForTabOfActionMenu(tabShipClassesCreate, shipClassCreate);

        addActionListener();
    }

    @Override
    protected void addActionListener() {
        actionSelectorMenu.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            StatsLayout<ShipClass> componentForTab = getComponentForTabOfActionMenu(selectedTab);
            if (shipClass != null) {
                shipClass = shipClassService.find(shipClass);
            }
            componentForTab.update(shipClass);
            content = setContent((ShipClassLayout<ShipClass>) componentForTab);
            subjectSelectorMenu.getSelectedTab().setSelected(true);
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean> readOnlyMap) {
        actionSelectorMenu.getChildren().forEach(menuItem -> {
            final Tab tab = (Tab) menuItem;

            if (readOnlyMap != null && readOnlyMap.containsKey(tab)) {
                Boolean aBoolean = readOnlyMap.get(tab);
                tab.setVisible(aBoolean);
            }

            boolean enabled = shipClass != null || visibleFlag;
            tab.setEnabled(enabled);
        });
    }

    @Override
    protected void createSubjectSelectorMenu() {
        Tab createNewClass = new Tab(CREATE_NEW_CLASS_SUBJECT_TITLE);
        addSubjectForTabOfSubjectMenu(createNewClass, null);

        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        sort(allByOwner);
        allByOwner.forEach(shipClassFE -> {
            Tab shipClassFETab = new Tab(shipClassFE.getName());
            addSubjectForTabOfSubjectMenu(shipClassFETab, shipClassFE);
        });
        addSubjectListener();
    }

    @Override
    protected void addSubjectListener() {
        subjectSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            shipClass = getSubjectForTabOfSubjectMenu(selectedTab);
            if (shipClass != null) {
                shipClass = shipClassService.find(shipClass);
            }
            useSubjectEntry(shipClass);
        });
    }

    private void useSubjectEntry(@Nullable final ShipClass shipClass) {
        visibleFlag = shipClass != null;
        this.shipClass = shipClass;
        if (this.shipClass == null) {
            content = setContent(shipClassCreate);
        } else {
            this.shipClass = shipClassService.find(this.shipClass);
            shipClassEdit.update(this.shipClass);
            shipClassDisplay.update(this.shipClass);
            if (content == shipClassCreate) {
                content = setContent(shipClassDisplay);
            }
        }
        final Tab currentTab = getTabForComponentOfActionMenu(content);
        currentTab.setSelected(true);

        boolean isCreateNew = CREATE_ACTION_TITLE.equals(currentTab.getLabel());
        HashMap<Tab, Boolean> tabMap = new HashMap<>();
        tabMap.put(getTabForComponentOfActionMenu(shipClassEdit), !isCreateNew);
        tabMap.put(getTabForComponentOfActionMenu(shipClassCreate), isCreateNew);

        content.update(this.shipClass);
        updateActionMenuUsability(tabMap);
    }

    /**
     * Updates the menu.
     * Removes the unnecessary menu items, adds the new items.
     */
    @Override
    protected void updateSubjectMenu() {
        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        sort(allByOwner);

        subjectSelectorMenu.getChildren().forEach(component -> {
            Tab tab = (Tab) component;
            if (CREATE_NEW_CLASS_SUBJECT_TITLE.equals(tab.getLabel())) {
                return;
            }
            ShipClass subject = getSubjectForTabOfSubjectMenu(tab);
            if (!allByOwner.contains(subject)) {
                removeFromSubject(tab);
            }
        });

        allByOwner.stream().filter(shipClass -> !subjectSelectorObject.containsValue(shipClass)).forEach(shipClassFE -> {
            Tab shipClassFETab = new Tab(shipClassFE.getName());
            addSubjectForTabOfSubjectMenu(shipClassFETab, shipClassFE);
        });

        addSubjectListener();
    }

    /**
     * Sorts the list of ship classes from biggest hull to smallest.
     *
     * @param allByOwner the list to sort
     */
    private void sort(List<ShipClass> allByOwner) {
        allByOwner.sort((o1, o2) -> {
            if (o1.getHull() == null || o2.getHull() == null) {
                return 1;
            }
            if (o1.getHull().getConstructionCapacity() < o2.getHull().getConstructionCapacity()) {
                return 1;
            } else if (o1.getHull().getConstructionCapacity() == o2.getHull().getConstructionCapacity()) {
                return 0;
            } else if (o1.getHull().getConstructionCapacity() > o2.getHull().getConstructionCapacity()) {
                return -1;
            }
            return 0;
        });
    }
}
