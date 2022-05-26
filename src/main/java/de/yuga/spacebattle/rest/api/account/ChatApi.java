package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.MessageThread;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.MessageThreadService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.account.chat.ChatHistory;
import de.yuga.spacebattle.rest.dto.account.chat.ChatMessage;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ChatApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ChatApi.ENDPOINT + "/")
public class ChatApi {

    @Nonnull
    public static final String ENDPOINT = "chat";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final MessageThreadService messageThreadService;

    @Autowired
    public ChatApi(@Nonnull final UserService userService,
                   @Nonnull final MessageThreadService messageThreadService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(messageThreadService, "messageThreadService shouldn't be null!");

        this.userService = userService;
        this.messageThreadService = messageThreadService;
    }

    @GetMapping(value = "{idUserOne}/{idUserTwo}")
    @Operation(summary = "Get the chat history of the users and marks them as read", operationId = "getChatByUsers",
            description = "Get the chat between the users and marks them as read",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChatHistory.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getChatByUsers(@PathVariable("idUserOne") final int idUserOne, @PathVariable("idUserTwo") final int idUserTwo) {

        final MessageThread messagesBetween = messageThreadService.findMessagesBetween(idUserOne, idUserTwo);
        if (messagesBetween != null) {
            return ResponseEntity.ok(new ChatHistory(messagesBetween));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "{idUser}")
    @Operation(summary = "Get all active chats of the user without the messages", operationId = "getChatByUser",
            description = "Get all active chats of the user without the messages",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ChatHistory.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getChatByUsers(@PathVariable("idUser") final int idUser) {

        final List<MessageThread> threadsWithUser = messageThreadService.findThreadsWithUser(idUser);
        if (!threadsWithUser.isEmpty()) {
            return ResponseEntity.ok(threadsWithUser.stream().map(ChatHistory::new).collect(Collectors.toList()));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/createMessageThread")
    @Operation(summary = "Creates a chat message thread", operationId = "createChatMessageThread",
            description = "Creates a chat message thread",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatHistory.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChatHistory.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createMessageThread(@RequestBody @Nonnull final ChatHistory chatHistory) {
        Preconditions.checkNotNull(chatHistory, "chatHistory shouldn't be null!");

        final List<ChatMessage> messages = chatHistory.getMessages();
        if (messages.size() != 1) {
            throw new NotifyWebUserException("This is not possible - there is already an active chat.");
        }
        final ChatMessage chatMessage = messages.get(0);
        final String message = chatMessage.getMessage();

        final int idUserSender = chatMessage.getSender().getIdUser();
        final int idUserOne = chatHistory.getUserOne().getIdUser();
        final int idUserTwo = chatHistory.getUserTwo().getIdUser();
        // detect the sender
        final int idReceiver = idUserSender != idUserOne ? idUserOne : idUserTwo;

        final User sender = userService.find(idUserSender);
        final User receiver = userService.find(idReceiver);

        final MessageThread messageThread = messageThreadService.createChatMessage(sender, receiver, message);
        return ResponseEntity.ok(new ChatHistory(messageThread));
    }

    @PutMapping("/sendMessage")
    @Operation(summary = "Creates a chat message", operationId = "sendChatMessage",
            description = "Creates a chat message",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChatMessage.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChatHistory.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> sendChatMessage(@RequestBody @Nonnull final ChatMessage message) {
        Preconditions.checkNotNull(message, "message shouldn't be null!");

        final String chatMessage = message.getMessage();
        final UserJson senderJ = message.getSender();
        final Integer idUserMessage = message.getIdUserMessage();
        if (idUserMessage == null) {
            throw new NotifyWebUserException("You should try to send the first message another way - please contact the administrator!");
        }
        final User sender = userService.find(senderJ.getIdUser());
        final MessageThread messageThread = messageThreadService.sendChatMessage(idUserMessage, sender, chatMessage);
        if (messageThread != null) {
            return ResponseEntity.ok(new ChatHistory(messageThread));
        } else {
            throw new NotifyWebUserException("An error has occurred while sending a message - please contact the administrator!");
        }
    }
}
