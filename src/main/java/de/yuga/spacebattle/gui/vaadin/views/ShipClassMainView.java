package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClassComparator;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
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
import de.yuga.spacebattle.gui.vaadin.misc.PageWithSubjectActionTabsAndStats;
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
@CssImport("./styles/views/main/details/module-display.css")
@Route(value = ShipClassMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ShipClassMainView.ROUTE, layout = MainView.class)
public class ShipClassMainView extends PageWithSubjectActionTabsAndStats<ShipClass> {

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
        final User loggedIn = userService.getLoggedInUser();
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
        updateShipClassCreate();
    }

    /**
     * Retrieves the user's current research base for modules and hulls and sets them to them are needed.
     */
    private void updateShipClassCreate() {
        final User user = userService.getLoggedInUser();
        final List<Hull> allHullByUser = hullService.findAllByUser(user);
        final List<Armor> allArmorByUser = moduleService.findAllArmorByUser(user);
        final List<ElectronicWarfare> allElectronicWarfareByUser = moduleService.findAllElectronicWarfareByUser(user);
        final List<Propulsion> allPropulsionByUser = moduleService.findAllPropulsionByUser(user);
        final List<Sidewall> allSidewallByUser = moduleService.findAllSidewallByUser(user);
        final List<Weapon> allWeaponByUser = moduleService.findAllWeaponByUser(user);
        final List<AmmunitionModule> allAmmunitionModulesByUser = moduleService.findAllAmmunitionModulesByUser(user);
        final List<PassiveModule> allPassiveModuleByUser = moduleService.findAllPassiveModuleByUser(user);

        final ShipClassCreateDTO shipClassCreateDTO =
                new ShipClassCreateDTO(user,
                        allArmorByUser,
                        allElectronicWarfareByUser,
                        allPropulsionByUser,
                        allSidewallByUser,
                        allWeaponByUser,
                        allAmmunitionModulesByUser,
                        allPassiveModuleByUser,
                        allHullByUser);
        shipClassCreate.setValue(shipClassCreateDTO);
    }

    /**
     * Retrieves the user's current research base for modules and hulls and sets them to them are needed.
     *
     * @param shipClass the ship class to modify
     */
    private void updateShipClassEdit(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final User user = userService.getLoggedInUser();
        final List<Armor> allArmorByUser = moduleService.findAllArmorByUser(user);
        final List<ElectronicWarfare> allElectronicWarfareByUser = moduleService.findAllElectronicWarfareByUser(user);
        final List<Propulsion> allPropulsionByUser = moduleService.findAllPropulsionByUser(user);
        final List<Sidewall> allSidewallByUser = moduleService.findAllSidewallByUser(user);
        final List<Weapon> allWeaponByUser = moduleService.findAllWeaponByUser(user);
        final List<AmmunitionModule> allAmmunitionModulesByUser = moduleService.findAllAmmunitionModulesByUser(user);
        final List<PassiveModule> allPassiveModuleByUser = moduleService.findAllPassiveModuleByUser(user);

        final ShipClassEditDTO shipClassEditDTO = new ShipClassEditDTO(user,
                allArmorByUser,
                allElectronicWarfareByUser,
                allPropulsionByUser,
                allSidewallByUser,
                allWeaponByUser,
                allAmmunitionModulesByUser,
                allPassiveModuleByUser,
                shipClass);
        shipClassEdit.setValue(shipClassEditDTO);
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
            this.shipClass = shipClassService.save(shipClass);
            NotificationHelper.notify("Class defined", 3000);
            updateSubjectMenu();
            useSubjectEntry();
        } else if (e.getPayload().equals(ESBEvent.SHIP_CLASS_DELETION.name())) {
            final ShipClass shipClass = ((ShipClassEdit) content).getShipClass();
            shipClassService.delete(shipClass);
            this.shipClass = null;
            NotificationHelper.notify("Class deleted", 3000);
            shipClassCreate.updateStatistics(shipClass);
            subjectSelectorMenu.setSelectedIndex(0);
            final Tab createNewClassTab = getTabForComponentOfActionMenu(shipClassCreate);
            setSelected(createNewClassTab);
            content = setContent(shipClassCreate);
            updateSubjectMenu();
            useSubjectEntry();
        } else if (e.getPayload().equals(ESBEvent.TICK_DONE.name())) {
            updateShipClassCreate();
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
                    updateShipClassEdit(shipClass);
                }
            }
            if (componentForTab instanceof ShipClassDisplay || componentForTab instanceof ShipClassCreate) {
                componentForTab.updateStatistics(shipClass);
            }
            componentForTab.refresh();
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

        final User user = userService.getLoggedInUser();
        final List<ShipClass> allByOwner = shipClassService.findAllLatestByOwner(user);
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
            updateShipClassEdit(shipClass);
            shipClassDisplay.updateStatistics(shipClass);
        } else {
            // implement a subject of null is definitely the create view
            content = setContent(shipClassCreate);
        }
        final Tab currentTab = getTabForComponentOfActionMenu(content);

        final HashMap<Tab, Boolean[]> tabMap = getActionTabUsability(currentTab);
        updateActionMenuUsability(tabMap);

        shipClassCreate.updateStatistics(shipClass);
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
     */
    @Override
    protected void updateSubjectMenu() {
        final User user = userService.getLoggedInUser();
        final List<ShipClass> allByOwner = shipClassService.findAllLatestByOwner(user);

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
