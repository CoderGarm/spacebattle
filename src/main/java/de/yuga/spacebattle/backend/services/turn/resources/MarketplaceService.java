package de.yuga.spacebattle.backend.services.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.resources.trade.TradesInTimeframe;
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
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.TakeSpotOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class MarketplaceService {

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TradedResourceRepository tradedResourceRepository;

    @Nonnull
    private final TradeOfferRepository tradeOfferRepository;

    @Autowired
    public MarketplaceService(@Nonnull final TickService tickService,
                              @Nonnull final UserService userService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final TradedResourceRepository tradedResourceRepository,
                              @Nonnull final TradeOfferRepository tradeOfferRepository) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.tradedResourceRepository = Preconditions.checkNotNull(tradedResourceRepository, "tradedResourceRepository shouldn't be null!");
        this.tradeOfferRepository = Preconditions.checkNotNull(tradeOfferRepository, "tradeOfferRepository must not be empty");
    }

    @Nonnull
    public TradesInTimeframe findForTicks(final int pastTicks) {
        final List<Tick> timeframe = tickService.getTimeframe(pastTicks);
        final List<Integer> ticks = timeframe.stream().map(Tick::getNo).collect(Collectors.toList());
        final List<TradedResource> trades = Objects.requireNonNullElse(tradedResourceRepository.findForTicks(ticks), new ArrayList<>());

        return new TradesInTimeframe(timeframe, trades);
    }

    @Nonnull
    public TradesInTimeframe findFinishedAndPendingTradesForUser(final int idUser) {
        final Tick today = tickService.getToday();
        final List<TradedResource> trades = Objects.requireNonNullElse(tradedResourceRepository.findFinishedAndPendingTradesForUser(today.getNo(), idUser), new ArrayList<>());
        return new TradesInTimeframe(List.of(today), trades);
    }


    @Nonnull
    public TradedResource takeOffer(final int idTradeOffer, final int idBuyer, final int idPlanetDestination) {

        final Tick today = tickService.getToday();
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

    /**
     * Takes the last n offers for the given resource which are not from the given user and creates the average.<br>
     * If no offers are present, it will return a random number in a pseudo-realistic range.
     */
    public int findOfferSpotPrice(final int idUser, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        final Tick today = tickService.getToday();
        final int sinceTick = today.getNo() - 10;
        final List<TradeOffer> offers = Objects.requireNonNullElse(tradeOfferRepository.findLatestOffer(idUser, sinceTick, resourceType), List.of());
        if (offers.isEmpty()) {
            return ThreadLocalRandom.current().nextInt(51, 102);
        }
        final List<Long> unitPrices = offers.stream().map(o -> o.getPrice() / o.getAmount()).collect(Collectors.toList());
        final long averagePrice = unitPrices.stream().reduce(0L, Long::sum) / unitPrices.size();
        return (int) averagePrice;
    }

    @Nonnull
    public TradeOffer createOffer(final int idSeller,
                                  final int idPlanetOrigin,
                                  @Nonnull final EResourceType resourceType,
                                  final long amount,
                                  final long price) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        final Tick today = tickService.getToday();
        final User seller = userService.find(idSeller);
        final Planet origin = planetService.find(idPlanetOrigin);
        if ((origin == null || origin.getOwner() == null) || !origin.getOwner().equals(seller)) {
            throw new NotifyWebUserException("In the interests of good bookkeeping, you should leave it alone.");
        }

        return checkAndAllocateOfferPayload(today, seller, origin, resourceType, amount, price);
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
    public TradeOffer updateOffer(final int idTradeOffer, final long newOfferAmount, final long price) {

        final Tick today = tickService.getToday();
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

    public void sellSpotOffer(final int idUser, @Nonnull final TakeSpotOffer takeSpotOffer) {
        Preconditions.checkNotNull(takeSpotOffer, "takeSpotOffer must not be empty");

        final int price = findOfferSpotPrice(idUser, takeSpotOffer.getResourceAmount().getRealType());
        /* fixme create offer from user to NPC */
    }
}
