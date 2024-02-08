package de.yuga.spacebattle.backend.services.caches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.rest.dto.turn.resources.PopulationOverview;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@Service
public class PlanetaryResourceCache extends DisabledWhileTicking {

    @Nonnull
    private final CacheStore<Integer, ResourceDeposit> ticklyIncomeCache = new CacheStore<>(1, TimeUnit.DAYS);

    @Nonnull
    private final CacheStore<Integer, PopulationOverview> overallPopCache = new CacheStore<>(1, TimeUnit.DAYS);

    public PlanetaryResourceCache() {
    }

    public void addTicklyIncome(final int idPlanet, @Nonnull final ResourceDeposit ticklyIncome) {
        Preconditions.checkNotNull(ticklyIncome, "ticklyIncome must not be empty");
        Preconditions.checkArgument(ticklyIncome.getSubType() == EDepositType.INCOME, "ticklyIncome must not be INCOME");

        ticklyIncomeCache.put(idPlanet, ticklyIncome);
    }

    @Nullable
    public ResourceDeposit getTicklyIncome(final int idPlanet) {
        if (isActive()) {
            return ticklyIncomeCache.get(idPlanet);
        }
        return null;
    }

    public void invalideTicklyIncome(final int idPlanet) {
        ticklyIncomeCache.invalidateKey(idPlanet);
    }

    public void addPopulationOverview(final int idUser, @Nonnull final PopulationOverview ticklyIncome) {
        Preconditions.checkNotNull(ticklyIncome, "ticklyIncome must not be empty");

        overallPopCache.put(idUser, ticklyIncome);
    }

    @Nullable
    public PopulationOverview getPopulationOverview(final int idUser) {
        if (isActive()) {
            return overallPopCache.get(idUser);
        }
        return null;
    }

    public void invalidePopulationOverview(final int idUser) {
        overallPopCache.invalidateKey(idUser);
    }
}
