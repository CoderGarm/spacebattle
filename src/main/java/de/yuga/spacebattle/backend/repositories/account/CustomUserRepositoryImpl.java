package de.yuga.spacebattle.backend.repositories.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<User> findAllUsers() {
        return em.createNamedQuery("User.getAll", User.class).getResultList();
    }

    @Nullable
    @Override
    public User findByUsernameAndEmail(@Nonnull final String username, @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        try {
            return em.createNamedQuery("User.findByUsernameAndEmail", User.class)
                    .setParameter("username", username)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public User findWithKnownStarSystems(final int idUser) {
        return em.createNamedQuery("User.getWithKnownStarSystems", User.class)
                .setParameter("idUser", idUser)
                .getSingleResult();
    }

    @Nonnull
    @Override
    public Set<StarSystem> getKnownStarSystems(int idUser) {
        return em.createNamedQuery("User.getWithKnownStarSystems", User.class)
                .setParameter("idUser", idUser)
                .getSingleResult().getKnownStarSystems();
    }

    @Nonnull
    @Override
    public List<User> findLikeUsername(@Nullable final String username) {
        if (StringUtils.isEmpty(username)) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("User.findByLikeUsername", User.class)
                .setParameter("username", username + "%").getResultList();
    }

    @Override
    public boolean existsUsername(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        return !em.createNamedQuery("User.findByUsernameExact", Integer.class)
                .setParameter("username", username).getResultList().isEmpty();
    }

    @Override
    public boolean existsEMail(@Nonnull final String eMail) {
        Preconditions.checkNotNull(eMail, "eMail shouldn't be null!");

        return !em.createNamedQuery("User.findByEMailExact", Integer.class)
                .setParameter("email", eMail).getResultList().isEmpty();
    }

    @Nullable
    @Override
    public User findByUsername(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        try {
            return em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username.toUpperCase()).getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }
}
