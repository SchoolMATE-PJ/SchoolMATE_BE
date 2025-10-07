package com.spring.schoolmate.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
  private final String frontendRedirectUri;

  public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, String frontendRedirectUri) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
      clientRegistrationRepository, "/oauth2/authorization");
    this.frontendRedirectUri = frontendRedirectUri;
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authorizationRequest = this.defaultResolver.resolve(request);
    return customizeAuthorizationRequest(authorizationRequest);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest authorizationRequest = this.defaultResolver.resolve(request, clientRegistrationId);
    return customizeAuthorizationRequest(authorizationRequest, clientRegistrationId);
  }

  // resolve(request)를 위한 커스터마이징 메서드
  private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
    if (authorizationRequest == null) {
      return null;
    }

    String clientRegistrationId = (String) authorizationRequest.getAdditionalParameters().get("registration_id");

    if (clientRegistrationId != null && "kakao".equals(clientRegistrationId)) {
      return OAuth2AuthorizationRequest.from(authorizationRequest)
        .redirectUri(this.frontendRedirectUri)
        .build();
    }

    return authorizationRequest;
  }

  // resolve(request, clientRegistrationId)를 위한 커스터마이징 메서드
  private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, String clientRegistrationId) {
    if (authorizationRequest == null) {
      return null;
    }

    // 파라미터로 받은 clientRegistrationId를 사용.
    if ("kakao".equals(clientRegistrationId)) {
      return OAuth2AuthorizationRequest.from(authorizationRequest)
        .redirectUri(this.frontendRedirectUri)
        .build();
    }

    return authorizationRequest;
  }
}