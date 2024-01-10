package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

@Schema(description = ".")
public class RPGTextBlocks {

    @Nullable
    @Size(max = 2000)
    @JsonProperty
    @Schema(description = "The text. max 2000")
    private String leftUpper;

    @Nullable
    @Size(max = 4000)
    @JsonProperty
    @Schema(description = "The text. max 4000")
    private String rightUpper;

    @Nullable
    @Size(max = 4000)
    @JsonProperty
    @Schema(description = "The text. max 4000")
    private String leftBottom;

    @Nullable
    @Size(max = 6000)
    @JsonProperty
    @Schema(description = "The text. max 6000")
    private String rightBottom;

    public RPGTextBlocks() {
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
