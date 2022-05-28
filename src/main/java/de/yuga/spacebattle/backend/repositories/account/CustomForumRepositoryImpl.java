package de.yuga.spacebattle.backend.repositories.account;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class CustomForumRepositoryImpl implements CustomForumRepository {

    @PersistenceContext
    private EntityManager em;

}
