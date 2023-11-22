package de.yuga.spacebattle.rest.dto.misc.wiki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Schema
public class DateBlock {

    @JsonProperty
    @Schema
    private Integer day;

    @JsonProperty
    @Schema
    private Integer month;

    @JsonProperty
    @Schema(required = true)
    private Integer year;

    public DateBlock(@Nullable final String day, @Nullable final String month, @Nonnull final String year) {
        Preconditions.checkNotNull(year, "year must not be empty");

        this.day = day != null ? Integer.parseInt(day) : null;
        this.month = getMonth(month);
        this.year = Integer.parseInt(year.replaceAll(" ", "").replaceAll("PD", ""));
    }

    @Nullable
    @JsonIgnore
    private Integer getMonth(@Nullable final String month) {
        if (month == null) {
            return null;
        }
        final Date date;
        try {
            date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(month);
        } catch (ParseException e) {
            System.out.println("nope: " + month);
            return null;
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }
}
