package net.atos.frenchcitizen.aop.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import net.atos.frenchcitizen.exception.UnauthorizedException;
import net.atos.frenchcitizen.helper.TokenHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SecuredAspect {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_BEARER_PREFIX = "Bearer ";

    @Autowired
    private TokenHelper tokenHelper;

    @Before("execution(* net.atos.frenchcitizen.controller..*(..)) && @annotation(secured)")
    public void securedIdEnsResponse(JoinPoint jp, Secured secured) {
        Object[] args = jp.getArgs();
        CodeSignature codeSignature = (CodeSignature) jp.getSignature();
        String[] sigParamNames = codeSignature.getParameterNames();
        assert args.length == sigParamNames.length;

        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            if (sigParamNames[argIndex].equals("id")) {
                checkAccess(args[argIndex].toString());
            }
        }
    }

    private void checkAccess(String id) {
        DecodedJWT jwt = tokenHelper.decode(getAuthorizationBearer());
        if (!jwt.getSubject().equals(id)) {
            throw new UnauthorizedException(null, "Unauthorized access");
        }
    }

    private String getAuthorizationBearer() {
        HttpServletRequest servletRequest = getCurrentHttpRequest();
        if (servletRequest == null) {
            return null;
        }
        return servletRequest.getHeader(AUTHORIZATION_HEADER).substring(TOKEN_BEARER_PREFIX.length());
    }

    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}
