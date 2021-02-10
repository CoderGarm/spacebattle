package de.yuga.spacebattle.backend.logic.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.repositories.constructables.buildings.ConstructionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class ConstructionService {

    @Nonnull
    private final ConstructionRepository constructionRepository;

    @Autowired
    public ConstructionService(@Nonnull final ConstructionRepository constructionRepository) {
        Preconditions.checkNotNull(constructionRepository, "constructionRepository shouldn't be null!");

        this.constructionRepository = constructionRepository;
    }

    @Nonnull
    public List<Construction> findAll() {
        return constructionRepository.findAllConstructions();
    }

    @Nullable
    public Construction find(@Nonnull final Integer idConstruction) {
        Preconditions.checkNotNull(idConstruction, "idConstruction shouldn't be null!");

        return constructionRepository.findById(idConstruction).orElse(null);
    }

    public Construction save(@Nonnull final Construction entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return constructionRepository.save(entity);
    }

    public void delete(@Nonnull final Construction entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        constructionRepository.delete(entity);
    }
}
