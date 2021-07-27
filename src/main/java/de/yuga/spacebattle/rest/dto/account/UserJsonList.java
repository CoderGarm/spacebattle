package de.yuga.spacebattle.rest.dto.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.SpacebattleApplication;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ridicules representation of a list for a user json - just for the fucked up swagger code gen.
 * Think about registering new classes in {@link SpacebattleApplication#api()}.
 */
public class UserJsonList extends ArrayList<UserJson> {

    public UserJsonList(@Nonnull final List<User> userList) {
        Preconditions.checkNotNull(userList, "userList shouldn't be null!");

        final List<UserJson> transformedUsers = userList.stream().map(UserJson::new).collect(Collectors.toList());
        addAll(transformedUsers);
    }
}
