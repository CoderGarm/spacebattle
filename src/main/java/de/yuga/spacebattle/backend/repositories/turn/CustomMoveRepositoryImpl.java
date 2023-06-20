package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.turn.Move;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomMoveRepositoryImpl implements CustomMoveRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Move> findAllMoves() {
        final List<Move> resultList = em.createNamedQuery("Move.getAll", Move.class).getResultList();
        return resultList;
    }
}
