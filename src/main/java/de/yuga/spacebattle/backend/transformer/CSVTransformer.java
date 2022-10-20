package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CSVTransformer<T> {

    protected List<CSVTransformer<?>> dependencies = new ArrayList<>();
    protected List<String> headers = new ArrayList<>();

    private StringBuilder sb = new StringBuilder();
    public static final String CSV_SEPARATOR = ",";
    protected static final String LN = System.lineSeparator();

    /**
     * If <code>true</code>, the header will be written directly above the payload line.<br>
     * Otherwise, the headers will not be written.<br>
     * <br>
     * The magic lies in to allow writing the header for the main class and not for the dependent classes.
     */
    private final boolean withoutHeader;

    @Nonnull
    private final String preferredLanguage;

    public CSVTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        this.withoutHeader = withoutHeader;
        this.preferredLanguage = preferredLanguage;
        getDependencies(preferredLanguage);
        createHeader();
    }

    public CSVTransformer(@Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        this.withoutHeader = false;
        this.preferredLanguage = preferredLanguage;
        getDependencies(preferredLanguage);
        createHeader();
    }

    @Nonnull
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    protected void a(@Nullable final String o) {
        if (o != null) {
            sb.append(o);
        }
        sb.append(CSV_SEPARATOR);
    }

    protected void a(@Nullable final Long o) {
        if (o != null) {
            sb.append(o);
        }
        sb.append(CSV_SEPARATOR);
    }

    protected void a(@Nullable final Integer o) {
        if (o != null) {
            sb.append(o);
        }
        sb.append(CSV_SEPARATOR);
    }

    protected void a(@Nullable final Enum<?> o) {
        if (o != null) {
            sb.append(o.name());
        }
        sb.append(CSV_SEPARATOR);
    }

    @Nonnull
    protected String sanitize() {
        String toString = sb.toString().trim();
        sb = new StringBuilder();
        if (toString.endsWith(CSV_SEPARATOR) && !toString.endsWith(CSV_SEPARATOR + CSV_SEPARATOR)) {
            final int lastIndexOf = toString.lastIndexOf(CSV_SEPARATOR);
            toString = toString.substring(0, lastIndexOf);
        }
        return toString;
    }

    /**
     * Add the headers in the correct order.
     */
    protected abstract void createHeader();

    /**
     * Add the dependent transformer in the order how the fields should be transformed.
     *
     * @param preferredLanguage the language which should be used for translations
     */
    protected abstract void getDependencies(@Nonnull final String preferredLanguage);

    /**
     * Add the object-to-csv methodology field by field.
     */
    protected abstract void convertInternally(@Nonnull final T toTransform);

    public String convert(@Nullable final Collection<T> toTransform) {
        final String headersString = getHeadersString();
        if (toTransform == null || toTransform.isEmpty()) {
            if (withoutHeader) {
                return "";
            }
            return headersString + LN;
        }
        final String payload = toTransform.stream().map(this::convertSingleElement).collect(Collectors.joining(LN));
        if (withoutHeader) {
            return payload;
        }
        return headersString + LN + payload;
    }

    public String convert(@Nullable final T toTransform) {
        final String headersString = getHeadersString();
        if (toTransform == null) {
            if (withoutHeader) {
                return "";
            }
            return headersString + LN;
        }
        final String payload = convertSingleElement(toTransform);
        if (withoutHeader) {
            return payload;
        }
        return headersString + LN + payload;
    }

    private String convertSingleElement(@Nullable final T toTransform) {
        if (toTransform == null) {
            return "";
        }

        convertInternally(toTransform);
        return sanitize();
    }

    protected List<CSVTransformer<?>> getDependencies() {
        return dependencies;
    }

    protected List<String> getHeaders() {
        return headers;
    }

    protected String getHeadersString() {
        return String.join(CSV_SEPARATOR, headers);
    }
}
