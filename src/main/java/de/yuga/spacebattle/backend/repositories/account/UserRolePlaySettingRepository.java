package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nullable;

public interface UserRolePlaySettingRepository extends JpaRepository<RolePlaySetting, Integer> {

    @Nullable
    @Query("SELECT r FROM RolePlaySetting r WHERE r.user.id = :idUser")
    RolePlaySetting findForUser(final int idUser);
}
