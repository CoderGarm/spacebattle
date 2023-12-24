INSERT INTO forum (idForum, createdAt, description, role, title, idAlliance)
    VALUES (null,
        now(),
        'Everything around game events',
        'USER',
        'Event Forum',
         null);

INSERT INTO forumThread (idForumThread, createdAt, description, lastChanged, title, idForum)
    VALUES (null,
        now(),
        'For suggestions, criticism and everything that doesn\'t fit anywhere else',
        now(),
        'Game Events - general thread', (select idForum from forum where title = 'Event Forum'));


INSERT INTO forumMessage (idForumMessage, message, sentAt, idUserAuthor, idForumThread)
    VALUES (null, 'Moin,

on various occasions in the past I was told that game events would be appreciated.
**Here we are!**

We have made great progress in the game this year.
We will get a release shortly before the end of the year that will significantly advance the immersion in the Honorverse.

And I want to celebrate that with you in the form of the upcoming event!

<img src="https://media.battleforhonor.de/unrelated/happy-leo.webp" width="480" height="204"/>',
        now(), 1,
        (select idForumThread from forumThread where title = 'Game Events - general thread'));
