package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMission;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMissionItem;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.entities.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.services.turn.mission.MissionItemService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Attacks trades at the start and the end.
 */
@Service
public class RaidingConvoyMission implements MissionRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(RaidingConvoyMission.class);

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final MissionItemService missionItemService;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final HeatMapService heatMapService;

    @Autowired
    public RaidingConvoyMission(@Nonnull final MissionItemService missionItemService,
                                @Nonnull final MarketplaceService marketplaceService,
                                @Nonnull final MissionService missionService,
                                @Nonnull final HeatMapService heatMapService) {
        this.missionItemService = Preconditions.checkNotNull(missionItemService, "pirateSpawnPhase must not be empty");
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "marketplaceService must not be empty");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
    }

    @Override
    public void executeMission(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Passive pirate mission started");
        attackConvoys();
        releaseAllShips();
        LOGGER.info("Passive pirate mission done");
    }

    private void releaseAllShips() {
        final List<TradedResource> trades = marketplaceService.findTodayTrades();
        final Set<Integer> tradeIDs = trades.stream().map(AbstractEntityKey::getId).collect(Collectors.toSet());
        final Map<Integer, List<ConvoyProtectionMission>> missionsByActorId = missionService.findConvoyProtectionForTrades(tradeIDs).stream()
                .collect(Collectors.groupingBy(m -> m.getActor().getId(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
        missionsByActorId.forEach((userId, missions) ->
                missions.forEach(m -> missionService.stopMission(m, userId)));
    }

    private void attackConvoys() {
        final List<TradedResource> trades = marketplaceService.findTradesToAttack();
        final Map<TradedResource, List<ConvoyProtectionMission>> missions = missionService.findConvoyProtectionForTrades(trades.stream()
                        .map(AbstractEntityKey::getId)
                        .collect(Collectors.toSet())).stream()
                .collect(Collectors.groupingBy(ConvoyProtectionMission::getProtectedTrade,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Set<Planet> planets = trades.stream()
                .map(TradedResource::getDestination)
                .map(Planet::getSystem)
                .map(StarSystem::getPlanets)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        planets.addAll(trades.stream()
                .map(TradedResource::getTradeOffer)
                .map(TradeOffer::getOrigin)
                .map(Planet::getSystem)
                .map(StarSystem::getPlanets)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));

        // take regional pirate activity
        final Map<Planet, HeatMap> heatMap = heatMapService.findHeatForPlanets(planets, EMissionType.PIRATE_RAID).stream()
                .collect(Collectors.toMap(HeatMap::getPlanet, Function.identity()));

        trades.forEach(tradedResource -> attackConvoy(tradedResource, missions.getOrDefault(tradedResource, new ArrayList<>()), heatMap));
    }

    private void attackConvoy(@Nonnull final TradedResource tradedResource,
                              @Nonnull final List<ConvoyProtectionMission> protectionMissions,
                              @Nonnull final Map<Planet, HeatMap> heatMap) {
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");
        Preconditions.checkNotNull(protectionMissions, "protectionMissions must not be empty");
        Preconditions.checkNotNull(heatMap, "heatMap must not be empty");

        //noinspection DataFlowIssue
        LOGGER.info("Attacking convoy {} from {} to {}",
                tradedResource.getId(),
                tradedResource.getTradeOffer().getOrigin().getOwner().getUsername(),
                tradedResource.getDestination().getOwner().getUsername());

        final EMissionAction phase = today.getNo() - tradedResource.getTick().getNo() <= 1 ? EMissionAction.BEGIN_OF_MISSION : EMissionAction.END_OF_MISSION;
        final Set<Planet> planets;
        switch (phase) {
            case END_OF_MISSION:
                planets = tradedResource.getDestination().getSystem().getPlanets();
                break;
            case BEGIN_OF_MISSION:
                planets = tradedResource.getTradeOffer().getOrigin().getSystem().getPlanets();
                break;
            default:
                throw new NotifyWebUserException("Please dont do this!");
        }

        final Set<WarShip> defenders = protectionMissions.stream()
                .map(ConvoyProtectionMission::getShips)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        int defendersHeatImpact = defenders.stream()
                .map(WarShip::getShipClass)
                .map(ShipClass::getShipClassType)
                .map(eShipClassType -> eShipClassType.getHeatImpact(EMissionType.CONVOY_PROTECTION))
                .reduce(0, Integer::sum);

        defendersHeatImpact = setUpNPCProtection(defendersHeatImpact, protectionMissions);

        final int systemsCumulatedHeat = planets.stream()
                .map(heatMap::get)
                .filter(Objects::nonNull)
                .map(HeatMap::getHeat)
                .reduce(0, Integer::sum);

        final ConvoyProtectionMissionItem whatever;
        if (systemsCumulatedHeat > defendersHeatImpact) {
            LOGGER.info("\tPirates are quarrelsome");

            final double chanceToEvade = (systemsCumulatedHeat + defendersHeatImpact) / 100D;
            final double evade = ThreadLocalRandom.current().nextDouble(0, 1);
            boolean hasEvaded = evade <= chanceToEvade;
            if (hasEvaded) {
                LOGGER.info("\t\t...but not well equipped");
                whatever = MissionItem.convoyGuardedWithShipContact(today, tradedResource, phase);
            } else {
                final int pirateForce = systemsCumulatedHeat - defendersHeatImpact;
                final int percentOfCargoLost;
                if (pirateForce > 0 && pirateForce < 10) {
                    percentOfCargoLost = Integer.parseInt(pirateForce + "0");
                } else {
                    percentOfCargoLost = Integer.parseInt(String.valueOf(pirateForce).substring(0, 1));
                }
                final boolean isRansom = ThreadLocalRandom.current().nextInt(0, 100) >= 50;
                LOGGER.info("\t\t...and they take {} {}", percentOfCargoLost, (isRansom ? " for ransom" : "on their own"));
                whatever = MissionItem.convoyRaided(today, tradedResource, phase, percentOfCargoLost, isRansom);
            }
        } else {
            LOGGER.info("\tPirates are scared");
            whatever = MissionItem.convoyGuardedOnSight(today, tradedResource, phase);
        }
        missionItemService.save(whatever);
    }

    private int setUpNPCProtection(int defendersHeatImpact, @Nonnull final List<ConvoyProtectionMission> protectionMissions) {
        Preconditions.checkNotNull(protectionMissions, "protectionMissions must not be empty");

        defendersHeatImpact = protectionMissions.stream().map(m -> {
            final NonPlayerCharacter buyer = m.getProtectedTrade().getBuyer().getNpcOwner();
            final NonPlayerCharacter seller = m.getProtectedTrade().getTradeOffer().getSeller().getNpcOwner();
            return buyer == null && seller == null ? 0 : (3 * EShipClassType.DD.getHeatImpact(EMissionType.CONVOY_PROTECTION)); // npc sends 3 destroyers
        }).reduce(defendersHeatImpact, Integer::sum);

        return defendersHeatImpact;
    }
}
