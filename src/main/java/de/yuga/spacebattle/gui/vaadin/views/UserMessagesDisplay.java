package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.TextRenderer;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.UserMessage;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.vaadin.pekka.WysiwygE;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Displays the user messages and the form to write new messages.
 */
public class UserMessagesDisplay extends VerticalLayout {

    @Nonnull
    final UserService userService;

    private final User login;

    private final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private ListDataProvider<UserMessage> messagesDataProvider;
    private final HorizontalLayout messageListWriteDivider = new HorizontalLayout();
    private final Div messagesListContainer = new Div();
    private UserMessageForm userMessageForm;
    private UserMessage selectedMessage;
    private final AtomicReference<String> filter = new AtomicReference<>("");

    UserMessagesDisplay(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService);

        this.userService = userService;
        this.login = userService.getLoggedInUser();
        if (login == null)
        {
            throw new NotifySBUserException("You need to be logged in!");
        }

        // load user Messages, filter and sort messages
        messagesDataProvider = createUserMessageListDataProvider();

        // left side of the tab
        final VerticalLayout messageListLayout = new VerticalLayout();
        messageListLayout.setWidth("30%");
        messageListLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
        messageListLayout.setMargin(false);

        // left side contains 'new message', filter, unread and list
        messagesListContainer.setWidth("100%");
        final Button newMessage = new Button("New Message", event -> {
            selectedMessage = null;
            updateMessageList(); // to remove the selection of a message
            newMessage(messageListWriteDivider, null);
        });
        final TextField msgFilter = new TextField("Search", event -> {
            filter.set(event.getValue());
            updateMessageList(); // to update the list for the filtered ones
        });
        msgFilter.setWidthFull();
        updateMessageList(); // initial creation of the msg list
        messageListLayout.add(newMessage, msgFilter, messagesListContainer);

        // right side of the tab
        userMessageForm = new UserMessageForm(false);
        userMessageForm.setWidthFull();
        userMessageForm.setUserMessage(new UserMessage(login));

