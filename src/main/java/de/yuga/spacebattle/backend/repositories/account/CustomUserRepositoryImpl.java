package de.yuga.spacebattle.backend.repositories.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<User> findAllUsers() {
        final List<User> resultList = em.createNamedQuery("User.getAll", User.class).getResultList();
        return resultList;
    }

    @Nullable
    @Override
    public User login(@Nonnull String username, @Nonnull String password) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        try {
            final User u = em.createNamedQuery("User.login", User.class)
                    .setParameter("username", StringUtils.upperCase(username))
                    .setParameter("password", StringUtils.upperCase(password))
                    .getSingleResult();
            return u;
        } catch (final NoResultException e) {
            return null;
        }
    }


}
