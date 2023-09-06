package de.yuga.spacebattle.rest.api.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.resources.trade.TradesInTimeframe;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EResourceType;
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
    private static final String SPOT_ENDPOINT = "spot";

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Autowired
    public MarketplaceApi(@Nonnull final MarketplaceService marketplaceService) {
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "tradedResourceService must not be empty");
    }

    @GetMapping(TRADE_HISTORY_ENDPOINT + "/{pastTicks}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getTrades",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ValueTradesByTick.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTrades(@PathVariable("pastTicks") final int pastTicks) {
        final TradesInTimeframe tradesInTimeframe = marketplaceService.findForTicks(pastTicks);
        final List<ValueTradesByTick> result = mapTradesByTicks(tradesInTimeframe);
        return ResponseEntity.ok(result);
    }

    @Nonnull
    private List<ValueTradesByTick> mapTradesByTicks(@Nonnull final TradesInTimeframe tradesInTimeframe) {
        Preconditions.checkNotNull(tradesInTimeframe, "tradesInTimeframe must not be empty");

        final List<ValueTradesByTick> result = new ArrayList<>();
        for (final Tick tick : tradesInTimeframe.getTimeframe()) {
            final List<Trade> res = new ArrayList<>();
            final Map<EResourceType, List<TradedResource>> tradedResourcesMap = tradesInTimeframe.getTrades().stream()
                    .filter(t -> t.getTick().equals(tick))
                    .collect(Collectors.groupingBy(tr -> tr.getTradeOffer().getResourceType(),
                            Collectors.mapping(Function.identity(), Collectors.toList())));
            for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
                final List<TradedResource> tradedResources = tradedResourcesMap.getOrDefault(eResourceType, new ArrayList<>());
                tradedResources.forEach(tradedResource -> {
                    res.add(new Trade(tradedResource.getTradeOffer().getUnitPrice(), eResourceType, tradedResource.getTradeOffer().getAmount()));
                });
            }
            result.add(new ValueTradesByTick(tick, res));
        }
        return result;
    }

    @GetMapping(TRADE_HISTORY_FOR_USER_ENDPOINT)
    @Operation(summary = "Get all today relevant trades.", operationId = "getTradesForUser",
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
        final TradesInTimeframe tradesInTimeframe = marketplaceService.findFinishedAndPendingTradesForUser(getIdUser());
        final List<TradesByLocation> result = mapTradeContracts(tradesInTimeframe);
        return ResponseEntity.ok(result);
    }


    @GetMapping(TRADE_HISTORY_FOR_USER_ENDPOINT + "/fresh")
    @Operation(summary = "Get all today contracted trades.", operationId = "getFreshTradesForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = TradeContract.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getFreshTradesForUser() {
        final List<TradedResource> result = marketplaceService.findTodayTradedResourceForUser(getIdUser());
        return ResponseEntity.ok(result.stream().map(TradeContract::new).collect(Collectors.toList()));
    }

    @Nonnull
    private List<TradesByLocation> mapTradeContracts(@Nonnull final TradesInTimeframe tradesInTimeframe) {
        Preconditions.checkNotNull(tradesInTimeframe, "tradesInTimeframe must not be empty");

        final int idUser = getIdUser();

        // this case only has today as timeframe - recycling the dto
        final Tick today = tradesInTimeframe.getTimeframe().get(0);
        final Map<Integer, TradesByLocation> result = new HashMap<>();
        final Map<EResourceType, List<TradedResource>> tradedResourcesMap = tradesInTimeframe.getTrades().stream()
                .collect(Collectors.groupingBy(tr -> tr.getTradeOffer().getResourceType(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final List<TradedResource> tradedResources = tradedResourcesMap.getOrDefault(eResourceType, new ArrayList<>());
            final Set<TradedResource> sales = tradedResources.stream().filter(tr -> tr.getTradeOffer().getSeller().getId() == idUser).collect(Collectors.toSet());
            final Set<TradedResource> purchases = new HashSet<>(tradedResources);
            purchases.removeAll(sales);

            purchases.forEach(p -> {
                final int key = p.getDestination().getId();
                final TradesByLocation byLocation = result.getOrDefault(key, new TradesByLocation(null, p.getDestination()));
                byLocation.addPurchase(today, p);
                result.put(key, byLocation);
            });

            sales.forEach(s -> {
                final int key = s.getTradeOffer().getOrigin().getId();
                final TradesByLocation byLocation = result.getOrDefault(key, new TradesByLocation(s.getTradeOffer().getOrigin(), null));
                byLocation.addSale(today, s);
                result.put(key, byLocation);
            });

        }
        return new ArrayList<>(result.values());
    }

    @PutMapping(value = OFFER_ENDPOINT)
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

        final Integer idTradeOffer = offer.getIdTradeOffer();
        final de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer result;
        if (idTradeOffer == null) {
            result = marketplaceService.createOffer(getIdUser(), offer.getIdPlanetOrigin(), offer.getResourceAmount().getRealType(), offer.getResourceAmount().getAmount(), offer.getPricePerUnit());
        } else {
            result = marketplaceService.updateOffer(idTradeOffer, offer.getResourceAmount().getAmount(), offer.getPricePerUnit());
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

    @PostMapping(value = OFFER_ENDPOINT)
    @Operation(summary = "Get all EResourceTypes.", operationId = "takeOffer", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TakeOffer.class)
            )
    ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TradeContract.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> takeOffer(@RequestBody @Nonnull final TakeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        final TradedResource trade = marketplaceService.takeOffer(offer.getIdTradeOffer(), getIdUser(), offer.getIdDestination());
        return ResponseEntity.ok(new TradeContract(trade));
    }

    @GetMapping(SPOT_ENDPOINT + "/pricePerUnit/{resourceType}")
    @Operation(summary = "Get all EResourceTypes.", operationId = "getSpotPrice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getSpotPrice(@PathVariable("resourceType") @Nonnull final String resourceType) {
        final EResourceType eResourceType = EResourceType.valueOf(resourceType);
        final int price = marketplaceService.findOfferSpotPrice(getIdUser(), eResourceType);
        return ResponseEntity.ok(price);
    }

    @PutMapping(SPOT_ENDPOINT + "/sell")
    @Operation(summary = "Get all EResourceTypes.", operationId = "sellAtSpotMarket",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpotOffer.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> sellAtSpotMarket(@RequestBody @Nonnull final SpotOffer spotOffer) {

        marketplaceService.sellSpotOffer(getIdUser(), spotOffer);
        return ResponseEntity.ok(true);
    }

    @PutMapping(SPOT_ENDPOINT + "/buy")
    @Operation(summary = "Get all EResourceTypes.", operationId = "buyAtSpotMarket",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SpotOffer.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> buyAtSpotMarket(@RequestBody @Nonnull final SpotOffer spotOffer) {

        marketplaceService.buySpotOffer(getIdUser(), spotOffer);
        return ResponseEntity.ok(true);
    }
}
