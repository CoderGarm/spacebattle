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
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.*;

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
            return em.createNamedQuery("User.login", User.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
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
                    .setParameter("username", username)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public User findWithResearchesAndJobs(final int idUser) {
        return em.createNamedQuery("User.getWithResearchesAndJobs", User.class)
                .setParameter("idUser", idUser)
                .getSingleResult();
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
    public Map<Research, Integer> getResearchesForUser(final int idUser) {
        try {
            return em.createNamedQuery("User.getWithResearches", User.class)
                    .setParameter("idUser", idUser)
                    .getSingleResult().getResearches();
        } catch (final NoResultException e) {
            return new HashMap<>();
        }
    }

    @Nullable
    @Override
    public User getWithResearches(final int idUser) {
        try {
            return em.createNamedQuery("User.getWithResearches", User.class)
                    .setParameter("idUser", idUser)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
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
    public Set<Colonization> getColonizations(@Nonnull User user) {
        return em.createNamedQuery("User.getColonizations", User.class)
                .setParameter("user", user)
                .getSingleResult().getColonizations();
    }

    @Override
    public boolean isResearchUnlocked(@Nonnull User user, @Nonnull Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        // cannot execute namedNative query because spring fuck up
        final Query nativeQuery = em.createNativeQuery("SELECT COUNT(ur.idResearch) FROM unlockedResearch ur WHERE ur.idUser = :idUser AND ur.idResearch = :idResearch");
        final Object singleResult = nativeQuery.setParameter("idUser", user.getId())
                .setParameter("idResearch", research.getId())
                .getSingleResult();
        return ((BigInteger) singleResult).intValue() > 0;
    }

    @Override
    public int getLevelForResearch(@Nonnull final User user, @Nonnull final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        // cannot execute namedNative query because spring fuck up
        final Query nativeQuery = em.createNativeQuery("SELECT ur.level FROM unlockedResearch ur WHERE ur.idUser = :idUser AND ur.idResearch = :idResearch");
        final Object singleResult;
        try {
            singleResult = nativeQuery.setParameter("idUser", user.getId())
                    .setParameter("idResearch", research.getId())
                    .getSingleResult();
        } catch (final NoResultException e) {
            return 0;
        }
        return ((BigInteger) singleResult).intValue();
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
