package com.ws01.websocket.confing.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CognitoJwtProcessor {
    private static final String COGNITO_JWK_URI_TEMPLATE =
            "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json";
    @Value("${application.cognito.awsRegion}")
    private  String cognitoAwsRegion;
    @Value("${application.cognito.userPoolId}")
    private  String cognitoUserPoolId;

    public String getSub(String token) {
        if(Objects.isNull(token)) return null;

        Jwt claimsSet = NimbusJwtDecoder
                .withJwkSetUri(COGNITO_JWK_URI_TEMPLATE.formatted(cognitoAwsRegion, cognitoUserPoolId))
                .build().decode(token);

        return claimsSet.getClaim("sub");

    }
}