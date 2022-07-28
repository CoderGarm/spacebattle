package de.yuga.spacebattle.backend.repositories.i18n;

import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranslatableRepository extends JpaRepository<Translatable, Integer> {
}
