package de.yuga.spacebattle.repositories.account;

import de.yuga.spacebattle.entities.account.User;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> findAllUsers() {
        final List<User> resultList = em.createNamedQuery("User.getAll", User.class).getResultList();
        return resultList;
    }
}
