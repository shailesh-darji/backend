package com.under10s.user.filter;



import com.under10s.user.api.UserServiceURI;
import com.under10s.user.dao.entity.TokenModel;
import com.under10s.user.dao.repository.TokenRepository;
import com.under10s.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;

@Component
@RequiredArgsConstructor
public class JWTAuthFilter extends OncePerRequestFilter {

    Logger LOGGER = LoggerFactory.getLogger(JWTAuthFilter.class);
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService = new JwtService();

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Runs Once per request to validate token and store authentication object in Security
     * Context Holder
     *
     * @param request from client
     * @param response from client
     * @param filterChain the filter chain object
     * @throws ServletException if there is a servlet exception
     * @throws IOException if there is an I/O exception
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        String requestURI = request.getRequestURI();

        if (requestURI.endsWith(UserServiceURI.URI_LOGIN) || requestURI.endsWith(UserServiceURI.URI_REGISER_USER)
                || requestURI.contains(UserServiceURI.FORGOT_PASSWORD_OTP_URL) || requestURI.contains(UserServiceURI.VALIDATE_OTP_URL)) {
            filterChain.doFilter(request,response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            LOGGER.error("Auth Header not passed");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{ \"error\": \"Token is invalid\" }");
            return;
        }

        jwt=authHeader.substring(7);
        LOGGER.error("JWT Token : {}", jwt);
        if (jwtService.isTokenExpired(jwt)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{ \"error\": \"Token is expired\" }");
            return;
        }

        Optional<TokenModel> token = Optional.ofNullable(tokenRepository.findByToken(jwt));
        if(token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{ \"error\": \"Invalid or Old Token used\" }");
            return;
        }

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtService.extractUsername(jwt));
        if (SecurityContextHolder.getContext().getAuthentication() == null){
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request,response);
    }
}