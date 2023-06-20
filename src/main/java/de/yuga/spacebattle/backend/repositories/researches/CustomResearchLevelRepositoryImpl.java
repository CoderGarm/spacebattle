package de.yuga.spacebattle.backend.repositories.researches;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class CustomResearchLevelRepositoryImpl implements CustomResearchLevelRepository {

    @PersistenceContext
    private EntityManager em;
}
