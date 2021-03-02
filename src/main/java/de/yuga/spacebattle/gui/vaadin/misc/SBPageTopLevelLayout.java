package de.yuga.spacebattle.gui.vaadin.misc;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;

import javax.annotation.Nonnull;

public abstract class SBPageTopLevelLayout extends HorizontalLayout {

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
        actionSelectorMenu.setWidth("100%");

        subjectSelectorMenu.setId("subjectSelectorMenu");
        subjectSelectorMenu.setWidth("100%");

        mainContent.add(this.subjectSelectorMenu);
        mainContent.add(this.actionSelectorMenu);
        mainContent.setMaxWidth("80%");
        mainContent.setWidth("80%");
        setHeight("100%");
        mainContent.addComponentAtIndex(INDEX_CONTENT, content);
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
