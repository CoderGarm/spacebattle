package de.yuga.spacebattle.backend.services.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.resource.trade.TradeOfferRepository;
import de.yuga.spacebattle.backend.repositories.turn.resource.trade.TradedResourceRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TradedResourceRepository tradedResourceRepository;

    @Nonnull
    private final TradeOfferRepository tradeOfferRepository;

    @Autowired
    public MarketplaceService(@Nonnull final UserService userService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final TradedResourceRepository tradedResourceRepository,
                              @Nonnull final TradeOfferRepository tradeOfferRepository) {
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.tradedResourceRepository = Preconditions.checkNotNull(tradedResourceRepository, "tradedResourceRepository shouldn't be null!");
        this.tradeOfferRepository = Preconditions.checkNotNull(tradeOfferRepository, "tradeOfferRepository must not be empty");
    }

    @Nonnull
    public List<TradedResource> findForTicks(@Nonnull final List<Tick> timeframe) {
        Preconditions.checkNotNull(timeframe, "timeframe must not be empty");

        final List<Integer> ticks = timeframe.stream().map(Tick::getNo).collect(Collectors.toList());
        return Objects.requireNonNullElse(tradedResourceRepository.findForTicks(ticks), new ArrayList<>());
    }

    @Nonnull
    public List<TradedResource> findFinishedAndPendingTradesForUser(@Nonnull final Tick tick, final int idUser) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return Objects.requireNonNullElse(tradedResourceRepository.findFinishedAndPendingTradesForUser(tick.getNo(), idUser), new ArrayList<>());
    }


    @Nonnull
    public TradedResource takeOffer(@Nonnull final Tick today, final int idTradeOffer, final int idBuyer, final int idPlanetDestination) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final TradeOffer tradeOffer = tradeOfferRepository.findById(idTradeOffer).orElseThrow(() -> new NotifyWebUserException("Hell no! You can only take an existing offer!"));

        final User seller = tradeOffer.getSeller();
        final User buyer = userService.find(idBuyer);
        Preconditions.checkNotNull(buyer, "buyer must not be empty");

        if (buyer.equals(seller)) {
            throw new NotifyWebUserException("Yeah but no. You can't sell stuff to yourself.");
        }

        final Planet destination = planetService.find(idPlanetDestination);
        if ((destination == null || destination.getOwner() == null) || !destination.getOwner().equals(buyer)) {
            throw new NotifyWebUserException("In the interests of good bookkeeping, you should leave it alone.");
        }

        final TradedResource tradedResource = checkAndAllocatePayment(today, tradeOffer, buyer, destination);
        tradeOffer.delete();
        tradeOfferRepository.save(tradeOffer);
        return tradedResource;
    }

    @Nonnull
    private TradedResource checkAndAllocatePayment(@Nonnull final Tick today, @Nonnull final TradeOffer tradeOffer, @Nonnull final User buyer, @Nonnull final Planet destination) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(tradeOffer, "tradeOffer must not be empty");
        Preconditions.checkNotNull(buyer, "buyer must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final long price = tradeOffer.getPrice();
        final PayingPossibleResult payingPossible = destination.getResourceDeposit().isPayingPossible(EResourceType.CREDITS, price);
        if (!payingPossible.isValid()) {
            throw new NotifyWebUserException("Not enough credits to take the offer.", payingPossible);
        }

        // the destination pays the bill
        destination.getResourceDeposit().pay(EResourceType.CREDITS, price);
        planetService.save(destination);

        final TradedResource tradedResource = new TradedResource(today, 10, tradeOffer, buyer, destination);
        return tradedResourceRepository.save(tradedResource);
    }

    @Nonnull
    public List<TradedResource> findAllUnfinishedTrades() {
        return Objects.requireNonNullElse(tradedResourceRepository.findAllUnfinished(), List.of());
    }

    public void save(@Nonnull final Collection<TradedResource> trades) {
        Preconditions.checkNotNull(trades, "trades must not be empty");

        tradedResourceRepository.saveAll(trades);
    }

    @Nonnull
    public List<TradeOffer> findActiveOffers() {
        return Objects.requireNonNullElse(tradeOfferRepository.findActiveOffers(), List.of());
    }

    @Nonnull
    public TradeOffer createOffer(@Nonnull final Tick initialTick,
                                  final int idSeller,
                                  final int idPlanetOrigin,
                                  @Nonnull final EResourceType resourceType,
                                  final long amount,
                                  final long price) {
        Preconditions.checkNotNull(initialTick, "initialTick must not be empty");
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        final User seller = userService.find(idSeller);
        final Planet origin = planetService.find(idPlanetOrigin);
        if ((origin == null || origin.getOwner() == null) || !origin.getOwner().equals(seller)) {
            throw new NotifyWebUserException("In the interests of good bookkeeping, you should leave it alone.");
        }

        return checkAndAllocateOfferPayload(initialTick, seller, origin, resourceType, amount, price);
    }

    @Nonnull
    private TradeOffer checkAndAllocateOfferPayload(@Nonnull final Tick initialTick,
                                                    @Nonnull final User seller,
                                                    @Nonnull final Planet origin,
                                                    @Nonnull final EResourceType resourceType,
                                                    final long amount,
                                                    final long price) {
        Preconditions.checkNotNull(initialTick, "initialTick must not be empty");
        Preconditions.checkNotNull(seller, "seller must not be empty");
        Preconditions.checkNotNull(origin, "origin must not be empty");

        final PayingPossibleResult payingPossible = origin.getResourceDeposit().isPayingPossible(resourceType, amount);
        if (!payingPossible.isValid()) {
            throw new NotifyWebUserException("Not enough resources for trade.", payingPossible);
        }

        // payload is persisted in the offer and 'allocated'
        origin.getResourceDeposit().pay(resourceType, amount);
        planetService.save(origin);

        return tradeOfferRepository.save(new TradeOffer(initialTick, seller, origin, resourceType, amount, price));
    }

    @Nonnull
    public TradeOffer updateOffer(@Nonnull final Tick today, final int idTradeOffer, final long newOfferAmount, final long price) {
        Preconditions.checkNotNull(today, "today must not be empty");

        final TradeOffer offer = tradeOfferRepository.findById(idTradeOffer).orElse(null);
        Preconditions.checkNotNull(offer, "offer must not be empty");

        final Planet origin = offer.getOrigin();
        final EResourceType resourceType = offer.getResourceType();
        final long oldOfferAmount = offer.getAmount();
        final long difference = newOfferAmount - oldOfferAmount;
        // payload is persisted in the offer and 'allocated'
        if (newOfferAmount > oldOfferAmount) {
            final PayingPossibleResult payingPossible = origin.getResourceDeposit().isPayingPossible(resourceType, difference);
            if (!payingPossible.isValid()) {
                throw new NotifyWebUserException("Not enough resources for trade.", payingPossible);
            }
            origin.getResourceDeposit().pay(resourceType, difference);
        } else {
            final long presentAmount = origin.getResourceDeposit().getResourceAmountByType(resourceType);
            // just add the difference back
            origin.getResourceDeposit().setAbsoluteResourceValue(resourceType, presentAmount - difference);
        }
        planetService.save(origin);

        offer.setTick(today);
        offer.setAmount(newOfferAmount);
        offer.setPrice(price);
        return tradeOfferRepository.save(offer);
    }
}
