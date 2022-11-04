package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.repositories.spacecraft.HullRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class HullService {

    @Nonnull
    private final HullRepository hullRepository;

    public HullService(@Nonnull final HullRepository hullRepository) {
        Preconditions.checkNotNull(hullRepository, "hullRepository shouldn't be null!");

        this.hullRepository = hullRepository;
    }

    @Nonnull
    public List<Hull> findAll() {
        return hullRepository.findAll();
    }

    @Nonnull
    public List<Hull> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return hullRepository.findAllByUser(user);
    }

    @Nullable
    public Hull find(@Nonnull final Integer idHull) {
        Preconditions.checkNotNull(idHull, "idHull shouldn't be null!");

        return hullRepository.findById(idHull).orElse(null);
    }

    @Nonnull
    public List<Hull> findByHullType(@Nonnull final EHullType hullType) {
        Preconditions.checkNotNull(hullType, "hullType shouldn't be null!");

        return Objects.requireNonNullElse(hullRepository.findByHullType(hullType), new ArrayList<>());
    }

    @Nonnull
    @Deprecated(since = "productive environment")
    @SuppressWarnings("DeprecatedIsStillUsed")
    public Hull createHull(@Nonnull final String name,
                           final int ccOverall,
                           final int ccModules,
                           final int ccBow,
                           final int ccStern,
                           final int ccBroadsides,
                           @Nonnull final ETechLevel techLevel,
                           @Nonnull final String description,
                           @Nonnull final Research unlockedThrough,
                           @Nonnull final EHullType hullType,
                           @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(hullType, "hullType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        return hullRepository.save(new Hull(name, ccOverall, ccModules, ccBow, ccStern, ccBroadsides, techLevel, description, unlockedThrough, hullType, crewRequirement));
    }

    public Hull save(@Nonnull final Hull entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return hullRepository.save(entity);
    }

    public void saveAll(@Nonnull final Collection<Hull> toStore) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");

        hullRepository.saveAll(toStore);
    }
}
