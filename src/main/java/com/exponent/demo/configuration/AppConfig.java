package com.exponent.demo.configuration;

import com.exponent.demo.auth.AuthFilter;
import com.exponent.demo.auth.JwtFilter;
import com.exponent.demo.dto.UserData;
import com.exponent.demo.model.UserModel;
import com.exponent.demo.repo.UserRepository;
import com.exponent.demo.response.AuthResponse;
import com.exponent.demo.service.JwtService;
import com.exponent.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class AppConfig {

    private String [] publicUrls = {
            "/api/user/create", "/v3/api-docs/**",    // OpenAPI JSON
            "/swagger-ui.html",   // Swagger UI HTML entrypoint
            "/swagger-ui/**",     // Swagger UI resources (JS, CSS)
            "/webjars/**",        // (optional, legacy)
            "/actuator/**",
            "/api/user/login",
    };


    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtService jwtService;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(getPasswordEncoder());
        return authenticationProvider;
    }

    @Bean
    public MyUserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    public AuthFilter authFilter(AuthenticationManager authenticationManager) throws Exception {
        AuthFilter authFilter = new AuthFilter();
        authFilter.setAuthenticationManager(authenticationManager);
        authFilter.setFilterProcessesUrl("/api/user/login");
        authFilter.setAuthenticationSuccessHandler(((request, response, authentication) -> {

            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            response.setStatus(HttpServletResponse.SC_OK);

             UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
             UserModel userModel = userRepository.
                     findByUsername(userPrincipal.getUsername())
                     .orElse(new UserModel());

             UserData userData = UserData
                     .builder()
                     .fullname(userModel.getFullname())
                     .username(userModel.getUsername())
                     .build();

             String jwtToken = jwtService.generateToken(userData);
             AuthResponse authResponse = AuthResponse.builder()
                    .username(userModel.getUsername())
                    .jwtToken(jwtToken)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(authResponse));
        }));

        authFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            log.error("ERROR: "+exception.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Login Failure: "+exception.getMessage());

        });

        return authFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173","https://health-inspector.onrender.com")); // frontend port
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // important for cookies or auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationManager manager) throws Exception{
        httpSecurity.csrf(CsrfConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests.requestMatchers(publicUrls)
                        .permitAll().anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterAt(authFilter(manager), AuthFilter.class)
                .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return  httpSecurity.build();
    }
}
