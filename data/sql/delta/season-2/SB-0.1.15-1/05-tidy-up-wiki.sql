
update articleLines set content = "Start with your first colony to _Explore_ the galaxy. _Exploit_ the resources of your star system and _Expand_ your empire."
where content = "Start with your first colony to _Explore_ the galaxy. _Exploit_ the resources of your star system and _Expand_ your";

update articleLines set content = "Besides the strong recommendation to read the opera it could be useful but is not necessary to understand the principles of the honorverse."
where content = "Besides the strong recommendation to read the opera it could be useful but is not necessary to understand the principles";

update articleLines set content = "Battle for Honor is tick-based and a tick starts and ends at midnight - obviously not at the same day."
where content = "Battle for Honor is tick-based and a tick starts and ends at midnight - obviously not at the same days.";

delete from articleLines where idArticleRevision in (select idArticleRevision from articleRevision where version > 1);
delete from article_articleRevisions where article_articleRevisions.articleRevisions_idArticleRevision in (select idArticleRevision from articleRevision where version > 1);
delete from articleRevision where idArticleRevision in (select idArticleRevision from articleRevision where version > 1);

INSERT INTO dbPatch VALUES (NULL, NOW(), 'tidy up wiki', '0.1.15-5');
