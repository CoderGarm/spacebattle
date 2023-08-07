package de.yuga.spacebattle.backend.services.constructables;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class OperationalService {

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final ConstructionService constructionService;

    @Autowired
    public OperationalService(@Nonnull final WarShipService warShipService,
                              @Nonnull final ConstructionService constructionService) {
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
    }

    @Nonnull
    public List<WarShip> getPendingWarShips(final int idUser) {
        return warShipService.findAliveInoperationalForUser(idUser);
    }

    @Nonnull
    public List<Construction> getPendingConstructions(final int idUser) {
        return constructionService.findInoperationalForUser(idUser);
    }
}
