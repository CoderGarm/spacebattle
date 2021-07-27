package de.yuga.spacebattle.rest.dto.account.chat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.SpacebattleApplication;
import de.yuga.spacebattle.backend.entities.account.MessageThread;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ridicules representation of a list for a user json - just for the fucked up swagger code gen.
 * Think about registering new classes in {@link SpacebattleApplication#api()}.
 */
public class ChatHistoryList extends ArrayList<ChatHistory> {

    public ChatHistoryList(@Nonnull final List<MessageThread> messageThreads) {
        Preconditions.checkNotNull(messageThreads, "messageThreads shouldn't be null!");

        final List<ChatHistory> transformedUsers = messageThreads.stream().map(ChatHistory::new).collect(Collectors.toList());
        addAll(transformedUsers);
    }
}
