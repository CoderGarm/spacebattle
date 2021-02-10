package de.yuga.spacebattle.backend.services.account;

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

    @Nullable
    private User login;

    @Autowired
    public UserService(@Nonnull final UserRepository userRepository) {
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");

        this.userRepository = userRepository;
    }

    @Nullable
    public User isLoggedIn() {
        return login;
    }

    public void setLogin(@Nullable final User login) {
        this.login = login;
    }

    @Nullable
    public User login(@Nonnull final String username, @Nonnull final String password) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        return userRepository.login(username, password);
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
                           @Nonnull final String email,
                           @Nonnull final ERaceType raceType) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");
        Preconditions.checkNotNull(raceType, "raceType shouldn't be null!");

        return userRepository.save(new User(username, password, email, raceType));
    }
}
