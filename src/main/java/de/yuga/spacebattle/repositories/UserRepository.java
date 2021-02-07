package de.yuga.spacebattle.repositories;

import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.repositories.impl.CustomUserRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>, CustomUserRepository {
}
