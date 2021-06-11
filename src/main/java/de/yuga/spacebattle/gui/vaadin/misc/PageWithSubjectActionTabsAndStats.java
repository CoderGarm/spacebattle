package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;
import de.yuga.spacebattle.gui.vaadin.views.PlanetMainView;
import de.yuga.spacebattle.gui.vaadin.views.ShipClassMainView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main layout for pages with a statistics section.
 * Every usages must extending an abstract class which is implementing {@link StatsLayout< GenericSubject >}.
 * <p>
 * This will define the structure to automatically use a statistics panel,
 * a subject selector menu for ever usable entity and
 * an action selector menu to do something with this entity.
 * <p>
 * Usage examples are {@link ShipClassMainView} and {@link PlanetMainView}.
 * <p>
 * There are several methods which should be used and overridden.
 * <p>
 * In a perfect world all these methods should be self-explanatory, but if not, submit a pull request.
 *
 * @param <GenericSubject>
 */
@CssImport("./styles/views/main/details/SBPageTopLevelLayout.css")
public abstract class PageWithSubjectActionTabsAndStats<GenericSubject> extends SplitLayout {

    /**
     * The index of the "do user stuff" section of the view below the two selector menus.
     */
    private final static int INDEX_CONTENT = 2;

    /**
     * The component which displays the statistics component.
     */
    @Nonnull
    private final StatsDrawer statsDrawer = new StatsDrawer();

    /**
     * The "do user stuff" section. Next to it there are the menus and the statistics.
     */
    @Nonnull
    private Component content = new HorizontalLayout();

    /**
     * The menu which holds every element which the user could work with.
     */
    @Nonnull
    public final Tabs subjectSelectorMenu = new Tabs();

    /**
     * This map holds the specific object to their corresponding selector tab.
     */
    @Nonnull
    public final Map<Tab, GenericSubject> subjectSelectorObject = new HashMap<>();

    /**
     * The "what the user want to do with the object" menu.
     */
    @Nonnull
    public final Tabs actionSelectorMenu = new Tabs();

    /**
     * This map holds the specific component to their corresponding action tab.
     */
    @Nonnull
    public final Map<Tab, StatsLayout<GenericSubject>> actionSelectorPages = new HashMap<>();

    /**
     * The canvas which displays all the stuff above.
     */
    @Nonnull
    private final VerticalLayout mainContent = new VerticalLayout();

    public PageWithSubjectActionTabsAndStats() {
        subjectSelectorMenu.setId("subjectSelectorMenu");
        subjectSelectorMenu.addClassName("selector");
        mainContent.add(subjectSelectorMenu);
        actionSelectorMenu.setId("actionSelectorMenu");
        actionSelectorMenu.addClassName("selector");
        mainContent.add(actionSelectorMenu);

        mainContent.addComponentAtIndex(INDEX_CONTENT, content);
        setOrientation(Orientation.HORIZONTAL);
        addToPrimary(mainContent);
        addToSecondary(statsDrawer);
        setSplitterPosition(80);
        addThemeVariants(SplitLayoutVariant.LUMO_MINIMAL);
        setHeight("100%");
    }

    /**
     * Sets the statistics section to the statistics canvas.
     *
     * @param content the component to set
     */
    private void setDrawer(@Nullable final StatisticsDisplay content) {
        statsDrawer.setValue(content);
    }

    /**
     * Sets the full content to display including the statistics section.
     *
     * @param content         the content to set
     * @param <GenericLayout> the type definition which must be used
     */
    public <GenericLayout extends StatsLayout<GenericSubject>> GenericLayout setContent(@Nonnull final GenericLayout content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        mainContent.remove(this.content);
        this.content = (Component) content;
        mainContent.addComponentAtIndex(INDEX_CONTENT, this.content);
        setDrawer(content.getStatisticsComponent());
        return content;
    }

