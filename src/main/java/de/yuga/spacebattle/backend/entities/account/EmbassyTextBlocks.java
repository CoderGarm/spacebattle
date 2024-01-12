package de.yuga.spacebattle.backend.entities.account;

import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.validation.constraints.Size;

@Embeddable
public class EmbassyTextBlocks {

    @Lob
    @Nullable
    @Size(max = 2000)
    private String leftUpper;

    @Lob
    @Nullable
    @Size(max = 4000)
    private String rightUpper;

    @Lob
    @Nullable
    @Size(max = 4000)
    private String leftBottom;

    @Lob
    @Nullable
    @Size(max = 6000)
    private String rightBottom;

    public EmbassyTextBlocks() {
    }

    @Nullable
    public String getLeftUpper() {
        return leftUpper;
    }

    public void setLeftUpper(@Nullable final String leftUpper) {
        this.leftUpper = leftUpper;
    }

    @Nullable
    public String getRightUpper() {
        return rightUpper;
    }

    public void setRightUpper(@Nullable final String rightUpper) {
        this.rightUpper = rightUpper;
    }

    @Nullable
    public String getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(@Nullable final String leftBottom) {
        this.leftBottom = leftBottom;
    }

    @Nullable
    public String getRightBottom() {
        return rightBottom;
    }

    public void setRightBottom(@Nullable final String rightBottom) {
        this.rightBottom = rightBottom;
    }
}
