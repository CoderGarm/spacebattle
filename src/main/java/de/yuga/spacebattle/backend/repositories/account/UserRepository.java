package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>, CustomUserRepository {
}
