package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TradeTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public TradeTickRunner(@Nonnull final MarketplaceService marketplaceService,
                           @Nonnull final PlanetService planetService) {
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "marketplaceService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Proceed trades");
        tickTrades();
    }


    private void tickTrades() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final List<TradedResource> trades = marketplaceService.findAllUnfinishedTrades();
        final Set<TradedResource> arrivals = trades.stream().filter(t -> t.getTicksLeft() == 1).collect(Collectors.toSet());

        final Map<Integer, Planet> toStore = new HashMap<>();
        for (final TradedResource tradedResource : arrivals) {
            tradedResource.setFinished(today);
            transferTradedPayload(toStore, tradedResource);
            transferTradePayment(toStore, tradedResource);
        }
        planetService.saveAll(toStore.values());

        trades.stream().filter(t -> t.getTicksLeft() > 0).forEach(TradedResource::tick);
        marketplaceService.save(trades);
    }

    private void transferTradePayment(@Nonnull final Map<Integer, Planet> toStore, @Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        Planet origin = tradedResource.getTradeOffer().getOrigin();
        origin = toStore.getOrDefault(origin.getId(), origin);
        origin.getResourceDeposit().updateResource(EResourceType.CREDITS, tradedResource.getFullPrice());
        toStore.put(origin.getId(), origin);
    }

    private void transferTradedPayload(@Nonnull final Map<Integer, Planet> toStore, @Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        Planet destination = tradedResource.getDestination();
        destination = toStore.getOrDefault(destination.getId(), destination);
        destination.getResourceDeposit().updateResource(tradedResource.getTradeOffer().getResourceType(), tradedResource.getTradedAmount());
        toStore.put(destination.getId(), destination);
    }
}
