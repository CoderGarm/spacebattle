package de.yuga.spacebattle.rest.api;

import de.yuga.spacebattle.rest.config.context.RequestContext;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.Nonnull;

@Controller
public class BaseApi {

    @Nonnull
    @Autowired
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private RequestContext requestContext;

    @Nonnull
    @Autowired
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    private JwtTokenUtil tokenUtil;

    public BaseApi() {
    }

    public String getPreferredLanguage() {
        return requestContext.getAcceptedLanguage();
    }

    public int getIdUser() {
        return tokenUtil.getIdUserFromAccessToken(requestContext.getToken());
    }
}
