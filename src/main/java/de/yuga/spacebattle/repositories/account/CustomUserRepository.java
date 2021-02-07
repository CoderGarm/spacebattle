package de.yuga.spacebattle.repositories.account;

import de.yuga.spacebattle.entities.account.User;

import java.util.List;

public interface CustomUserRepository {

    List<User> findAllUsers();
}
