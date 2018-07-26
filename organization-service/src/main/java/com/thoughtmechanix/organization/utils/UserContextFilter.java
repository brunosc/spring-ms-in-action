package com.thoughtmechanix.organization.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class UserContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        logger.debug("Entering the UserContextFilter for the organization service");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        logger.debug("I am entering the organization service id with auth token: ", httpServletRequest.getHeader("Authorization"));


        String correlationId = httpServletRequest.getHeader(UserContext.CORRELATION_ID);
        String userId = httpServletRequest.getHeader(UserContext.USER_ID);
        String authToken = httpServletRequest.getHeader(UserContext.AUTH_TOKEN);
        String orgId = httpServletRequest.getHeader(UserContext.ORG_ID);

        logger.debug("***** I am entering the organization service id with correlation id: {}" ,correlationId);
        UserContextHolder.getContext().setCorrelationId(correlationId);
        UserContextHolder.getContext().setUserId(userId);
        UserContextHolder.getContext().setAuthToken(authToken);
        UserContextHolder.getContext().setOrgId(orgId);

        logger.debug("Exiting the UserContextFilter");
        chain.doFilter(httpServletRequest, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
