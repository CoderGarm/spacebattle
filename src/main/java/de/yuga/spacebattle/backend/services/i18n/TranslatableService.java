package de.yuga.spacebattle.backend.services.i18n;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.repositories.i18n.TranslatableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TranslatableService {

    @Nonnull
    private final TranslatableRepository translatableRepository;

    @Autowired
    public TranslatableService(@Nonnull final TranslatableRepository translatableRepository) {
        Preconditions.checkNotNull(translatableRepository, "translatableRepository must not be empty");

        this.translatableRepository = translatableRepository;
    }

    @Nonnull
    public List<Translatable> saveAll(@Nonnull final Collection<Translatable> toStore) {
        Preconditions.checkNotNull(toStore, "toStore shouldn't be null!");

        final Iterable<Translatable> translatables = translatableRepository.saveAll(toStore);
        return StreamSupport.stream(translatables.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<Translatable> findAll() {
        return Objects.requireNonNullElse(translatableRepository.findAll(), new ArrayList<>());
    }

    public Translatable save(@Nonnull final Translatable translatable) {
        Preconditions.checkNotNull(translatable, "translatable must not be empty");

        return translatableRepository.save(translatable);
    }

    @Nullable
    public Translatable findById(final int idTranslatable) {
        return translatableRepository.findById(idTranslatable).orElse(null);
    }
}
