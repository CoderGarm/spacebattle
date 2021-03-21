package de.yuga.spacebattle.gui.vaadin.misc;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;

import javax.annotation.Nonnull;

@CssImport("./styles/views/main/details/SBPageTopLevelLayout.css")
public abstract class SBPageTopLevelLayout extends FlexLayout {

    private final static int INDEX_CONTENT = 2;

    @Nonnull
    private final StatsDrawer statsDrawer = new StatsDrawer();

    @Nonnull
    private Component content = new HorizontalLayout();

    @Nonnull
    public final MenuBar subjectSelectorMenu = new MenuBar();

    @Nonnull
    public final MenuBar actionSelectorMenu = new MenuBar();

    @Nonnull
    private final VerticalLayout mainContent = new VerticalLayout();

    public SBPageTopLevelLayout() {
        actionSelectorMenu.setId("actionSelectorMenu");
        actionSelectorMenu.setClassName("selector");
        ViewHelper.setWidth(actionSelectorMenu, "100%");
        subjectSelectorMenu.setId("subjectSelectorMenu");
        subjectSelectorMenu.setClassName("selector");
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

    protected abstract void createActionSelectorMenu();

    protected abstract void createSubjectSelectorMenu();

    protected abstract void updateActionMenuVisibility();

    protected abstract void updateMenus();
}
