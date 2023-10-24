package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.role.AllowedRoles;
import de.yuga.spacebattle.rest.dto.account.forum.*;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "ForumApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + ForumApi.ENDPOINT + "/")
public class ForumApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "forum";

    private static final String FORUMS_FOR_USER = "forumsForUser";
    private static final String ALLIANCE_FORUMS_FOR_USER = "allianceForumsForUser";
    private static final String FORUM_THREAD = "threadById";
    private static final String FORUM_THREAD_COUNT = "threadById/count";
    private static final String CREATE_FORUM_THREAD = "createThread";
    private static final String CREATE_FORUM_THREAD_MESSAGE = "createThreadMessage";
    private static final String EDIT_FORUM_THREAD_MESSAGE = "editThreadMessage";
    private static final String BY_FORUM = "byForum";
    public static final String MARK_FORUM_MESSAGE_READ_ENDPOINT = "markForumMessageRead";

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public ForumApi(@Nonnull final ForumService forumService,
                    @Nonnull final UserService userService) {
        Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.forumService = forumService;
        this.userService = userService;
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(FORUMS_FOR_USER)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getForumsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Forum.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getForumsForUser() {

        final int idUser = getIdUser();
        final User user = userService.find(idUser);
        assert user != null;
        final List<de.yuga.spacebattle.backend.entities.account.forum.Forum> forAllowedForUser = forumService.findForumsAllowedForUser(user);

        final List<Forum> forums = forAllowedForUser.stream().map(Forum::new).collect(Collectors.toList());
        final List<Integer> idForums = forums.stream().map(Forum::getIdForum).collect(Collectors.toList());
        final List<IdToId> threadIdToForum = forumService.findAllIdForumThreadForIdForums(idForums);

        final Map<Integer, List<Integer>> idThreadByIdForum = threadIdToForum.stream()
                .collect(Collectors.groupingBy(IdToId::getIdSelector,
                        Collectors.mapping(IdToId::getIdPayload, Collectors.toList())));

        forums.forEach(forum -> {
            final int idForum = forum.getIdForum();
            final List<Integer> idForumThreads = idThreadByIdForum.getOrDefault(idForum, new ArrayList<>());
            forum.enrichForumThreads(idForumThreads);
        });

        return ResponseEntity.ok(forums);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(ALLIANCE_FORUMS_FOR_USER)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getAllianceForumForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Forum.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllianceForumForUser() {

        final int idUser = getIdUser();
        final User user = userService.find(idUser);
        assert user != null;
        final Alliance alliance = user.getAlliance();
        if (alliance == null) {
            return ResponseEntity.ok().build();
        }

        final de.yuga.spacebattle.backend.entities.account.forum.Forum forAllowedForUser = forumService.getAllianceForumForUser(alliance);
        final List<IdToId> threadIdToForum = forumService.findAllIdForumThreadForIdForums(List.of(forAllowedForUser.getId()));
        final Map<Integer, List<Integer>> idThreadByIdForum = threadIdToForum.stream()
                .collect(Collectors.groupingBy(IdToId::getIdSelector,
                        Collectors.mapping(IdToId::getIdPayload, Collectors.toList())));

        final Forum forum = new Forum(forAllowedForUser);
        final int idForum = forum.getIdForum();
        final List<Integer> idForumThreads = idThreadByIdForum.getOrDefault(idForum, new ArrayList<>());
        forum.enrichForumThreads(idForumThreads);

        return ResponseEntity.ok(forum);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(FORUM_THREAD + "/{idForumThread}")
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getForumThreadById",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForumThread.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getForumThreadById(@PathVariable("idForumThread") final int idForumThread) {

        final int idUser = getIdUser();

        final de.yuga.spacebattle.backend.entities.account.forum.ForumThread forumThread = forumService.findForumThread(idForumThread);
        if (forumThread != null) {
            validateAccessToForum(idUser, forumThread.getForum());
            final List<IdToId> messageIdToIds = forumService.getMessageIdsForThread(idForumThread);
            final List<Integer> messageIds = messageIdToIds.stream().map(IdToId::getIdPayload).collect(Collectors.toList());
            final ForumThread thread = new ForumThread(forumThread);
            thread.enrichMessageIds(messageIds);
            return ResponseEntity.ok(thread);
        }
        return ResponseEntity.ok().build();
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(FORUM_THREAD + "/" + BY_FORUM + "/{idForum}")
    @Operation(summary = "Get a list of threads in a forum which the given user is allowed to access.", operationId = "getForumThreadsByForumId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ForumThread.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getForumThreadsByForumId(@PathVariable("idForum") final int idForum) {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.account.forum.Forum forumById = forumService.findForumById(idForum);
        validateAccessToForum(idUser, forumById);

        final List<IdToId> threadIdToForum = forumService.findAllIdForumThreadForIdForums(List.of(idForum));
        final Map<Integer, List<Integer>> idThreadByIdForum = threadIdToForum.stream()
                .collect(Collectors.groupingBy(IdToId::getIdSelector,
                        Collectors.mapping(IdToId::getIdPayload, Collectors.toList())));
        final List<Integer> idForumThreads = idThreadByIdForum.getOrDefault(idForum, new ArrayList<>());
        final List<de.yuga.spacebattle.backend.entities.account.forum.ForumThread> forumThreads = forumService.findForumThreads(idForumThreads);

        final List<ForumThread> result = forumThreads.stream()
                .sorted(Comparator.comparing(de.yuga.spacebattle.backend.entities.account.forum.ForumThread::getLastChanged).reversed())
                .map(forumThread -> {
                    final List<IdToId> messageIdToIds = forumService.getMessageIdsForThread(forumThread.getId());
                    final List<Integer> messageIds = messageIdToIds.stream().map(IdToId::getIdPayload).collect(Collectors.toList());
                    final ForumThread thread = new ForumThread(forumThread);
                    thread.enrichMessageIds(messageIds);
                    return thread;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(FORUM_THREAD + "/{idForumThread}/{page}/{size}")
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getMessagesInThread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForumMessageContainer.class)))
                    ,
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMessagesInThread(@PathVariable("idForumThread") final int idForumThread,
                                                 @PathVariable("page") int page,
                                                 @PathVariable("size") final int size) {

        final int idUser = getIdUser();

        if (page == 0) {
            // fetch unread messages page
            page = forumService.findPageWithFirstUnreadMessageInThread(idUser, idForumThread, size);
        }

        final List<de.yuga.spacebattle.backend.entities.account.forum.ForumMessage> messagesInForumThread = forumService.findMessagesInForumThread(idForumThread, page, size)
                .stream()
                .sorted(Comparator.comparing(de.yuga.spacebattle.backend.entities.account.forum.ForumMessage::getSentAt))
                .collect(Collectors.toList());

        if (messagesInForumThread.isEmpty()) {
            return ResponseEntity.ok().build();
        }

        final Set<de.yuga.spacebattle.backend.entities.account.forum.Forum> forums = messagesInForumThread.stream()
                .map(f -> f.getForumThread().getForum())
                .collect(Collectors.toSet());

        if (forums.size() != 1) {
            throw new NotifyWebUserException("Good game, you want to read more than one thread simultaneously.");
        }
        final de.yuga.spacebattle.backend.entities.account.forum.Forum forum = new ArrayList<>(forums).get(0);
        validateAccessToForum(idUser, forum);

        final List<ForumMessage> forumMessages = messagesInForumThread.stream()
                .map(ForumMessage::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ForumMessageContainer(page, forumMessages));
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping(FORUM_THREAD_COUNT + "/{idForumThread}")
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "countMessagesInThread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> countMessagesInThread(@PathVariable("idForumThread") final int idForumThread) {

        final Integer amount = forumService.countMessagesInForumThread(idForumThread);
        return ResponseEntity.ok(amount);
    }

    @Nonnull
    private User validateAccessToForum(@Nullable final Integer idUser, @Nullable final de.yuga.spacebattle.backend.entities.account.forum.Forum forum) {
        PreconditionWebHelper.checkNotNull(idUser, "The user id did not exist!");
        PreconditionWebHelper.checkNotNull(forum, "The forum did not exist!");

        final User user = userService.find(idUser);
        PreconditionWebHelper.checkNotNull(user, "The user did not exist!");

        final boolean userAllowed = forum.isUserAllowed(user);
        if (!userAllowed) {
            throw new NotifyWebUserException("You have no access to this forum.");
        }
        return user;
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_WRITE)
    @PutMapping(value = CREATE_FORUM_THREAD, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "createForumThread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForumThread.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createForumThread(@RequestBody @Nonnull final CreateForumThread createForumThread) {
        PreconditionWebHelper.checkNotNull(createForumThread, "createForumThread shouldn't be null!");

        final int idUser = getIdUser();

        final de.yuga.spacebattle.backend.entities.account.forum.Forum forum = forumService.findForumById(createForumThread.getIdForum());
        final User user = validateAccessToForum(idUser, forum);

        assert forum != null : "asserted in validation";
        final de.yuga.spacebattle.backend.entities.account.forum.ForumThread forumThread = forumService.save(new de.yuga.spacebattle.backend.entities.account.forum.ForumThread(forum, createForumThread));
        final String firstMessage = createForumThread.getFirstMessage();
        if (StringUtils.isNotBlank(firstMessage)) {
            forumService.createForumMessage(forumThread, user, firstMessage);
        }
        return ResponseEntity.ok(new ForumThread(forumThread));
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_WRITE)
    @PutMapping(value = CREATE_FORUM_THREAD_MESSAGE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "createThreadMessage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createThreadMessage(@RequestBody @Nonnull final CreateForumThreadMessage threadMessage) {
        PreconditionWebHelper.checkNotNull(threadMessage, "threadMessage shouldn't be null!");

        final int idUser = getIdUser();

        final de.yuga.spacebattle.backend.entities.account.forum.ForumThread forumThread = forumService.findForumThread(threadMessage.getIdForumThread());
        PreconditionWebHelper.checkNotNull(forumThread, "forumThread shouldn't be null!");
        final User user = validateAccessToForum(idUser, forumThread.getForum());

        forumService.createForumMessage(forumThread, user, threadMessage.getMessage());
        return ResponseEntity.ok(true);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_WRITE)
    @PostMapping(value = EDIT_FORUM_THREAD_MESSAGE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "editThreadMessage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> editThreadMessage(@RequestBody @Nonnull final ForumMessage threadMessage) {
        PreconditionWebHelper.checkNotNull(threadMessage, "threadMessage shouldn't be null!");

        final int idUser = getIdUser();

        final de.yuga.spacebattle.backend.entities.account.forum.ForumMessage msg = forumService.findMessage(threadMessage.getIdForumMessage());
        PreconditionWebHelper.checkNotNull(msg, "msg shouldn't be null!");
        validateAccessToForum(idUser, msg.getForumThread().getForum());
        if (msg.getAuthor().getId() != idUser) {
            throw new NotifyWebUserException("Yes but no!");
        }
        msg.setMessage(threadMessage.getMessage());

        forumService.save(msg);
        return ResponseEntity.ok(true);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping("/hasThreadUnread/{idForumThread}")
    @Operation(summary = "Returns if the chat has unread messages.", operationId = "hasThreadUnread",
            description = "Returns if the chat has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasThreadUnread(@PathVariable("idForumThread") final int idForumThread) {
        final boolean hasUnread = forumService.hasThreadUnread(idForumThread, getIdUser());
        return ResponseEntity.ok(hasUnread);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping("/hasForumUnread/{idForum}")
    @Operation(summary = "Returns if the chat has unread messages.", operationId = "hasForumUnread",
            description = "Returns if the chat has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasForumUnread(@PathVariable("idForum") final int idForum) {
        final boolean hasUnread = forumService.hasForumUnread(idForum, getIdUser());
        return ResponseEntity.ok(hasUnread);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping("/hasUserUnreadMessages")
    @Operation(summary = "Returns if the chat has unread messages.", operationId = "hasUserUnreadMessages",
            description = "Returns if the chat has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasUserUnreadMessages() {
        final boolean hasUnread = forumService.hasUserUnread(getIdUser());
        return ResponseEntity.ok(hasUnread);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @GetMapping("/hasUnreadMessages/{idThread}")
    @Operation(summary = "Returns if the chat has unread messages.", operationId = "getUnreadMessages",
            description = "Returns if the chat has unread messages.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Integer.class)
                            ))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getUnreadMessages(@PathVariable("idThread") final int idThread) {
        final List<Integer> hasUnread = forumService.findUnreadMessages(idThread, getIdUser());
        return ResponseEntity.ok(hasUnread);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_READ)
    @PutMapping(MARK_FORUM_MESSAGE_READ_ENDPOINT)
    @Operation(summary = "Marks a message or thread or forum as read.", operationId = "markForumMessageRead",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ForumIdContainer.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> markForumMessageRead(@RequestBody @Nonnull final ForumIdContainer container) {

        final int idUser = getIdUser();
        final Integer idForum = container.getIdForum();
        final Integer idForumThread = container.getIdThread();
        final Integer idForumMessage = container.getIdMessage();
        forumService.markMessageRead(idForum, idForumThread, idForumMessage, idUser);
        return ResponseEntity.ok(true);
    }

    @AllowedRoles(roles = EGameUserRole.FORUM_WRITE)
    @RolesAllowed("ADMIN")
    @PutMapping("/distributeRelease/{idForumThread}")
    @Operation(summary = "Creates a chat message", operationId = "distributeRelease",
            description = "Creates a chat message",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> distributeRelease(@PathVariable("idForumThread") final int idForumThread) {

        final Set<String> recipients = userService.findReleaseRecipients();
        forumService.sendRelease(recipients, idForumThread);
        return ResponseEntity.ok(true);
    }
}
