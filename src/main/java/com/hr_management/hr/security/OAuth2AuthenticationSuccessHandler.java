package com.hr_management.hr.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        log.info("OAuth2 Authentication successful. Preparing JWT and redirect.");

        // Extract user details (we expect CustomUserDetails from our CustomOAuth2UserService)
        Object principal = authentication.getPrincipal();
        String username;
        
        if (principal instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) principal;
            username = customUserDetails.getUsername(); // Or getEmail() depending on JwtService
            log.debug("Extracted username '{}' from CustomUserDetails.", username);
            
            // Generate JWT token using the CustomUserDetails object
            String token = jwtService.generateToken(customUserDetails);
            log.debug("Generated JWT token for user '{}'.", username);

            // Build the redirect URI with the token
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .build().toUriString();

            log.info("Redirecting user '{}' to: {}", username, targetUrl);
            
            // Configure the redirect strategy
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } else {
             // Handle cases where the principal is not CustomUserDetails (error or fallback)
             log.error("Principal is not an instance of CustomUserDetails after OAuth2 login. Principal type: {}", principal.getClass().getName());
             // Redirect to an error page or handle appropriately
             String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "AuthenticationFailed")
                    .build().toUriString();
             getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
} 