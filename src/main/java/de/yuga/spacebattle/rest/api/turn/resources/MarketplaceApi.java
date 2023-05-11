package de.yuga.spacebattle.rest.api.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "MarketplaceApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + MarketplaceApi.ENDPOINT + "/")
public class MarketplaceApi extends BaseApi {

    public static final String ENDPOINT = "trade";
    private static final String TRADE_HISTORY_ENDPOINT = "history";
    private static final String TRADE_HISTORY_FOR_USER_ENDPOINT = "historyForUser";
    private static final String OFFER_ENDPOINT = "offer";

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Autowired
    public MarketplaceApi(@Nonnull final TickService tickService,
                          @Nonnull final MarketplaceService marketplaceService) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "tradedResourceService must not be empty");
    }

    @GetMapping(TRADE_HISTORY_ENDPOINT + "/{pastTicks}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getTrades",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradesByTick.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTrades(@PathVariable("pastTicks") final int pastTicks) {
        final List<Tick> timeframe = tickService.getTimeframe(pastTicks);
        final List<TradedResource> trades = marketplaceService.findForTicks(timeframe);
        final List<TradesByTick> result = mapTradesByTicks(timeframe, trades);
        return ResponseEntity.ok(result);
    }

    @Nonnull
    private List<TradesByTick> mapTradesByTicks(@Nonnull final List<Tick> timeframe, @Nonnull final List<TradedResource> trades) {
        Preconditions.checkNotNull(timeframe, "timeframe must not be empty");
        Preconditions.checkNotNull(trades, "trades must not be empty");

        final List<TradesByTick> result = new ArrayList<>();
        for (final Tick tick : timeframe) {
            final List<Trade> res = new ArrayList<>();
            final Map<EResourceType, List<TradedResource>> tradedResourcesMap = trades.stream()
                    .filter(t -> t.getTick().equals(tick))
                    .collect(Collectors.groupingBy(TradedResource::getResourceType,
                            Collectors.mapping(Function.identity(), Collectors.toList())));
            for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
                final List<TradedResource> tradedResources = tradedResourcesMap.getOrDefault(eResourceType, new ArrayList<>());
                tradedResources.forEach(tradedResource -> {
                    res.add(new Trade(tradedResource.getPrice(), eResourceType, tradedResource.getAmount()));
                });
            }
            result.add(new TradesByTick(tick, res));
        }
        return result;
    }

    @GetMapping(TRADE_HISTORY_FOR_USER_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "getTradesForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradesByLocation.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTradesForUser() {
        final Tick today = tickService.getToday();
        final List<TradedResource> trades = marketplaceService.findFinishedAndPendingTradesForUser(today, getIdUser());
        final List<TradesByLocation> result = mapTradeContracts(trades);
        return ResponseEntity.ok(result);
    }

    @Nonnull
    private List<TradesByLocation> mapTradeContracts(@Nonnull final List<TradedResource> trades) {
        Preconditions.checkNotNull(trades, "trades must not be empty");

        final int idUser = getIdUser();

        final Map<String, TradesByLocation> result = new HashMap<>();
        final Map<EResourceType, List<TradedResource>> tradedResourcesMap = trades.stream()
                .collect(Collectors.groupingBy(TradedResource::getResourceType,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final List<TradedResource> tradedResources = tradedResourcesMap.getOrDefault(eResourceType, new ArrayList<>());
            final Set<TradedResource> sales = tradedResources.stream().filter(tr -> tr.getSeller().getId() == idUser).collect(Collectors.toSet());
            final Set<TradedResource> purchases = new HashSet<>(tradedResources);
            purchases.removeAll(sales);

            purchases.forEach(p -> {
                final String key = "P-" + p.getDestination().getId();
                final TradesByLocation byLocation = result.getOrDefault(key, new TradesByLocation(null, p.getDestination()));
                byLocation.add(p.getTick(), p.getTicksLeft(), eResourceType, p.getPrice(), p.getAmount());
                result.put(key, byLocation);
            });

            sales.forEach(s -> {
                final String key = "S-" + s.getOrigin().getId();
                final TradesByLocation byLocation = result.getOrDefault(key, new TradesByLocation(s.getOrigin(), null));
                byLocation.add(s.getTick(), s.getTicksLeft(), eResourceType, s.getPrice(), s.getAmount());
                result.put(key, byLocation);
            });

        }
        return new ArrayList<>(result.values());
    }

    @PutMapping(OFFER_ENDPOINT)
    @Operation(summary = "Creates an offer", operationId = "setOffer",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TradeOffer.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TradeOffer.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> setOffer(@RequestBody @Nonnull final TradeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        final Tick today = tickService.getToday();
        final Integer idTradeOffer = offer.getIdTradeOffer();
        final de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer result;
        if (idTradeOffer == null) {
            result = marketplaceService.createOffer(today, getIdUser(), offer.getIdPlanetOrigin(), offer.getResourceAmount().getRealType(), offer.getResourceAmount().getAmount(), offer.getPrice());
        } else {
            result = marketplaceService.updateOffer(today, idTradeOffer, offer.getResourceAmount().getAmount(), offer.getPrice());
        }
        return ResponseEntity.ok(new TradeOffer(result));
    }

    @GetMapping(OFFER_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "getOffers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradeOffer.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getOffers() {
        final List<de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer> trades = marketplaceService.findActiveOffers();
        return ResponseEntity.ok(trades.stream().map(TradeOffer::new).collect(Collectors.toList()));
    }

    @PostMapping(value = OFFER_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all EResourceTypes.", operationId = "takeOffer",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TradeContract.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> takeOffer(@RequestBody @Nonnull final TakeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        final Tick today = tickService.getToday();
        final TradedResource trade = marketplaceService.takeOffer(today, offer.getIdTradeOffer(), getIdUser(), offer.getIdDestination());
        return ResponseEntity.ok(new TradeContract(trade));
    }
}
