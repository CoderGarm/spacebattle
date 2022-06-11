package de.yuga.spacebattle.backend.repositories.researches;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class CustomResearchLevelRepositoryImpl implements CustomResearchLevelRepository {

    @PersistenceContext
    private EntityManager em;

}
