package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import de.yuga.spacebattle.gui.vaadin.views.*;

import javax.annotation.Nonnull;

public class SBRouting {

    /**
     * Main routing definition for menu building and navigation.
     */
    @Nonnull
    public final static SBRouting[] SB_ROUTING_ITEMS =
            new SBRouting[]{
                    new SBRouting(false, "Login", LoginView.class, "Login"),
                    new SBRouting(false, "Info", InfoView.class, "Info"),
                    new SBRouting(true, "Dashboard", DashboardView.class, "Dashboard"),
                    new SBRouting(true, "User", UserView.class, "User"),
                    new SBRouting(true, "Planets", PlanetMainView.class, "Planets"),
                    new SBRouting(true, "Ship classes", ShipClassMainView.class, "Ship classes"),
                    new SBRouting(true, "Researches", ResearchMainView.class, "Researches"),
                    new SBRouting(true, "Fleets", FleetMainView.class, "Fleets"),
                    new SBRouting(true, "Star map", StarMapMainView.class, "Star map"),
            };

    private final boolean loginNeeded;

    @Nonnull
    private final String navText;

    @Nonnull
    private final Class<? extends Component> clazz;

    @Nonnull
    private final String pageName;

    public SBRouting(final boolean loginNeeded,
                     @Nonnull final String navText,
                     @Nonnull final Class<? extends Component> clazz,
                     @Nonnull final String pageName) {
        Preconditions.checkNotNull(navText, "navText shouldn't be null!");
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");
        Preconditions.checkNotNull(pageName, "pageName shouldn't be null!");

        this.loginNeeded = loginNeeded;
        this.navText = navText;
        this.clazz = clazz;
        this.pageName = pageName;
    }

    public boolean isLoginNeeded() {
        return loginNeeded;
    }

    @Nonnull
    public String getNavText() {
        return navText;
    }

    @Nonnull
    public Class<? extends Component> getClazz() {
        return clazz;
    }

    @Nonnull
    public String getPageName() {
        return pageName;
    }
}
