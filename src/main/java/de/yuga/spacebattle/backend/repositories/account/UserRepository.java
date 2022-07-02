package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer>, CustomUserRepository {

    @Nullable
    @Query(name = "User.findAllianceAdminByAlliance")
    List<User> findAllianceAdminByAlliance(@Param("alliance") @Nonnull final Alliance alliance, @Param("gameUserRole") @Nonnull final EGameUserRole gameUserRole);
}
