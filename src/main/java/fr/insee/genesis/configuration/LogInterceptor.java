package fr.insee.genesis.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String fishTag = UUID.randomUUID().toString();
        String method = request.getMethod();
        String operationPath = request.getRequestURI();
        Authentication authentication = getCurrentUser();
        String username = "anonymous";
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                username = jwtAuth.getToken().getClaimAsString("preferred_username");
            }
        }

        ThreadContext.put("user", username.toUpperCase());
        ThreadContext.put("id", fishTag);
        ThreadContext.put("path", operationPath);
        ThreadContext.put("method", method);
        log.info("[" + username.toUpperCase() + "] - [" + method + "] - [" + operationPath + "]");
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) {
        // no need to posthandle things for this interceptor
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception exception) throws Exception {
        ThreadContext.clearMap();
    }


    public Authentication getCurrentUser(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

}

