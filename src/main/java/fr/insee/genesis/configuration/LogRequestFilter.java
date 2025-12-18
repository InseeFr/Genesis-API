package fr.insee.genesis.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
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
			 "CALL {} {} - "
			+ "Params : {}";
	
	private static final String RESPONSE_MESSAGE_FORMAT = 
			 "END {} {}  - "
			+ "Status :  {} - ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        int cacheLimit = -1;//<0 means no limit

    	//Cache request to avoid calling twice the same inputStream
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request, cacheLimit);
        ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        
        log.info(REQUEST_MESSAGE_FORMAT, 
        		req.getMethod(), req.getRequestURI(),
        	//	req.getContentType(),
            //    new ServletServerHttpRequest(req).getHeaders(), //Headers
                request.getQueryString());//Params


        // Execution request chain
        filterChain.doFilter(req, resp);
               

        log.info(RESPONSE_MESSAGE_FORMAT, 
        		req.getMethod(), req.getRequestURI(),
        		resp.getStatus()); //Body
        
        // Finally remember to respond to the client with the cached data.
        resp.copyBodyToResponse();
    }

}
