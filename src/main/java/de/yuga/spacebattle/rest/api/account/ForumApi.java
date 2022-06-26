package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.forum.IdToId;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import de.yuga.spacebattle.rest.dto.account.forum.*;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
public class ForumApi {

    @Nonnull
    public static final String ENDPOINT = "forum";

    private static final String FORUMS_FOR_USER = "forumsForUser";
    private static final String ALLIANCE_FORUMS_FOR_USER = "allianceForumsForUser";
    private static final String FORUM_THREAD = "threadById";
    private static final String FORUM_THREAD_COUNT = "threadById/count";
    private static final String CREATE_FORUM_THREAD = "createThread";
    private static final String CREATE_FORUM_THREAD_MESSAGE = "createThreadMessage";
    private static final String BY_FORUM = "byForum";

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final JwtTokenUtil tokenUtil;

    @Autowired
    public ForumApi(@Nonnull final ForumService forumService,
                    @Nonnull final UserService userService,
                    @Nonnull final JwtTokenUtil tokenUtil) {
        Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(tokenUtil, "tokenUtil shouldn't be null!");

        this.forumService = forumService;
        this.userService = userService;
        this.tokenUtil = tokenUtil;
    }

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
    public ResponseEntity<?> getForumsForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token) {

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
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

    @GetMapping(ALLIANCE_FORUMS_FOR_USER)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getAllianceForumForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Forum.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllianceForumForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token) {

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
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

    @GetMapping(FORUM_THREAD + "/{idForumThread}")
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getForumThreadById",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForumThread.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getForumThreadById(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                                @PathVariable("idForumThread") final int idForumThread) {

        final Integer idUser = tokenUtil.getIdUserFromAccessToken(token);
        PreconditionWebHelper.checkNotNull(idUser, "idUser shouldn't be null!");

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
    public ResponseEntity<?> getForumThreadsByForumId(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                                      @PathVariable("idForum") final int idForum) {

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
        final de.yuga.spacebattle.backend.entities.account.forum.Forum forumById = forumService.findForumById(idForum);
        validateAccessToForum(idUser, forumById);

        final List<IdToId> threadIdToForum = forumService.findAllIdForumThreadForIdForums(List.of(idForum));
        final Map<Integer, List<Integer>> idThreadByIdForum = threadIdToForum.stream()
                .collect(Collectors.groupingBy(IdToId::getIdSelector,
                        Collectors.mapping(IdToId::getIdPayload, Collectors.toList())));
        final List<Integer> idForumThreads = idThreadByIdForum.getOrDefault(idForum, new ArrayList<>());
        final List<de.yuga.spacebattle.backend.entities.account.forum.ForumThread> forumThreads = forumService.findForumThreads(idForumThreads);

        final List<ForumThread> result = forumThreads.stream().map(forumThread -> {
            final List<IdToId> messageIdToIds = forumService.getMessageIdsForThread(forumThread.getId());
            final List<Integer> messageIds = messageIdToIds.stream().map(IdToId::getIdPayload).collect(Collectors.toList());
            final ForumThread thread = new ForumThread(forumThread);
            thread.enrichMessageIds(messageIds);
            return thread;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping(FORUM_THREAD + "/{idForumThread}/{page}/{size}")
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "getMessagesInThread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = ForumMessage.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMessagesInThread(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                                 @PathVariable("idForumThread") final int idForumThread,
                                                 @PathVariable("page") final int page,
                                                 @PathVariable("size") final int size) {

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);

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
        return ResponseEntity.ok(forumMessages);
    }

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

    @PutMapping(value = CREATE_FORUM_THREAD, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "createForumThread",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForumThread.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createForumThread(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                               @RequestBody @Nonnull final CreateForumThread createForumThread) {
        PreconditionWebHelper.checkNotNull(createForumThread, "createForumThread shouldn't be null!");

        final Integer idUser = tokenUtil.getIdUserFromAccessToken(token);
        PreconditionWebHelper.checkNotNull(idUser, "idUser shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.account.forum.Forum forum = forumService.findForumById(createForumThread.getIdForum());
        final User user = validateAccessToForum(idUser, forum);

        assert forum != null : "asserted in validation";
        final de.yuga.spacebattle.backend.entities.account.forum.ForumThread forumThread = forumService.save(new de.yuga.spacebattle.backend.entities.account.forum.ForumThread(forum, createForumThread));
        final String firstMessage = createForumThread.getFirstMessage();
        if (StringUtils.isNotBlank(firstMessage)) {
            forumService.save(new de.yuga.spacebattle.backend.entities.account.forum.ForumMessage(forumThread, user, firstMessage));
        }
        return ResponseEntity.ok(new ForumThread(forumThread));
    }

    @PutMapping(value = CREATE_FORUM_THREAD_MESSAGE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a list of forums which the given user is allowed to access.", operationId = "createThreadMessage",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createThreadMessage(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                                 @RequestBody @Nonnull final CreateForumThreadMessage threadMessage) {
        PreconditionWebHelper.checkNotNull(threadMessage, "threadMessage shouldn't be null!");

        final Integer idUser = tokenUtil.getIdUserFromAccessToken(token);
        PreconditionWebHelper.checkNotNull(idUser, "idUser shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.account.forum.ForumThread forumThread = forumService.findForumThread(threadMessage.getIdForumThread());
        PreconditionWebHelper.checkNotNull(forumThread, "forumThread shouldn't be null!");
        final User user = validateAccessToForum(idUser, forumThread.getForum());

        forumService.save(new de.yuga.spacebattle.backend.entities.account.forum.ForumMessage(forumThread, user, threadMessage.getMessage()));
        return ResponseEntity.ok(true);
    }
}
