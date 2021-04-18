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
import de.yuga.spacebattle.gui.vaadin.misc.SBPageSubjectSelectorStatsLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Route(value = ShipClassMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ShipClassMainView.ROUTE, layout = MainView.class)
public class ShipClassMainView extends SBPageSubjectSelectorStatsLayout<ShipClass> {

    @Nonnull
    public static final String ROUTE = "shipClass";

    /**
     * Special stuff: This subject is null in the real world while it's tab is used without a nonnull-object.
     */
    @Nonnull
    private static final String CREATE_NEW_CLASS_SUBJECT_TITLE = "Create new Class";

    @Nonnull
    private static final String STATS_ACTION_TITLE = "Fitting data";

    @Nonnull
    private static final String MODIFY_ACTION_TITLE = "Modify fitting";

    @Nonnull
    private static final String CREATE_NEW_CLASS_ACTION_TITLE = "Create new fitting";

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
        subjectSelectorMenu.setAutoselect(false);
        createActionSelectorMenu();
        actionSelectorMenu.setAutoselect(false);
        content = shipClassCreate;
        setContent(content);
        final Tab actionTab = getTabForComponentOfActionMenu(content);
        final HashMap<Tab, Boolean[]> tabMap = getActionTabUsability(actionTab);
        updateActionMenuUsability(tabMap);
        setSelected(actionTab);
        fetchBaseData();
    }

    /**
     * Retrieves the user's current research base for modules and hulls and sets them to them are needed.
     */
    private void fetchBaseData() {
        List<Hull> hulls = hullService.findAllByUser(user);
        List<Module> modules = moduleService.findAllByUser(user);
        shipClassEdit.setBaseData(modules);
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
            shipClassCreate.update(shipClass);
            subjectSelectorMenu.setSelectedIndex(0);
            final Tab createNewClassTab = getTabForComponentOfActionMenu(shipClassCreate);
            setSelected(createNewClassTab);
            content = setContent(shipClassCreate);
            updateSubjectMenu();
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
        Tab tabShipClassesCreate = new Tab(CREATE_NEW_CLASS_ACTION_TITLE);
        tabShipClassesCreate.setVisible(false);
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
        });
    }

    @Override
    protected void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap) {
        if (readOnlyMap == null || readOnlyMap.isEmpty()) {
            return;
        }
        actionSelectorMenu.getChildren().forEach(menuItem -> {
            final Tab tab = (Tab) menuItem;
            final Boolean[] aBoolean = readOnlyMap.get(tab);
            if (aBoolean != null) {
                tab.setVisible(aBoolean[0]);
                tab.setEnabled(aBoolean[1]);
            }
        });
    }

    @Override
    protected void createSubjectSelectorMenu() {
        Tab createNewClass = new Tab(CREATE_NEW_CLASS_SUBJECT_TITLE);
        addSubjectForTabOfSubjectMenu(createNewClass, null);

        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        allByOwner.sort(new ShipClassComparator());
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
            if (selectedTab == null) {
                // strange thing with removing tabs from menu -> change event will be fired
                return;
            }
            shipClass = getSubjectForTabOfSubjectMenu(selectedTab);
            if (shipClass != null) {
                shipClass = shipClassService.find(shipClass);
            }
            useSubjectEntry(shipClass);
        });
    }

    /**
     * Proceeds the workload to change menus and view components by the given entity.
     *
     * @param shipClass the parameter
     */
    private void useSubjectEntry(@Nullable final ShipClass shipClass) {
        this.shipClass = shipClass;
        if (this.shipClass != null) {
            if (content == shipClassCreate) {
                content = setContent(shipClassDisplay);
            }
            content.update(this.shipClass);
        } else {
            // implement a subject of null is definitely the create view
            content = setContent(shipClassCreate);
        }
        final Tab currentTab = getTabForComponentOfActionMenu(content);

        final HashMap<Tab, Boolean[]> tabMap = getActionTabUsability(currentTab);
        updateActionMenuUsability(tabMap);

        content.update(this.shipClass);
        setSelected(currentTab);
    }

    /**
     * Returns the visibility and usage state for every component by the current tab.
     *
     * @param currentTab the current tab
     * @return a map which contains the states to their view component
     */
    @Nonnull
    private HashMap<Tab, Boolean[]> getActionTabUsability(@Nonnull final Tab currentTab) {
        Preconditions.checkNotNull(currentTab, "currentTab shouldn't be null!");

        boolean isCreateNew = CREATE_NEW_CLASS_ACTION_TITLE.equals(currentTab.getLabel());
        HashMap<Tab, Boolean[]> tabMap = new HashMap<>();
        tabMap.put(getTabForComponentOfActionMenu(shipClassDisplay), new Boolean[]{true, !isCreateNew});
        tabMap.put(getTabForComponentOfActionMenu(shipClassEdit), new Boolean[]{!isCreateNew, !isCreateNew});
        tabMap.put(getTabForComponentOfActionMenu(shipClassCreate), new Boolean[]{isCreateNew, isCreateNew});
        return tabMap;
    }

    /**
     * Updates the menu.
     * Removes the unnecessary menu items, adds the new items and order them.
     * <p>
     * todo rework because of ugly and redundant calls
     */
    @Override
    protected void updateSubjectMenu() {
        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        allByOwner.sort(new ShipClassComparator());

        final List<Tab> toRemove = new ArrayList<>();
        subjectSelectorMenu.getChildren().forEach(component -> {
            Tab tab = (Tab) component;
            if (CREATE_NEW_CLASS_SUBJECT_TITLE.equals(tab.getLabel())) {
                return;
            }
            ShipClass subject = getSubjectForTabOfSubjectMenu(tab);
            if (!allByOwner.contains(subject)) {
                toRemove.add(tab);
            }
        });
        toRemove.forEach(super::removeFromSubject); // no problem while setting another tab active

        allByOwner.stream()
                .filter(shipClass -> !subjectSelectorObject.containsValue(shipClass))
                .forEach(shipClassFE -> {
                    Tab shipClassFETab = new Tab(shipClassFE.getName());
                    addSubjectForTabOfSubjectMenu(shipClassFETab, shipClassFE);
                });

        final Map<ShipClass, Tab> shipClassTabMap = subjectSelectorMenu.getChildren()
                .collect(Collectors.toMap(component -> getSubjectForTabOfSubjectMenu((Tab) component), component -> (Tab) component));

        final List<ShipClass> orderedKeys = shipClassTabMap.keySet().stream()
                .sorted(new ShipClassComparator()).collect(Collectors.toList());

        final Tab selectedTab = subjectSelectorMenu.getSelectedTab();
        subjectSelectorMenu.setSelectedIndex(-1); // see above, removing active tabs is not a good idea
        shipClassTabMap.keySet().stream()
                .sorted(new ShipClassComparator())
                .forEach(shipClass1 -> {
                    final Tab tab = shipClassTabMap.get(shipClass1);
                    final int i = orderedKeys.indexOf(shipClass1);
                    subjectSelectorMenu.addComponentAtIndex(i, tab);
                });
        setSelected(selectedTab);
    }

    /**
     * Sorts the list of ship classes from biggest hull to smallest.
     */
    private static class ShipClassComparator implements Comparator<ShipClass> {

        @Override
        public int compare(ShipClass o1, ShipClass o2) {
            if (o1 == null || o2 == null || o1.getHull() == null || o2.getHull() == null) {
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
        }
    }
}
