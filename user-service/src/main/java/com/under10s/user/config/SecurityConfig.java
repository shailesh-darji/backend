package com.under10s.user.config;



import com.under10s.user.api.UserServiceURI;
import com.under10s.user.filter.JWTAuthFilter;
import com.under10s.user.service.LogoutService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private final JWTAuthFilter jwtAuthFilter;

    @Autowired
    private final AuthenticationProvider authenticationProvider;

    @Autowired
    private final LogoutService logoutService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling( exceptionHandler -> exceptionHandler
                        .authenticationEntryPoint((request, response, ex) -> {
                            LOGGER.error("URI: {} , Error: ",request.getRequestURI(), ex);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Not Authorized");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers (UserServiceURI.LOGIN_URL,UserServiceURI.REGISER_URL, UserServiceURI.FORGOT_PASSWORD_OTP_URL, UserServiceURI.VALIDATE_OTP_URL, UserServiceURI.URI_ERROR)
                        .permitAll()
                        .anyRequest()
                        .authenticated()

                )
                .sessionManagement( session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout( logoutHandler -> logoutHandler
                        .logoutUrl(UserServiceURI.LOGOUT_URL)
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
                );

        return http.build();
    }
}
