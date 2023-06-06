package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.chat.MessageThread;
import de.yuga.spacebattle.backend.services.account.ChatService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.BaseApi;
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
public class ChatApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "chat";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ChatService chatService;

    @Autowired
    public ChatApi(@Nonnull final UserService userService,
                   @Nonnull final ChatService chatService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(chatService, "messageThreadService shouldn't be null!");

        this.userService = userService;
        this.chatService = chatService;
    }

    @GetMapping(value = "{idUser}")
    @Operation(summary = "Get the chat history of the users.", operationId = "getChatByUsers",
            description = "Get the chat between the users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ChatHistory.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getChatByUsers(@PathVariable("idUser") final int idUser) {

        final int idUserByToken = getIdUser();
        final MessageThread messagesBetween = chatService.findMessagesBetween(idUserByToken, idUser);
        if (messagesBetween != null) {
            return ResponseEntity.ok(new ChatHistory(messagesBetween));
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
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
    public ResponseEntity<?> getChatByUser() {

        final int idUserByToken = getIdUser();
        final List<MessageThread> threadsWithUser = chatService.findThreadsWithUser(idUserByToken);
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

        final int idUserByToken = getIdUser();

        final ChatMessage chatMessage = messages.get(0);
        final String message = chatMessage.getMessage();

        //noinspection ConstantConditions
        final int idUserSender = chatMessage.getSender().getIdUser();
        //noinspection ConstantConditions
        final int idUserOne = chatHistory.getUserOne().getIdUser();
        //noinspection ConstantConditions
        final int idUserTwo = chatHistory.getUserTwo().getIdUser();
        // detect the sender
        final int idReceiver = idUserSender != idUserOne ? idUserOne : idUserTwo;

        if (idUserByToken != idUserSender && idUserByToken != idReceiver) {
            throw new NotifyWebUserException("You should not pretend to be someone other!");
        }

        final User sender = userService.find(idUserSender);
        final User receiver = userService.find(idReceiver);

        //noinspection ConstantConditions
        final MessageThread messageThread = chatService.createChatMessage(sender, receiver, message);
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

        final int idUserByToken = getIdUser();

        final String chatMessage = message.getMessage();
        final UserJson senderJ = message.getSender();
        final Integer idUserMessage = message.getIdUserMessage();
        if (idUserMessage == null) {
            throw new NotifyWebUserException("You should try to send the first message another way - please contact the administrator!");
        }
        final int idUserSender = senderJ.getIdUser();
        if (idUserByToken != idUserSender) {
            throw new NotifyWebUserException("You should not pretend to be someone other if you try to write a message!");
        }
        final User sender = userService.find(idUserSender);
        final MessageThread messageThread = chatService.sendChatMessage(idUserMessage, sender, chatMessage);
        if (messageThread != null) {
            return ResponseEntity.ok(new ChatHistory(messageThread));
        } else {
            throw new NotifyWebUserException("An error has occurred while sending a message - please contact the administrator!");
        }
    }

    @GetMapping("/hasUnread/{idChatHistory}")
    @Operation(summary = "Returns if the chat has unread messages.", operationId = "hasUnread",
            description = "Returns if the chat has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasUnread(
            @PathVariable("idChatHistory") final int idChatHistory) {
        final int idReceiver = getIdUser();
        final boolean hasUnread = chatService.hasUnreadMessages(idReceiver, idChatHistory);
        return ResponseEntity.ok(hasUnread);
    }

    @GetMapping("/hasUserUnread")
    @Operation(summary = "Returns if the user has unread messages.", operationId = "hasUserUnread",
            description = "Returns if the user has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasUserUnread() {
        final int idUser = getIdUser();
        final boolean hasUnread = chatService.hasUserUnreadMessages(idUser);
        return ResponseEntity.ok(hasUnread);
    }

    @PutMapping("/markMessageRead/{idChatMessage}")
    @Operation(summary = "Creates a chat message", operationId = "markMessageRead",
            description = "Creates a chat message",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> markMessageRead(@PathVariable("idChatMessage") final int idUserMessage) {

        final int idUser = getIdUser();

        chatService.markMessageReadIfForUser(idUserMessage, idUser);
        return ResponseEntity.ok(true);
    }
}