    /**
     * Sets and relate a selector tab to it's corresponding component.
     * Will add a new tab or simply update a component for a known tab.
     *
     * @param tab       the selector's tab
     * @param component the tab's component
     */
    public void addComponentForTabOfActionMenu(@Nonnull final Tab tab, @Nonnull final StatsLayout<GenericSubject> component) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        final Component knownTab = actionSelectorMenu.getChildren().filter(tab::equals).findFirst().orElse(null);
        if (knownTab == null) {
            actionSelectorMenu.add(tab);
        }
        actionSelectorPages.put(tab, component);
    }

    /**
     * Returns the corresponding component for the given tab.
     *
     * @param tab the tab to search for
     * @return the corresponding component
     */
    @Nonnull
    public StatsLayout<GenericSubject> getComponentForTabOfActionMenu(@Nonnull final Tab tab) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");
        final StatsLayout<GenericSubject> statsLayout = actionSelectorPages.get(tab);
        if (statsLayout == null) {
            throw new NotifySBUserException("You should talk to the administrator about that.");
        }
        return statsLayout;
    }

    /**
     * Returns a Tab for a given subject.
     *
     * @param subject the subject
     * @return Optional of a tab
     */
    public Optional<Tab> getTabForSubject(@Nullable GenericSubject subject) {
        return subjectSelectorObject.entrySet().stream()
                .filter(e -> {
                    if (e.getValue() == null && subject == null) {
                        return true;
                    }
                    return e.getValue() != null && e.getValue().equals(subject);
                })
                .findFirst()
                .map(Map.Entry::getKey);
    }

    /**
     * Returns the tab for it's corresponding component.
     * Via versa for {@link PageWithSubjectActionTabsAndStats#getComponentForTabOfActionMenu(Tab)}.
     *
     * @param component the component to search for
     * @return the corresponding tab
     */
    @Nonnull
    public Tab getTabForComponentOfActionMenu(@Nonnull final StatsLayout<GenericSubject> component) {
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        Tab tab = actionSelectorPages.keySet().stream().filter(tabA -> actionSelectorPages.get(tabA) == component).findFirst().orElse(null);
        if (tab == null) {
            throw new NotifySBUserException("If there is a component, there must be a tab. Say this to the admin!");
        }
        return tab;
    }

    /**
     * Sets and relate a selector tab to it's corresponding object.
     * Will add a new tab or simply update a object for a known tab.
     *
     * @param tab     the selector's tab
     * @param subject the tab's object
     */
    public void addSubjectForTabOfSubjectMenu(@Nonnull final Tab tab, @Nullable final GenericSubject subject) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");

        final Component knownTab = subjectSelectorMenu.getChildren().filter(tab::equals).findFirst().orElse(null);
        if (knownTab == null) {
            subjectSelectorMenu.add(tab);
        }
        subjectSelectorObject.put(tab, subject);
    }

    /**
     * Returns the object for it's corresponding tab.
     *
     * @param tab the tab to search for
     * @return the tab's object
     */
    @Nullable
    public GenericSubject getSubjectForTabOfSubjectMenu(@Nonnull final Tab tab) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null while searching!");

        return subjectSelectorObject.get(tab);
    }

    /**
     * Removes a subject's tab and it's object.
     * <p>
     * Take care of the fact, that removing a tab will call the listener for the next index -
     * and if there is no tab, than the event will called for a empty tab.
     *
     * @param tab the tab to remove.
     */
    public void removeFromSubject(@Nonnull final Tab tab) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null while removing!");

        subjectSelectorMenu.remove(tab);
        subjectSelectorObject.remove(tab);
    }

    /**
     * Select only the tab which is presented by the parameter.
     * Every other tab of the tab's menu will deselected.
     *
     * @param tab the tab which should be selected
     */
    public void setSelected(@Nonnull final Tab tab) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");

        setSelectedIndexForMenu(tab, actionSelectorMenu);
        setSelectedIndexForMenu(tab, subjectSelectorMenu);
    }

    /**
     * Sets the given task as selected in it's own menu.
     *
     * @param tab  the tab to select
     * @param tabs the menu to crawl
     */
    private void setSelectedIndexForMenu(@Nonnull Tab tab, Tabs tabs) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");
        Preconditions.checkNotNull(tabs, "tabs shouldn't be null!");

        Set<Tab> subjectTabs = tabs.getChildren().map(Tab.class::cast).collect(Collectors.toSet());
        if (subjectTabs.contains(tab)) {
            int i = tabs.indexOf(tab);
            tabs.setSelectedIndex(i);
        }
    }

    /**
     * Must define all action selectors menu entries and their behavior, {@link PageWithSubjectActionTabsAndStats#addActionListener()}.
     */
    protected abstract void createActionSelectorMenu();

    /**
     * Must define all initial subject selectors menu entries and their behavior, {@link PageWithSubjectActionTabsAndStats#addSubjectListener()}.
     */
    protected abstract void createSubjectSelectorMenu();

    /**
     * Must define the usability of the action tab.
     * Leave blank if no change of action selector's tabs is needed.
     * <p>
     * Else:
     * Boolean[0] - setVisible
     * Boolean[1] - setEnabled
     */
    protected abstract void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap);

    /**
     * Must update the subject selector's menu entries by removing or adding new tab-object combinations.
     */
    protected abstract void updateSubjectMenu();

    /**
     * Must define the behavior of <code>every</code> subject tab.
     * <p>
     * Hint #1: fetch <T> on every call.
     * Hint #2: must only be called once for the full menu.
     */
    protected abstract void addSubjectListener();

    /**
     * Must define the behavior of <code>every</code> action tab.
     * <p>
     * Hint #1: fetch <T> on every call.
     * Hint #2: must only be called once for the full menu.
     */
    protected abstract void addActionListener();
}
