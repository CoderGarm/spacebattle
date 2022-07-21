     create table forumMessageRead (
        idForumMessageRead integer not null auto_increment,
         idForum integer not null,
         idForumMessage integer not null,
         idForumThread integer not null,
         idUser integer not null,
         primary key (idForumMessageRead)
     ) engine=InnoDB;

     alter table forumMessageRead 
        add constraint FK12uxerbm5t8a7shn88fvvalbu 
        foreign key (idForum) 
        references forum (idForum);
 
     alter table forumMessageRead 
        add constraint FKnf5e4g437o3l3hdg2ei0ywwe0 
        foreign key (idForumMessage) 
        references forumMessage (idForumMessage);
 
     alter table forumMessageRead 
        add constraint FKtcsdm5ruogje2vjsy4oeok3md 
        foreign key (idForumThread) 
        references forumThread (idForumThread);
 
     alter table forumMessageRead 
        add constraint FK2xe08nytb3qnnfmf906ynapx6 
        foreign key (idUser) 
        references user (idUser);
