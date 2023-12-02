package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.OrbitalStructureRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class OrbitalStructureService {

    @Nonnull
    private final OrbitalStructureRepository orbitalStructureRepository;

    public OrbitalStructureService(@Nonnull final OrbitalStructureRepository orbitalStructureRepository) {
        Preconditions.checkNotNull(orbitalStructureRepository, "orbitalStructureRepository shouldn't be null!");

        this.orbitalStructureRepository = orbitalStructureRepository;
    }

    @Nonnull
    public List<OrbitalStructure> findAll() {
        return orbitalStructureRepository.findAll();
    }

    @Nonnull
    public List<OrbitalStructure> findAll(@Nonnull final Collection<Integer> ids) {
        Preconditions.checkNotNull(ids, "ids must not be empty");

        return orbitalStructureRepository.findAllById(ids);
    }

    @Nullable
    public OrbitalStructure find(@Nonnull final Integer idOrbitalStructure) {
        Preconditions.checkNotNull(idOrbitalStructure, "idOrbitalStructure shouldn't be null!");

        return orbitalStructureRepository.findById(idOrbitalStructure).orElse(null);
    }

    public OrbitalStructure save(@Nonnull final OrbitalStructure entities) {
        Preconditions.checkNotNull(entities, "entities must not be empty");

        return orbitalStructureRepository.save(entities);
    }

    public List<OrbitalStructure> saveAll(@Nonnull final Collection<OrbitalStructure> toStore) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");

        return orbitalStructureRepository.saveAll(toStore);
    }

    @Nonnull
    public List<OrbitalStructure> findByPlanet(final int idPlanet) {
        return Objects.requireNonNullElse(orbitalStructureRepository.findByPlanet(idPlanet), new ArrayList<>());
    }
}
