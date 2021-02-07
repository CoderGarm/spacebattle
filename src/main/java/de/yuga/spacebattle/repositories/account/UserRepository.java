package de.yuga.spacebattle.repositories.account;

import de.yuga.spacebattle.entities.account.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>, CustomUserRepository {
}
