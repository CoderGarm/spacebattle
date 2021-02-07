package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Module;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomModuleRepositoryImpl implements CustomModuleRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Module> findAllModules() {
        final List<Module> resultList = em.createNamedQuery("Module.getAll", Module.class).getResultList();
        return resultList;
    }
}
