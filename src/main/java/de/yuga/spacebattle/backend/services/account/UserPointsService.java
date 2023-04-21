package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.entities.account.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class UserPointsService {

    @Nonnull
    private final UserService userRepository;

    @Autowired
    public UserPointsService(@Nonnull final UserService userRepository) {
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");

        this.userRepository = userRepository;
    }


    @Nonnull
    public UserPoints getPoints(final int idUser) {
        final User user = userRepository.find(idUser);
        Preconditions.checkNotNull(user, "user must not be empty");
        return new UserPoints(user);
    }
}
