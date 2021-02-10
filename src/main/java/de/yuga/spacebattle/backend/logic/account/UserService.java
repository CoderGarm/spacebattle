package de.yuga.spacebattle.backend.logic.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class UserService {

    @Nonnull
    private final UserRepository userRepository;

    @Autowired
    public UserService(@Nonnull final UserRepository userRepository) {
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");

        this.userRepository = userRepository;
    }

    @Nonnull
    public List<User> findAll() {
        return userRepository.findAllUsers();
    }

    @Nullable
    public User find(@Nonnull final Integer idUser) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");

        return userRepository.findById(idUser).orElse(null);
    }

    public User save(@Nonnull final User entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return userRepository.save(entity);
    }

    @Nonnull
    public User createUser(@Nonnull final String username,
                           @Nonnull final String password,
                           @Nonnull final ERaceType raceType) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(raceType, "raceType shouldn't be null!");

        return userRepository.save(new User(username, password, raceType));
    }
}
