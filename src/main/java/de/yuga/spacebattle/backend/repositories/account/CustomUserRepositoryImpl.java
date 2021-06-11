package de.yuga.spacebattle.backend.repositories.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
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
    public User login(@Nonnull final String username, @Nonnull final String password) {
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

    @Nullable
    @Override
    public User findByUsernameAndEmail(@Nonnull final String username, @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        try {
            return em.createNamedQuery("User.findByUsernameAndEmail", User.class)
                    .setParameter("username", StringUtils.upperCase(username))
                    .setParameter("email", StringUtils.upperCase(email))
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public User findWithResearchesAndJobs(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("User.getWithResearchesAndJobs", User.class)
                .setParameter("user", user)
                .getSingleResult();
    }

    @Nonnull
    @Override
    public User findWithKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("User.getWithKnownStarSystems", User.class)
                .setParameter("user", user)
                .getSingleResult();
    }

    @Nonnull
    @Override
    public Map<Research, Integer> getResearchesForUser(@Nonnull User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("User.getWithResearches", User.class)
                .setParameter("user", user)
                .getSingleResult().getResearches();
    }

    @Nonnull
    @Override
    public Set<StarSystem> getKnownStarSystems(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("User.getWithKnownStarSystems", User.class)
                .setParameter("user", user)
                .getSingleResult().getKnownStarSystems();
    }

    @Nonnull
    @Override
    public Set<Colonization> getColonizations(@Nonnull User user) {
        return em.createNamedQuery("User.getColonizations", User.class)
                .setParameter("user", user)
                .getSingleResult().getColonizations();
    }
}
