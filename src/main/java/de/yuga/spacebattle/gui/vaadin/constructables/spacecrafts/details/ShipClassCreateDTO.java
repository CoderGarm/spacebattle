package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ShipClassCreateDTO extends ShipClassEditDTO {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassEditDTO.class);

    @Nonnull
    private final Collection<Hull> possibleHulls = new HashSet<>();

    public ShipClassCreateDTO(@Nonnull final User owner,
                              @Nonnull final Map<Module, Integer> modules,
                              @Nonnull final Collection<Hull> possibleHulls) {
        super(owner, modules);
        Preconditions.checkNotNull(possibleHulls, "possibleHulls shouldn't be null!");

        this.possibleHulls.addAll(possibleHulls);
    }

    @Nonnull
    public Collection<Hull> getPossibleHulls() {
        return possibleHulls;
    }

    public void setHull(@Nullable Hull hull) {
        super.setHull(hull);
    }

    public void setHulls(@Nullable final Collection<Hull> hulls) {
        if (hulls == null || hulls.isEmpty()) {
            LOGGER.info("hulls is empty");
            return;
        }
        hulls.stream().findFirst().ifPresent(this::setHull);
    }
}
