package de.yuga.spacebattle.backend.services.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.resources.trade.TradesInTimeframe;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.repositories.turn.resource.trade.TradeOfferRepository;
import de.yuga.spacebattle.backend.repositories.turn.resource.trade.TradedResourceRepository;
import de.yuga.spacebattle.backend.services.account.OwnerService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.SpotOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MarketplaceService {

    @Nonnull
    private final TickTimeService tickService;

    @Nonnull
    private final OwnerService ownerService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TradedResourceRepository tradedResourceRepository;

    @Nonnull
    private final TradeOfferRepository tradeOfferRepository;

    @Autowired
    public MarketplaceService(@Nonnull final TickTimeService tickService,
                              @Nonnull final OwnerService ownerService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final TradedResourceRepository tradedResourceRepository,
                              @Nonnull final TradeOfferRepository tradeOfferRepository) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.ownerService = Preconditions.checkNotNull(ownerService, "ownerService must not be empty");
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

        final Owner seller = tradeOffer.getSeller();
        final Owner buyer = ownerService.find(idBuyer);
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
    private TradedResource checkAndAllocatePayment(@Nonnull final Tick today,
                                                   @Nonnull final TradeOffer tradeOffer,
                                                   @Nonnull final Owner buyer,
                                                   @Nonnull final Planet destination) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(tradeOffer, "tradeOffer must not be empty");
        Preconditions.checkNotNull(buyer, "buyer must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final long price = tradeOffer.getUnitPrice();
        if (isHumanUser(buyer)) {
            // paying is possible by default for NPCs

            final PayingPossibleResult payingPossible = destination.getResourceDeposit().isPayingPossible(EResourceType.CREDITS, price);
            if (!payingPossible.isValid()) {
                throw new NotifyWebUserException("Not enough credits to take the offer.", payingPossible);
            }

            // the destination pays the bill
            destination.getResourceDeposit().pay(EResourceType.CREDITS, price);
            planetService.save(destination);
        }

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

        final List<TradeOffer> offers = Objects.requireNonNullElse(tradeOfferRepository.findLatestOffer(resourceType, PageRequest.of(0, 100)), List.of());
        if (offers.isEmpty()) {
            final ETechLevel techLevelOf = ETechLevel.getTechLevelOf(resourceType);
            switch (techLevelOf) {
                case TECH_I:
                    return 50;
                case TECH_II:
                    return 75;
                case TECH_III:
                default:
                    return 100;
            }
        }

        final long averagePriceClosed = getAverageUnitPrice(offers.stream().filter(Deletable::isDeleted));

        final long averagePriceOpen = getAverageUnitPrice(offers.stream().filter(t -> t.getSeller().getId() != idUser).filter(Deletable::isAlive));
        if (averagePriceOpen == 0 || averagePriceClosed == 0) {
            return (int) Long.max(averagePriceOpen, averagePriceClosed);
        }
        // taken offers are more important than just existent offers
        return (int) (averagePriceClosed + averagePriceClosed + averagePriceOpen) / 3;
    }

    private long getAverageUnitPrice(@Nonnull final Stream<TradeOffer> tradeOfferStream) {
        Preconditions.checkNotNull(tradeOfferStream, "tradeOfferStream must not be empty");

        final List<Long> unitPrices = new ArrayList<>();
        tradeOfferStream.forEach(offer -> {
            for (int i = 1; i < offer.getAmount(); i++) {
                unitPrices.add(offer.getUnitPrice());
            }
        });
        return unitPrices.isEmpty() ? 0 : unitPrices.stream().reduce(0L, Long::sum) / unitPrices.size();
    }

    @Nonnull
    public TradeOffer createOffer(final int idSeller,
                                  final int idPlanetOrigin,
                                  @Nonnull final EResourceType resourceType,
                                  final long amount,
                                  final long pricePerUnit) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        final Tick today = tickService.getToday();
        final Owner seller = ownerService.find(idSeller);
        final Planet origin = planetService.find(idPlanetOrigin);
        if ((origin == null || origin.getOwner() == null) || !origin.getOwner().equals(seller)) {
            throw new NotifyWebUserException("In the interests of good bookkeeping, you should leave it alone.");
        }

        return checkAndAllocateOfferPayload(today, seller, origin, resourceType, amount, pricePerUnit);
    }

    @Nonnull
    private TradeOffer checkAndAllocateOfferPayload(@Nonnull final Tick initialTick,
                                                    @Nonnull final Owner seller,
                                                    @Nonnull final Planet origin,
                                                    @Nonnull final EResourceType resourceType,
                                                    final long amount,
                                                    final long price) {
        Preconditions.checkNotNull(initialTick, "initialTick must not be empty");
        Preconditions.checkNotNull(seller, "seller must not be empty");
        Preconditions.checkNotNull(origin, "origin must not be empty");

        if (isHumanUser(seller)) {
            // paying is possible by default for NPCs

            final PayingPossibleResult payingPossible = origin.getResourceDeposit().isPayingPossible(resourceType, amount);
            if (!payingPossible.isValid()) {
                throw new NotifyWebUserException("Not enough resources for trade.", payingPossible);
            }

            // payload is persisted in the offer and 'allocated'
            origin.getResourceDeposit().pay(resourceType, amount);
            planetService.save(origin);
        }

        return tradeOfferRepository.save(new TradeOffer(initialTick, seller, origin, resourceType, amount, price));
    }

    private static boolean isHumanUser(@Nonnull final Owner owner) {
        Preconditions.checkNotNull(owner, "owner must not be empty");

        return owner.getHumanOwner() != null;
    }

    @Nonnull
    public TradeOffer updateOffer(final int idTradeOffer, final long newOfferAmount, final long pricePerUnit) {

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
        offer.setUnitPrice(pricePerUnit);
        return tradeOfferRepository.save(offer);
    }

    public void sellSpotOffer(final int idUser, @Nonnull final SpotOffer spotOffer) {
        Preconditions.checkNotNull(spotOffer, "spotOffer must not be empty");

        final ResourceAmount resourceAmount = spotOffer.getResourceAmount();
        final int price = findOfferSpotPrice(idUser, resourceAmount.getRealType());
        final TradeOffer offer = createOffer(idUser, spotOffer.getIdPlanet(), resourceAmount.getRealType(), resourceAmount.getAmount(), price);

        final Planet mainPlanet = planetService.findMainPlanet(ownerService.getRandomNPC());
        takeOffer(offer.getId(), Objects.requireNonNull(mainPlanet.getNpcOwner()).getId(), mainPlanet.getId());
    }

    public void buySpotOffer(final int idUser, @Nonnull final SpotOffer spotOffer) {
        Preconditions.checkNotNull(spotOffer, "spotOffer must not be empty");

        final ResourceAmount resourceAmount = spotOffer.getResourceAmount();
        final int price = findOfferSpotPrice(idUser, resourceAmount.getRealType());

        final Planet mainPlanet = planetService.findMainPlanet(ownerService.getRandomNPC());

        final int idNPC = Objects.requireNonNull(mainPlanet.getNpcOwner()).getId();
        final TradeOffer offer = createOffer(idNPC, mainPlanet.getId(), resourceAmount.getRealType(), resourceAmount.getAmount(), price);

        takeOffer(offer.getId(), idUser, spotOffer.getIdPlanet());
    }
}
