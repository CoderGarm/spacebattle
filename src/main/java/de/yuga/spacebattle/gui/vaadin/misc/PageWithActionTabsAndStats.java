package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;
import de.yuga.spacebattle.gui.vaadin.views.ResearchMainView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Main layout for pages with a statistics section.
 * Every usages must extending an abstract class which is implementing {@link StatsLayout<GenericSubject>}.
 * <p>
 * This will define the structure to automatically use a statistics panel,
 * a subject selector menu for ever usable entity and
 * an action selector menu to do something with this entity.
 * <p>
 * Usage examples are {@link ResearchMainView}.
 * <p>
 * There are several methods which should be used and overridden.
 * <p>
 * In a perfect world all these methods should be self-explanatory, but if not, submit a pull request.
 *
 * @param <GenericSubject>
 */
public abstract class PageWithActionTabsAndStats<GenericSubject> extends SplitLayout {

    /**
     * The index of the "do user stuff" section of the view below the two selector menus.
     */
    private final static int INDEX_CONTENT = 1;

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

    public PageWithActionTabsAndStats() {
        actionSelectorMenu.setId("actionSelectorMenu");
        actionSelectorMenu.addClassName("selector");
        mainContent.add(actionSelectorMenu);

        mainContent.addComponentAtIndex(INDEX_CONTENT, content);
        setOrientation(SplitLayout.Orientation.HORIZONTAL);
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
     * Returns the tab for it's corresponding component.
     * Via versa for {@link PageWithActionTabsAndStats#getComponentForTabOfActionMenu(Tab)}.
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
     * Must define all action selectors menu entries and their behavior, {@link PageWithActionTabsAndStats#addActionListener()}.
     */
    protected abstract void createActionSelectorMenu();

    /**
     * Must define the usability of the action tab.
     * Leave blank if no disabling of action selector's tabs is needed.
     */
    protected abstract void updateActionMenuUsability(@Nullable final Map<Tab, Boolean[]> readOnlyMap);

    /**
     * Must define the behavior of <code>every</code> action tab.
     * Hint: fetch <T> on every call.
     */
    protected abstract void addActionListener();
}
