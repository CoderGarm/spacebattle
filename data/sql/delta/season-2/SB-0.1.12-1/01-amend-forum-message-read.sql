ALTER TABLE forumMessageRead ADD COLUMN isRead boolean NOT NULL DEFAULT FALSE AFTER idForumMessageRead;

create table forumMessageReadTEMP (
        idForumMessage integer not null,
        idUser integer not null
    ) engine=InnoDB;

insert INTO forumMessageReadTEMP SELECT idForumMessage, idUser from forumMessageRead;

# noinspection SqlWithoutWhere
delete from forumMessageRead;

DELIMITER |
    CREATE PROCEDURE createInitialReads()
    BEGIN
        DECLARE n INT DEFAULT 0;
        DECLARE i INT DEFAULT 0;
        SELECT COUNT(*) FROM user INTO n;
        SET i = 0;
        WHILE i < n
            DO
            select u.idUser, u.dType, u.userRole, u.idAlliance from user u order by u.idUser offset i rows fetch next 1 row only into @idUser, @dType, @userRole, @idAlliance;

            #@formatter:off
            if @dType ='USER' then
                    INSERT INTO forumMessageRead (idUser, idForum, idForumThread, idForumMessage, isRead)
                    SELECT @idUser, forumThread.idForum , forumThread.idForumThread, forumMessage.idForumMessage,
                    (SELECT IF((COUNT(x.idUser) > 0), TRUE, FALSE) FROM forumMessageReadTEMP x WHERE x.idForumMessage = forumMessage.idForumMessage AND x.idUser = @idUser)
                    FROM forumMessage
                             LEFT JOIN forumThread ON forumThread.idForumThread = forumMessage.idForumThread
                             LEFT JOIN forum on forum.idForum = forumThread.idForum
                                WHERE (forum.role IS NULL OR @userRole LIKE CONCAT('%', forum.role, '%'))
                                    OR (forum.idAlliance IS NULL OR @idAlliance = forum.idAlliance);
            end if;
            #@formatter:on


            SET i = i + 1;
            END WHILE;
    End |
    DELIMITER ;

    call createInitialReads();
    drop procedure createInitialReads;

DELIMITER //
CREATE TRIGGER forum_message_read_after_insert_user_trigger
    AFTER INSERT
    ON user
    FOR EACH ROW
BEGIN
    -- Insert into forumMessageRead for every existing forumMessage
    INSERT INTO forumMessageRead (idUser, idForum, idForumThread, idForumMessage)
    SELECT NEW.idUser, forumThread.idForum , forumThread.idForumThread, forumMessage.idForumMessage
    FROM forumMessage
             LEFT JOIN forumThread ON forumThread.idForumThread = forumMessage.idForumThread
             LEFT JOIN forum on forum.idForum = forumThread.idForum
                WHERE (forum.role IS NULL OR NEW.userRole LIKE CONCAT('%', forum.role, '%'))
                    OR (forum.idAlliance IS NULL OR NEW.idAlliance = forum.idAlliance);
END;
//
DELIMITER ;

DELIMITER //
CREATE TRIGGER forum_message_read_after_insert_forumMessage
AFTER INSERT ON forumMessage
FOR EACH ROW
BEGIN
    -- Insert into forumMessageRead for every existing user
    INSERT INTO forumMessageRead (idUser, idForum, idForumThread, idForumMessage)
    SELECT user.idUser, forumThread.idForum , forumThread.idForumThread, NEW.idForumMessage
    FROM user
        LEFT JOIN forumMessage ON forumMessage.idForumMessage = NEW.idForumMessage
        LEFT JOIN forumThread ON forumThread.idForumThread = forumMessage.idForumThread
        LEFT JOIN forum on forum.idForum = forumThread.idForum
                WHERE (forum.role IS NULL OR user.userRole LIKE CONCAT('%', forum.role, '%'))
                    OR (forum.idAlliance IS NULL OR user.idAlliance = forum.idAlliance);
END;
//
DELIMITER ;

INSERT INTO dbPatch VALUES (NULL, NOW(), 'add forum message trigger', '0.1.12-1');
