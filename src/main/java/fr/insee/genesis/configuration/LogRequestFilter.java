package fr.insee.genesis.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;


@Component
@WebFilter(urlPatterns = "/*")
@Order(-999)
@Slf4j
public class LogRequestFilter extends OncePerRequestFilter {
	
	private static final String REQUEST_MESSAGE_FORMAT =
            "CALL {} {} - User: {} - Params : {}";
	
	private static final String RESPONSE_MESSAGE_FORMAT = 
			 "END {} {}  - "
			+ "Status :  {} - ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

    	//Cache request to avoid calling twice the same inputStream
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = "anonymous";

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            username = jwtAuth.getToken().getClaimAsString("preferred_username");
        } else if (authentication != null) {
            username = authentication.getName();
        }

        log.info(REQUEST_MESSAGE_FORMAT,
                req.getMethod(),
                req.getRequestURI(),
                username,
                request.getQueryString());


        // Execution request chain
        filterChain.doFilter(req, resp);

        log.info(RESPONSE_MESSAGE_FORMAT,
        		req.getMethod(), req.getRequestURI(),
        		resp.getStatus()); //Body
        
        // Finally remember to respond to the client with the cached data.
        resp.copyBodyToResponse();
    }

}
