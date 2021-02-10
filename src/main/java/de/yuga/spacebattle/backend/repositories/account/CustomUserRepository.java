package de.yuga.spacebattle.backend.repositories.account;

import de.yuga.spacebattle.backend.entities.account.User;

import java.util.List;

public interface CustomUserRepository {

    List<User> findAllUsers();
}
