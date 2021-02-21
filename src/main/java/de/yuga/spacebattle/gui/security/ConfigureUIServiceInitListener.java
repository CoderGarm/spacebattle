package de.yuga.spacebattle.gui.security;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import de.yuga.spacebattle.gui.vaadin.SBRouting;
import de.yuga.spacebattle.gui.vaadin.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.SBRouting.SB_ROUTING_ITEMS;


@Component
public class ConfigureUIServiceInitListener implements VaadinServiceInitListener {

    @Nonnull
    private final SecurityUtils securityUtils;

    @Autowired
    private ConfigureUIServiceInitListener(@Nonnull final SecurityUtils securityUtils) {
        Preconditions.checkNotNull(securityUtils, "securityUtils shouldn't be null!");

        this.securityUtils = securityUtils;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter);
        });
    }

    /**
     * Reroutes the user if (s)he is not authorized to access the view.
     *
     * @param event before navigation event with event details
     */
    private void beforeEnter(@Nonnull final BeforeEnterEvent event) {
        Preconditions.checkNotNull(event, "event shouldn't be null!");

        if (checkIfLoginNeeded(event.getNavigationTarget())
                && !securityUtils.isUserLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
    }

    /**
     * Checks if a login is needed to see this target.
     *
     * @param clazz the check target
     * @return <code>true</code> if an login is needed
     */
    public static boolean checkIfLoginNeeded(@Nonnull final Class<?> clazz) {
        Preconditions.checkNotNull(clazz, "clazz shouldn't be null!");

        return !Arrays.stream(SB_ROUTING_ITEMS).map(SBRouting::getClazz).collect(Collectors.toList()).contains(clazz);
    }
}