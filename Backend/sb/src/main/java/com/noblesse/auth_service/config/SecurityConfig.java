package com.noblesse.auth_service.config;

import com.noblesse.auth_service.enums.Role;
import com.noblesse.auth_service.service.CustomOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomJwtDecoder customJwtDecoder;

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private CustomOauth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                })
                .authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.POST, "/users/register","/users/{userId}/select-topics", "/auth/login", "/auth/introspect", "/auth/logout", "/topic/**", "/users/google/login", "/users/{userId}/upload-avatar", "/post/create-post/{userId}", "/comment/create/{userId}/{postId}", "/post/{postId}/upvote", "/post/{postId}/downvote", "/forgot-password/request", "/forgot-password/verify", "/forgot-password/reset").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/delete/{userId}", "/topic/delete/{topicId}").permitAll()
                        //.requestMatchers(HttpMethod.GET, "/users/all").hasAuthority(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/topic/**", "/user-avatars/**", "/images/**","/post/get-post/{userId}", "/post/{topicId}", "/post/get/{postId}", "/post/{postId}/score", "/post/posts").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/users/update/{userId}").permitAll()
                        .anyRequest().authenticated());

        // cau hinh dang nhap bang gg
        httpSecurity.oauth2Login(oauth2 -> oauth2
                .loginPage("/sharebox/login")
                .successHandler((request, response, authentication) -> response.sendRedirect("/sharebox/profile"))
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))

        );

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter())));

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

//    @Bean
//    JwtDecoder jwtDecoder(){
//        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
//        return NimbusJwtDecoder.withSecretKey(secretKeySpec)
//                .macAlgorithm(MacAlgorithm.HS512)
//                .build();
//    };

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

}
