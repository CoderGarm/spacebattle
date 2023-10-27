# noinspection SqlWithoutWhere
update forumMessageRead set isRead = true;
# noinspection SqlWithoutWhere
update userMessage set receivedAt = now();
