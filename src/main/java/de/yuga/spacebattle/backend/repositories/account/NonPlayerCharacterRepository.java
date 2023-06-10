package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface NonPlayerCharacterRepository extends JpaRepository<NonPlayerCharacter, Integer> {

    @Nullable
    @Query("SELECT n FROM NonPlayerCharacter  n WHERE n.username = :username")
    NonPlayerCharacter findByUsername(@Param("username") @Nonnull final String username);

    @Nonnull
    @Query("SELECT n FROM NonPlayerCharacter n")
    List<Owner> getNPC(@Nonnull final Pageable pageable);
}
