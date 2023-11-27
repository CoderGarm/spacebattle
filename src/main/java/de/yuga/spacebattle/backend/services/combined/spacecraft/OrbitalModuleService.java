package de.yuga.spacebattle.backend.services.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.repositories.combined.spacecraft.OrbitalModuleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Service
public class OrbitalModuleService {

    @Nonnull
    private final OrbitalModuleRepository orbitalModuleRepository;

    public OrbitalModuleService(@Nonnull final OrbitalModuleRepository orbitalModuleRepository) {
        Preconditions.checkNotNull(orbitalModuleRepository, "orbitalModuleRepository shouldn't be null!");

        this.orbitalModuleRepository = orbitalModuleRepository;
    }

    @Nonnull
    public List<OrbitalModule> findAll() {
        return orbitalModuleRepository.findAll();
    }

    @Nonnull
    public List<OrbitalModule> findAll(@Nonnull final Collection<Integer> ids) {
        Preconditions.checkNotNull(ids, "ids must not be empty");

        return orbitalModuleRepository.findAllById(ids);
    }

    @Nullable
    public OrbitalModule find(@Nonnull final Integer idOrbitalModule) {
        Preconditions.checkNotNull(idOrbitalModule, "idOrbitalModule shouldn't be null!");

        return orbitalModuleRepository.findById(idOrbitalModule).orElse(null);
    }

    public OrbitalModule save(@Nonnull final OrbitalModule entities) {
        Preconditions.checkNotNull(entities, "entities must not be empty");

        return orbitalModuleRepository.save(entities);
    }

    public List<OrbitalModule> saveAll(@Nonnull final Collection<OrbitalModule> toStore) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");

        return orbitalModuleRepository.saveAll(toStore);
    }

    @Nonnull
    public Set<OrbitalModule> findOrbitalModulesByUser(final int idUser) {
        return Objects.requireNonNullElse(orbitalModuleRepository.findOrbitalModulesByUser(idUser), new HashSet<>());
    }
}
