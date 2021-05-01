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
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClassComparator;
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
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassCreateDTO;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassEditDTO;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.SBPageSubjectSelectorStatsLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Route(value = ShipClassMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ShipClassMainView.ROUTE, layout = MainView.class)
public class ShipClassMainView extends SBPageSubjectSelectorStatsLayout<ShipClass> {

    @Nonnull
    public static final String ROUTE = "shipClass";

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassMainView.class);

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
        createShipClassCreateDTO();
    }

    /**
     * Retrieves the user's current research base for modules and hulls and sets them to them are needed.
     */
    private void createShipClassCreateDTO() {
        final List<Hull> hulls = hullService.findAllByUser(user);
        final List<Module> modules = moduleService.findAllByUser(user);
        final Map<Module, Integer> availableModules = modules.stream().collect(Collectors.toMap(o -> o, o -> 0));

        final ShipClassCreateDTO shipClassCreateDTO = new ShipClassCreateDTO(user, availableModules, hulls);
        shipClassCreate.setValue(shipClassCreateDTO);
    }

    private ShipClassEditDTO createShipClassEditDTO(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final List<Module> modules = moduleService.findAllByUser(user);
        return new ShipClassEditDTO(user, modules, shipClass);
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
            } else if (content instanceof ShipClassEdit) {
                shipClass = shipClassEdit.getShipClass();
            }
            if (shipClass == null) {
                throw new NotifySBUserException("The ship class shouldn't be empty here.");
            }
            this.shipClass = shipClassService.saveAndFlush(shipClass);
            NotificationHelper.notify("Class defined", 3000);
            updateSubjectMenu();
            useSubjectEntry();
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
            useSubjectEntry();
        } else if (e.getPayload().equals(ESBEvent.TICK_DONE.name())) {
            createShipClassCreateDTO();
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
            final Tab selectedTab = event.getSelectedTab();
            final ShipClassLayout<ShipClass> componentForTab
                    = (ShipClassLayout<ShipClass>) getComponentForTabOfActionMenu(selectedTab);

            if (shipClass != null) {
                shipClass = shipClassService.find(shipClass);
            }

            if (shipClass != null) {
                if (componentForTab instanceof ShipClassEdit) {
                    shipClassEdit.setValue(createShipClassEditDTO(shipClass));
                }
            }
            if (componentForTab instanceof ShipClassDisplay || componentForTab instanceof ShipClassCreate) {
                componentForTab.update(shipClass);
            }
            content = setContent(componentForTab);
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
            useSubjectEntry();
        });
    }

    /**
     * Proceeds the workload to change menus and view components by the given entity.
     */
    private void useSubjectEntry() {
        if (shipClass != null) {
            if (content == shipClassCreate) {
                content = setContent(shipClassDisplay);
            }
            shipClassEdit.setValue(createShipClassEditDTO(shipClass));
            shipClassDisplay.update(shipClass);
        } else {
            // implement a subject of null is definitely the create view
            content = setContent(shipClassCreate);
        }
        final Tab currentTab = getTabForComponentOfActionMenu(content);

        final HashMap<Tab, Boolean[]> tabMap = getActionTabUsability(currentTab);
        updateActionMenuUsability(tabMap);

        shipClassCreate.update(shipClass);
        getTabForSubject(shipClass).ifPresent(this::setSelected);
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

        final boolean isCreateNew = CREATE_NEW_CLASS_ACTION_TITLE.equals(currentTab.getLabel());
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

        subjectSelectorMenu.setSelectedIndex(-1); // see above, removing active tabs is not a good idea
        shipClassTabMap.keySet().stream()
                .sorted(new ShipClassComparator())
                .forEach(shipClass1 -> {
                    final Tab tab = shipClassTabMap.get(shipClass1);
                    final int i = orderedKeys.indexOf(shipClass1);
                    subjectSelectorMenu.addComponentAtIndex(i, tab);
                });
    }
}
