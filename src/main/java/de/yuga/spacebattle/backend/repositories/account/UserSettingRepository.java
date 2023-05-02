package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.UserSetting;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;

public interface UserSettingRepository extends CrudRepository<UserSetting, Integer> {

    @Nullable
    @Query("SELECT s FROM UserSetting s WHERE s.user.id = :idUser")
    UserSetting getForUser(@Param("idUser") final int idUser);
}
