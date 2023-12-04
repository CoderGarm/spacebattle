package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.OrbitalStructureRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Nonnull
    public Map<StarSystem, List<OrbitalStructure>> findAllBySystem(@Nonnull final Set<Integer> starSystemIDs) {
        Preconditions.checkNotNull(starSystemIDs, "starSystemIDs must not be empty");

        //noinspection DataFlowIssue
        return Objects.requireNonNullElse(orbitalStructureRepository.findAllBySystem(starSystemIDs), new ArrayList<OrbitalStructure>()).stream()
                .collect(Collectors.groupingBy(a -> a.getOrbit().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    @Nonnull
    public Map<StarSystem, List<OrbitalStructure>> findAllBySystemForUser(@Nonnull final Set<Integer> starSystemIDs, final int idOwner) {
        Preconditions.checkNotNull(starSystemIDs, "starSystemIDs must not be empty");

        //noinspection DataFlowIssue
        return Objects.requireNonNullElse(orbitalStructureRepository.findAllBySystemForUser(starSystemIDs, idOwner), new ArrayList<OrbitalStructure>()).stream()
                .collect(Collectors.groupingBy(a -> a.getOrbit().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
