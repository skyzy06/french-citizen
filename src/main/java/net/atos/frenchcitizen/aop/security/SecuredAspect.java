package net.atos.frenchcitizen.aop.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import net.atos.frenchcitizen.exception.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SecuredAspect {
    @Before("execution(* net.atos.frenchcitizen.controller..*(..)) && @annotation(secured)")
    public void securedIdEnsResponse(JoinPoint jp, Secured secured) throws NoSuchFieldException, IllegalAccessException {
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
        HttpServletRequest servletRequest = getCurrentHttpRequest();
        if (servletRequest == null) {
            return;
        }

        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.length() > "Bearer ".length()) {
            DecodedJWT jwt = decode(authHeader.substring("Bearer ".length()));
            if (jwt != null && jwt.getSubject() != null) {
                if (jwt.getSubject().equals(id)) {
                    return;
                }
                throw new UnauthorizedException(null, "Unauthorized access");
            }
        }
        throw new UnauthorizedException(null, "Invalid authorization header");
    }

    private DecodedJWT decode(String token) {
        try {
            return JWT.decode(token);
        } catch (JWTDecodeException ex) {
            return null;
        }
    }


    private HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }
}
