package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@CssImport("./styles/views/main/details/SBPageTopLevelLayout.css")
public abstract class SBPageTopLevelLayout<T> extends FlexLayout {

    private final static int INDEX_CONTENT = 2;

    @Nonnull
    private final StatsDrawer statsDrawer = new StatsDrawer();

    @Nonnull
    private Component content = new HorizontalLayout();

    @Nonnull
    public final MenuBar subjectSelectorMenu = new MenuBar();

    @Nonnull
    public final Tabs actionSelectorMenu = new Tabs();

    @Nonnull
    public final Map<Tab, StatsLayout<T>> actionSelectorPages = new HashMap<>();

    @Nonnull
    private final VerticalLayout mainContent = new VerticalLayout();

    public SBPageTopLevelLayout() {
        actionSelectorMenu.setId("actionSelectorMenu");
        actionSelectorMenu.setClassName("selector");
        actionSelectorMenu.setOrientation(Tabs.Orientation.HORIZONTAL);
        ViewHelper.setWidth(actionSelectorMenu, "100%");
        subjectSelectorMenu.setId("subjectSelectorMenu");
        subjectSelectorMenu.setClassName("selector");
        //subjectSelectorMenu.setOrientation(Tabs.Orientation.HORIZONTAL);
        ViewHelper.setWidth(subjectSelectorMenu, "100%");
        mainContent.add(subjectSelectorMenu);
        mainContent.add(actionSelectorMenu);

        setHeight("100%");
        setFlexDirection(FlexDirection.ROW);

        mainContent.addComponentAtIndex(INDEX_CONTENT, content);
        ViewHelper.setWidth(mainContent, "75%");
        ViewHelper.setWidth(statsDrawer, "20%");
        add(mainContent, statsDrawer);
    }

    private void setDrawer(Component content) {
        statsDrawer.update(content);
    }

    public <T extends StatsLayout> void setContent(T content) {
        mainContent.remove(this.content);
        this.content = (Component) content;
        ViewHelper.setWidth((HasSize) this.content, "80%");
        mainContent.addComponentAtIndex(INDEX_CONTENT, this.content);
        setDrawer(content.getStatisticsComponent());
    }

    public void addComponentForTab(@Nonnull final Tab tab, @Nonnull final StatsLayout<T> component) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        actionSelectorMenu.add(tab);
        actionSelectorPages.put(tab, component);
    }

    @Nonnull
    public StatsLayout<T> getComponentForTab(@Nonnull final Tab tab) {
        Preconditions.checkNotNull(tab, "tab shouldn't be null!");
        final StatsLayout<T> statsLayout = actionSelectorPages.get(tab);
        if (statsLayout == null) {
            throw new NotifySBUserException("You should talk to the administrator about that.");
        }
        return statsLayout;
    }

    protected abstract void createActionSelectorMenu();

    protected abstract void createSubjectSelectorMenu();

    protected abstract void updateActionMenuVisibility();

    protected abstract void updateMenus();
}
