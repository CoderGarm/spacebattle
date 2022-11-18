package de.yuga.spacebattle.backend.entities.wiki;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EDiffDeltaType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class ArticleLine implements Comparable<ArticleLine> {

    public static final int CONTENT_LENGTH = 255;
    public static final String LN = "\n";

    @NotNull
    private int lineNo;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EDiffDeltaType deltaType;

    /**
     * <b>Attention:</b> Never trim the content. Whitespaces at the end of a line are formatting relevant.
     */
    @Nonnull
    @NotNull
    @Size(max = CONTENT_LENGTH)
    private String content;

    public ArticleLine() {
    }

    public ArticleLine(final int lineNo, @Nonnull final EDiffDeltaType deltaType, @Nonnull final String content) {
        Preconditions.checkNotNull(deltaType, "deltaType must not be empty");
        Preconditions.checkNotNull(content, "content must not be empty");
        Preconditions.checkArgument(!content.contains(LN), "content must not contain a linebreak");
        Preconditions.checkArgument(content.length() <= CONTENT_LENGTH, "content is too long");

        this.lineNo = lineNo;
        this.deltaType = deltaType;
        this.content = content;
    }

    public int getLineNo() {
        return lineNo;
    }

    @Nonnull
    public EDiffDeltaType getDeltaType() {
        return deltaType;
    }

    @Nonnull
    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ArticleLine that = (ArticleLine) o;

        return new EqualsBuilder().append(lineNo, that.lineNo).append(deltaType, that.deltaType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(lineNo).append(deltaType).toHashCode();
    }

    @Override
    public String toString() {
        return "LineNo: " + lineNo + ": " + content;
    }

    @Override
    public int compareTo(@Nonnull final ArticleLine o) {
        return Integer.compare(getLineNo(), o.getLineNo());
    }
}
