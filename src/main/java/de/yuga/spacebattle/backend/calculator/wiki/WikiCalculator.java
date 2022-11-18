package de.yuga.spacebattle.backend.calculator.wiki;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.wiki.ArticleLine;
import de.yuga.spacebattle.backend.enums.EDiffDeltaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.wiki.ArticleLine.LN;

public class WikiCalculator {

    private final static Logger LOGGER = LoggerFactory.getLogger(WikiCalculator.class);

    public static final String WS = " ";

    private WikiCalculator() {
    }

    @Nonnull
    public static List<ArticleLine> generateArticleLines(@Nonnull final String content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        final List<ArticleLine> result = new ArrayList<>();
        if (StringUtils.isEmpty(content)) {
            return result;
        }
        if (content.contains(LN)) {
            List<String> lines = Arrays.stream(StringUtils.splitByWholeSeparatorPreserveAllTokens(content, LN))
                    .collect(Collectors.toList());
            final boolean tooLongLinesPresent = lines.stream().anyMatch(WikiCalculator::tooLong);
            if (tooLongLinesPresent) {
                lines = separateLines(lines);
            }

            for (int i = 0; i < lines.size(); i++) {
                result.add(new ArticleLine(i, EDiffDeltaType.INSERT, lines.get(i)));
            }
        }
        return result;
    }

    private static List<String> separateLines(@Nonnull final List<String> lines) {
        Preconditions.checkNotNull(lines, "lines must not be empty");

        final List<String> lst = new ArrayList<>();
        for (final String line : lines) {
            if (tooLong(line)) {
                final String[] words = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, null);
                final int to = words.length - 1;
                for (int j = to; j >= 0; j--) {
                    String[] copy = Arrays.copyOfRange(words, 0, j);
                    String newLine = String.join(WS, copy);
                    if (!tooLong(newLine)) {
                        lst.add(newLine);
                        break;
                    } else {
                        final int from = j + 1;
                        if (from < to) {
                            copy = Arrays.copyOfRange(words, from, to);
                            newLine = String.join(WS, copy);
                            if (!tooLong(newLine)) {
                                lst.add(newLine);
                                break;
                            }
                        }
                    }
                }
            } else {
                lst.add(line);
            }
        }
        return lst;
    }

    private static boolean tooLong(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");
        if (StringUtils.isEmpty(line)) {
            return false;
        }
        return line.replace(LN, "").length() > ArticleLine.CONTENT_LENGTH;
    }

    @Nullable
    public static String getAsPlainString(@Nonnull final List<ArticleLine> articleLines) {
        Preconditions.checkNotNull(articleLines, "articleLines must not be empty");

        if (articleLines.isEmpty()) {
            return null;
        }
        return articleLines.stream().map(ArticleLine::getContent).collect(Collectors.joining(ArticleLine.LN));
    }

    public static List<ArticleLine> buildDiff(@Nonnull final List<ArticleLine> oldContent, @Nonnull final List<ArticleLine> newContent) {
        Preconditions.checkNotNull(oldContent, "oldContent must not be empty");
        Preconditions.checkNotNull(newContent, "newContent must not be empty");

        final List<ArticleLine> changes = new ArrayList<>();
        final List<String> oldC = oldContent.stream().map(ArticleLine::getContent).collect(Collectors.toList());
        final List<String> newC = separateLines(newContent.stream().map(ArticleLine::getContent).collect(Collectors.toList()));
        final Patch<String> diff = DiffUtils.diff(oldC, newC);
        for (final AbstractDelta<String> delta : diff.getDeltas()) {
            final Chunk<String> source = delta.getSource();
            final Chunk<String> target = delta.getTarget();
            final int sourcePos = source.getPosition();
            final int targetPos = target.getPosition();

            final ArticleLine del;
            final ArticleLine ins;
            final DeltaType deltaType = delta.getType();
            switch (deltaType) {
                case CHANGE:
                    del = new ArticleLine(sourcePos, EDiffDeltaType.DELETE, String.join("", source.getLines()));
                    changes.add(del);
                    ins = new ArticleLine(targetPos, EDiffDeltaType.INSERT, String.join("", target.getLines()));
                    changes.add(ins);
                    break;
                case DELETE:
                    del = new ArticleLine(sourcePos, EDiffDeltaType.DELETE, String.join("", source.getLines()));
                    changes.add(del);
                    break;
                case INSERT:
                    ins = new ArticleLine(targetPos, EDiffDeltaType.INSERT, String.join("", target.getLines()));
                    changes.add(ins);
                    break;
                case EQUAL:
                default:
                    break;
            }
        }
        return changes;
    }
}
