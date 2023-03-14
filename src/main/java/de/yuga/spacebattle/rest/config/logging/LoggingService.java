package de.yuga.spacebattle.rest.config.logging;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.rest.*;
import de.yuga.spacebattle.rest.api.EndpointDefinition;
import de.yuga.spacebattle.rest.dto.account.AuthRequest;
import de.yuga.spacebattle.rest.dto.account.UserReq;
import de.yuga.spacebattle.rest.dto.account.chat.ChatMessage;
import de.yuga.spacebattle.rest.dto.account.forum.ForumMessage;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;
import de.yuga.spacebattle.rest.dto.enums.HasTypeName;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class LoggingService {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(AuthRequest.class, new AuthRequestAdapter())
            .registerTypeAdapter(HasTypeName.class, new HasTypeNameAdapter())
            .registerTypeAdapter(HasIcon.class, new HasIconAdapter())
            .registerTypeAdapter(ChatMessage.class, new ChatMessageAdapter())
            .registerTypeAdapter(ForumMessage.class, new ForumMessageAdapter())
            .registerTypeAdapter(StarSystem.class, new StarSystemAdapter())
            .registerTypeAdapter(UserReq.class, new UserReqAdapter())
            .registerTypeAdapter(BattleReport.class, new BattleReportAdapter())
            .registerTypeAdapter(MasterOfTheUniverseService.CoordsBlob.class, new CoordAdapter())
            .setPrettyPrinting()
            .create();
    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingService.class);

    private final boolean logLevel;

    public LoggingService(@Nonnull @Value("${logging.rest.calls:false}") final String logLevel) {
        Preconditions.checkNotNull(logLevel, "logLevel shouldn't be null!");

        this.logLevel = Boolean.parseBoolean(logLevel);
    }

    public void logRequest(@Nonnull final HttpServletRequest httpServletRequest,
                           @Nullable final Object body) {
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest shouldn't be null!");

        if (!logLevel) {
            return;
        }

        String requestURI = httpServletRequest.getRequestURI();
        if (!requestURI.contains(EndpointDefinition.BASE_ENDPOINT)) {
            // just to ignore everything which is not part of the base rest endpoint
            return;
        }
        String method = httpServletRequest.getMethod();
        String host = httpServletRequest.getRequestURL().toString();
        StringBuilder sb = new StringBuilder();

        // creating a should-be-unique ID for a pair of request and response
        int correlationID = httpServletRequest.hashCode();
        sb.append("\n" + "[" + correlationID + "] < " + method + " " + host);
        httpServletRequest.setAttribute("correlationID", correlationID);
        sb.append("\n");
        Map<String, String> parametersMap = buildParametersMap(httpServletRequest);
        parametersMap.forEach((header, argument) -> sb.append("\n" + header + ": " + argument));

        Map<String, String> headersMap = buildHeadersMap(httpServletRequest);
        headersMap.forEach((header, argument) -> sb.append("\n" + header + ": " + argument));
        sb.append("\n");
        writeBodyToLogString(body, sb);

        LOGGER.info(sb.toString() + "\n");
    }

    public void logResponse(@Nonnull final HttpServletRequest httpServletRequest,
                            @Nonnull final HttpServletResponse httpServletResponse,
                            @Nullable final Object body) {
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest shouldn't be null!");
        Preconditions.checkNotNull(httpServletResponse, "httpServletResponse shouldn't be null!");

        if (!logLevel) {
            return;
        }

        String requestURI = httpServletRequest.getRequestURI();
        if (!requestURI.contains(EndpointDefinition.BASE_ENDPOINT)) {
            // just to ignore everything which is not part of the base rest endpoint
            return;
        }
        String method = httpServletRequest.getMethod();
        String host = httpServletRequest.getRequestURL().toString();

        // fetching correlation ID to identify the response for a unique request
        Integer correlationID = (Integer) httpServletRequest.getAttribute("correlationID");
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "[" + correlationID + "] > " + method + " " + host);

        Map<String, String> headersMap = buildHeadersMap(httpServletResponse);
        headersMap.forEach((header, argument) -> sb.append("\n" + header + ": " + argument));
        sb.append("\n");
        writeBodyToLogString(body, sb);

        LOGGER.info(sb.toString());
    }

    private void writeBodyToLogString(@Nullable final Object body,
                                      @Nonnull final StringBuilder stringBuilder) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder shouldn't be null!");

        if (body != null) {
            try {
                String jsonInString = GSON.toJson(body);
                stringBuilder.append("\n" + jsonInString);
            } catch (final JsonIOException e) {
                stringBuilder.append("\nException while parsing body: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Map<String, String> buildParametersMap(@Nonnull final HttpServletRequest httpServletRequest) {
        Preconditions.checkNotNull(httpServletRequest, "httpServletRequest shouldn't be null!");

        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }

    private Map<String, String> buildHeadersMap(@Nonnull final HttpServletRequest request) {
        Preconditions.checkNotNull(request, "request shouldn't be null!");

        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private Map<String, String> buildHeadersMap(@Nonnull final HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String header : headerNames) {
            map.put(header, response.getHeader(header));
        }
        return map;
    }
}
