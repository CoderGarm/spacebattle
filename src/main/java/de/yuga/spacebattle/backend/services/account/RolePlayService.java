package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import de.yuga.spacebattle.backend.repositories.account.UserRolePlaySettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Service
public class RolePlayService {

    @Nonnull
    private final UserRolePlaySettingRepository rolePlaySettingRepository;

    @Autowired
    public RolePlayService(@Nonnull final UserRolePlaySettingRepository rolePlaySettingRepository) {
        this.rolePlaySettingRepository = Preconditions.checkNotNull(rolePlaySettingRepository, "rolePlaySettingRepository must not be empty");
    }

    @Nullable
    public RolePlaySetting findForUser(final int idUser) {
        return rolePlaySettingRepository.findForUser(idUser);
    }

    @Nonnull
    public RolePlaySetting save(@Nonnull final RolePlaySetting rpgSettings) {
        Preconditions.checkNotNull(rpgSettings, "rpgSettings must not be empty");

        return rolePlaySettingRepository.save(rpgSettings);
    }
}
