package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.contextmenu.MenuItem;
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
import de.yuga.spacebattle.gui.vaadin.misc.SBPageTopLevelLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
@Route(value = ShipClassMainView.ROUTE, layout = MainView.class)
@RouteAlias(value = ShipClassMainView.ROUTE, layout = MainView.class)
public class ShipClassMainView extends SBPageTopLevelLayout {

    @Nonnull
    public static final String ROUTE = "shipClass";

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassMainView.class);

    private static final int INDEX_OF_SUBJECT_MENU_BAR = 0;

    @Nonnull
    private static final String CREATE_NEW_CLASS_SUBJECT_TITLE = "Create new Class";

    @Nonnull
    private static final String STATS_ACTION_TITLE = "Stats";

    @Nonnull
    private static final String MODIFY_ACTION_TITLE = "Modify";

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
    private ShipClassLayout content = new ShipClassLayout();

    @Nonnull
    private final List<Hull> hulls;

    @Nonnull
    private final List<Module> modules;

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
        User loggedIn = userService.isLoggedIn();
        if (loggedIn == null) {
            throw new NotifySBUserException("You shouldn't see this.");
        }
        this.user = loggedIn;

        shipClassDisplay = new ShipClassDisplay();
        shipClassEdit = new ShipClassEdit();
        shipClassCreate = new ShipClassCreate();
        createSubjectSelectorMenu();
        createActionSelectorMenu();
        setContent(content);
        updateActionMenuVisibility();
        hulls = hullService.findAllByUser(this.user);
        modules = moduleService.findAllByUser(this.user);
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
            updateMenus();
        } else if (e.getPayload().equals(ESBEvent.SHIP_CLASS_DELETION.name())) {
            ShipClass shipClass = ((ShipClassEdit) content).getShipClass();
            shipClassService.delete(shipClass);
            this.shipClass = null;
            NotificationHelper.notify("Class deleted", 3000);
            updateMenus();
            shipClassCreate.update(hulls, modules);
            setContent(shipClassCreate);
        }
    }

    /**
     * Removes the already defined components and sets the given {@link ShipClassLayout} as content and
     * it's stats display as stats display.
     *
     * @param content the component to set
     */
    protected void setContent(@Nonnull final ShipClassLayout content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        this.content = content;
        this.content.setWidth("100%");
        super.setContent(this.content);
    }

    @Override
    protected void createActionSelectorMenu() {
        // Stats
        actionSelectorMenu.addItem(STATS_ACTION_TITLE, event -> {
            shipClassDisplay.update(shipClass);
            setContent(shipClassDisplay);
        });
        // ShipClasses
        actionSelectorMenu.addItem(MODIFY_ACTION_TITLE, event -> {
            final ShipClassLayout content;
            if (shipClass == null) {
                shipClassCreate.update(hulls, modules);
                content = shipClassCreate;
            } else {
                shipClassEdit.update(shipClass, modules);
                content = shipClassEdit;
            }
            setContent(content);
        });
    }

    @Override
    protected void updateActionMenuVisibility() {
        actionSelectorMenu.setVisible(true);
        actionSelectorMenu.getItems().forEach(menuItem -> menuItem.setEnabled(shipClass != null || visibleFlag));
    }

    @Override
    protected void createSubjectSelectorMenu() {
        subjectSelectorMenu.addItem(CREATE_NEW_CLASS_SUBJECT_TITLE, event -> {
            visibleFlag = false;
            shipClass = null;
            shipClassCreate.update(hulls, modules);
            setContent(shipClassCreate);
            updateActionMenuVisibility();
        });

        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        sort(allByOwner);
        allByOwner.forEach(shipClassFE -> {
            subjectSelectorMenu.addItem(shipClassFE.getName(), event -> {
                useSubjectEntry(shipClassFE);
            });
        });
    }

    private void useSubjectEntry(ShipClass shipClass) {
        this.shipClass = shipClass;
        shipClassEdit.update(shipClass, modules);
        shipClassDisplay.update(shipClass);
        updateActionMenuVisibility();
    }

    /**
     * Updates the menu.
     * Reattach the menu, removes the unnecessary menu items, adds the new items and reattach the current content.
     */
    @Override
    protected void updateMenus() {
        final List<ShipClass> allByOwner = shipClassService.findAllByOwner(user);
        sort(allByOwner);

        final List<String> shipNames = allByOwner.stream()
                .map(ShipClass::getName)
                .collect(Collectors.toList());

        subjectSelectorMenu.getItems().forEach(menuItem -> {
            final String itemTitle = menuItem.getText();
            if (!CREATE_NEW_CLASS_SUBJECT_TITLE.equals(itemTitle) && !shipNames.contains(itemTitle)) {
                subjectSelectorMenu.remove(menuItem);
            }
        });
        final List<String> itemTitles = subjectSelectorMenu.getItems().stream()
                .map(MenuItem::getText)
                .collect(Collectors.toList());

        allByOwner.stream().filter(shipClass -> !itemTitles.contains(shipClass.getName())).forEach(shipClassFE -> {
            subjectSelectorMenu.addItem(shipClassFE.getName(), event -> {
                useSubjectEntry(shipClassFE);
            });
        });

        remove(subjectSelectorMenu);
        addComponentAtIndex(INDEX_OF_SUBJECT_MENU_BAR, subjectSelectorMenu);
        ShipClassLayout content = new ShipClassLayout();
        if (shipClass != null) {
            shipClassEdit.update(shipClass, modules);
            shipClassDisplay.update(shipClass);

            if (this.content instanceof ShipClassCreate) {
                content = shipClassCreate;
            } else if (this.content instanceof ShipClassEdit) {
                content = shipClassDisplay;
            }
        }
        setContent(content);
        updateActionMenuVisibility();
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
