package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NonPlayerCharacterRepository extends JpaRepository<NonPlayerCharacter, Integer> {

    @Nullable
    @Query("SELECT n FROM NonPlayerCharacter  n WHERE n.username = :username")
    NonPlayerCharacter findByUsername(@Param("username") @Nonnull final String username);
}