        messageListWriteDivider.add(messageListLayout, userMessageForm);
        messageListWriteDivider.setWidthFull();
        add(messageListWriteDivider);
    }

    /**
     * Creates a new {@link ListDataProvider} to fetch the data from the DB.
     * @return new ListDataProvider
     */
    private ListDataProvider<UserMessage> createUserMessageListDataProvider() {
        final ListDataProvider<UserMessage> userMessageDataProvider = new ListDataProvider<>(userService.getAllReceivedMessages());
        final Predicate<String> txtCond = str -> str.toLowerCase().contains(filter.get().toLowerCase());
        userMessageDataProvider.setFilter(msg -> (txtCond.test(msg.getSubject()) || txtCond.test(msg.getUserSender().getUsername())));
        userMessageDataProvider.setSortComparator(Comparator.comparing(UserMessage::getSentAt).reversed()::compare);
        return userMessageDataProvider;
    }

    /**
     * Update the list of message entries.
     */
    private void updateMessageList() {
        messagesListContainer.removeAll();
        messagesDataProvider = createUserMessageListDataProvider();
        final Stream<Div> divStream = messagesDataProvider.fetch(new Query<>()).map(this::createMsgListEntry);
        messagesListContainer.add(createUnReadMessagesLabel());
        divStream.forEach(messagesListContainer::add);
    }

    /**
     * Fetech the current count of unread messages and create a label for it.
     * @return Label
     */
    private Label createUnReadMessagesLabel() {
        final Stream<UserMessage> unreadMessages = messagesDataProvider.fetch(new Query<>(UserMessage::isUnRead));
        final Label unread = new Label("You have " + unreadMessages.count() + " unread Messages.");
        unread.setWidthFull();
        return unread;
    }

    /**
     * Create an entry for the list of messages
     * |sender name | sent at |
     * |Subject               |
     * @param message {@link UserMessage} to display
     * @return Div
     */
    private Div createMsgListEntry(UserMessage message) {
        // layout
        final Label sender = new Label(message.getUserSender().getUsername());
        final Label sentAt = new Label(message.getSentAt().format(dateTimeFormat));
        final HorizontalLayout msgHeader = new HorizontalLayout(sender, sentAt);
        msgHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
        final Label msgBody = new Label(message.getSubject());
        msgBody.setWidth("100%");
        final VerticalLayout msgWrapper = new VerticalLayout(msgHeader, msgBody);
        final Div msg = new Div(msgWrapper);
        // click event
        msg.addClickListener(event -> {
            selectedMessage = message;
            showMessage(message);
        });
        // style
        msg.addClassName("msg-list-entry");
        if (message.isUnRead()) {
            msg.addClassName("bold");
        }
        if (message.equals(selectedMessage)) {
            msg.addClassName("selected");
        }
        return msg;
    }

    /**
     * Show this message in the form part.
     * @param selectedMessage {@link UserMessage}
     */
    private void showMessage(UserMessage selectedMessage) {
        if (selectedMessage != null) {
            // set read/received
            selectedMessage.setReceivedAt();
            userService.updateUserMessage(selectedMessage);
            // show the message in as read only form
            final UserMessageForm newMessage = new UserMessageForm(true);
            newMessage.setWidth("65%");
            newMessage.setUserMessage(selectedMessage);
            messageListWriteDivider.replace(userMessageForm, newMessage);
            userMessageForm = newMessage;
            // update the message list and to remove the unread layout
            updateMessageList();
        }
    }

    /**
     * Create a new Message and show the form in the view.
     * @param messageListWriteDivider layout to update
     * @param replyMessage a message to reply to
     */
    private void newMessage(HorizontalLayout messageListWriteDivider, UserMessage replyMessage) {
        // update message part with empty message
        final UserMessageForm newMessageForm = new UserMessageForm(false);
        final UserMessage msg = new UserMessage(login);
        if (replyMessage != null) {
            msg.setMessage("<p><blockquote blockquote=\"\">"+replyMessage.getMessage()+"</blockquote></p><br/> ");
            msg.setUserReceiver(replyMessage.getUserSender());
            msg.setSubject("Re: " + replyMessage.getSubject());
        }
        newMessageForm.setUserMessage(msg);
        newMessageForm.setWidth("65%");
        messageListWriteDivider.replace(userMessageForm, newMessageForm);
        userMessageForm = newMessageForm;
    }

    /**
     * Form to create a new Message.
     */
    private class UserMessageForm extends FormLayout {

        @PropertyId("userReceiver")
        private final Select<User> userReceiver = new Select<>();

        @PropertyId("subject")
        private final TextField subject  = new TextField("subject", "subject");

        // see https://vaadin.com/directory/component/wysiwyg-e-rich-text-editor-component-for-java/overview
        @PropertyId("message")
        private final WysiwygE message = new WysiwygE("300px", "100%");

        private final Button save = new Button("Send");
        private final Button reply = new Button("Reply");

        private final Binder<UserMessage> userMessageBinder = new Binder<>(UserMessage.class);

        UserMessageForm(boolean readOnly) {
            final VerticalLayout formVLayout = new VerticalLayout();
            userReceiver.setLabel("Receiver");
            userReceiver.setItems(userService.findAll());
            userReceiver.setRenderer(new TextRenderer<>(User::getUsername));
            userReceiver.setEmptySelectionAllowed(false);
            userReceiver.setReadOnly(readOnly);

            subject.setRequired(true);
            subject.setReadOnly(readOnly);
            subject.setMaxLength(255);

            message.setReadOnly(readOnly);

            save.addClickListener(event -> {
                save();
                updateMessageList();
            });
            save.setVisible(!readOnly);

            reply.addClickListener(event -> newMessage(messageListWriteDivider, userMessageBinder.getBean()));
            reply.setVisible(readOnly);

            formVLayout.add(userReceiver, subject, message, save, reply);
            formVLayout.setMargin(false);
            formVLayout.setMinWidth("100%");
            formVLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH);
            add(formVLayout);
            userMessageBinder.bindInstanceFields(this);
        }

        public void setUserMessage(UserMessage msg) {
            userMessageBinder.setBean(msg);
        }

        private void save() {
            final UserMessage msg = userMessageBinder.getBean();
            userService.saveUserMessage(msg);
            save.setText("Message Sent");
            save.setEnabled(false);
        }
    }
}
