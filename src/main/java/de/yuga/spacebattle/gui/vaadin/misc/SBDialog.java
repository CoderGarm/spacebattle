package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableConsumer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.yuga.spacebattle.gui.vaadin.misc.SBDialog.Position.INITIAL_POSITION;

/**
 * Non-modal dialog with a close button and a content free headline.
 * <p>
 * Properties:
 * - non-modal
 * - draggable
 * - close on ESC
 * - resizable
 * - positionable
 */
@CssImport("./styles/views/main/details/sb-dialog.css")
public class SBDialog extends Dialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SBDialog.class);

    @Nonnull
    protected final Button close;

    /**
     * CSS class name to allow dragging by the user.
     */
    public static final String DRAGGABLE_DRAGGABLE_LEAF_ONLY = "draggable draggable-leaf-only";

    /**
     * The content which should be displayed of this dialog.
     */
    @Nonnull
    private Component content;

    /**
     * Stores the last position of <code>this</code>.
     */
    @Nullable
    private SBDialog.Position lastPosition;

    /**
     * Container to display the content.
     */
    @Nonnull
    private final Div contentArea = new Div();

    public SBDialog(@Nonnull final Component content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        setDraggable(true);
        setCloseOnEsc(true);
        setCloseOnOutsideClick(false);
        setModal(false);
        setResizable(true);

        final Div fullContent = new Div();
        fullContent.setId("sb-dialog");
        fullContent.setClassName(DRAGGABLE_DRAGGABLE_LEAF_ONLY);
        fullContent.setMinHeight("150px");

        final Div topBar = new Div();
        topBar.setClassName("sb-dialog-header " + DRAGGABLE_DRAGGABLE_LEAF_ONLY);
        close = new Button("X", event -> close());
        close.setClassName("sb-dialog-close");
        topBar.add(close);

        contentArea.setClassName("sb-dialog-content-area " + DRAGGABLE_DRAGGABLE_LEAF_ONLY);

        if (content instanceof HasStyle) {
            final HasStyle hasStyle = (HasStyle) content;
            final String className = hasStyle.getClassName();
            final String classNameToSet = (className != null ? className + " " : "");
            hasStyle.setClassName(classNameToSet + DRAGGABLE_DRAGGABLE_LEAF_ONLY);
        }

        this.content = content;
        contentArea.add(content);
        fullContent.add(topBar, contentArea);
        add(fullContent);
    }

    /**
     * Replaces the content of the dialog.
     *
     * @param content the new content which should replace the old content
     */
    public void setContent(@Nullable final Component content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        contentArea.remove(this.content);
        this.content = content;
        contentArea.add(this.content);
    }

    /**
     * Closes the dialog and save it's last position for reopening it.
     */
    @Override
    public void close() {
        getPosition(position -> {
            lastPosition = position;
            super.close();
        });
    }

    /**
     * Opens the dialog at the given position. Except when the last position is saved.
     *
     * @param position the position to open html-top-left-based
     */
    public void open(@Nullable final SBDialog.Position position) {
        open();
        setPosition(lastPosition != null ? lastPosition : (position != null ? position : INITIAL_POSITION));
    }

    /**
     * Returns the content of this dialog.
     *
     * @return the content
     */
    @Nonnull
    public Component getContent() {
        return content;
    }

    private static final String SET_PROPERTY_IN_OVERLAY_JS = "this.$.overlay.$.overlay.style[$0]=$1";

    public void setPosition(Position position) {
        enablePositioning(true);
        getElement().executeJs(SET_PROPERTY_IN_OVERLAY_JS, "left", position.getLeft());
        getElement().executeJs(SET_PROPERTY_IN_OVERLAY_JS, "top", position.getTop());
    }

    private void enablePositioning(final boolean positioningEnabled) {
        getElement().executeJs(SET_PROPERTY_IN_OVERLAY_JS, "align-self", positioningEnabled ? "flex-start" : "unset");
        getElement().executeJs(SET_PROPERTY_IN_OVERLAY_JS, "position", positioningEnabled ? "absolute" : "relative");
    }

    public void getPosition(SerializableConsumer<Position> consumer) {
        getElement()
                .executeJs("return [" + "this.$.overlay.$.overlay.style['top'], this.$.overlay.$.overlay.style['left']" + "]")
                .then(String.class, s -> {
                            String[] split = StringUtils.split(s, ',');
                            if (split.length == 2 && split[0] != null && split[1] != null) {
                                Position position = new Position(split[0], split[1]);
                                consumer.accept(position);
                            }
                        }
                );
    }

    public static class Position {

        public static final Position INITIAL_POSITION = new Position("0px", "0px");

        /**
         * Initial position to open dialogs that they are out of the way.
         */
        public static final Position INITIAL_TOP_LEFT = new Position("100px", "100px");

        private String top;
        private String left;

        public Position(String top, String left) {
            this.top = top;
            this.left = left;
        }

        public String getTop() {
            return top;
        }

        public void setTop(String top) {
            this.top = top;
        }

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }
    }
}
