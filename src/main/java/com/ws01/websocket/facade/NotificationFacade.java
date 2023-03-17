package com.ws01.websocket.facade;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationFacade {
    @Value("${application.notification.password}")
    private String password;
    @Value("${application.notification.userName}")
    private String username;
    @Value("${application.notification.clientId}")
    private String clientId;
    @Value("${application.notification.notificationApi}")
    private String notificationApi;
    @Value("${application.notification.clientSecret}")
    private String clientSecret;
   @Value("${application.notification.enable}")
    private boolean enable;


    private  String [] getHeaders()   {

        try(CognitoIdentityProviderClient cognitoIdentityProviderClient=
                    CognitoIdentityProviderClient.builder()
                            .credentialsProvider(AnonymousCredentialsProvider.create())
                            .region(Region.US_EAST_1).build()){

            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", username);
            authParams.put("PASSWORD", password);
            authParams.put("SECRET_HASH", calculateSecretHash());

            InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(authParams)
                    .clientId(clientId)
                    .build();

            InitiateAuthResponse initiateAuthResponse =
                    cognitoIdentityProviderClient.initiateAuth(initiateAuthRequest);

            String accessToken = initiateAuthResponse.authenticationResult().accessToken();
            return new String[]{"Authorization", "Bearer "+accessToken, "Content-Type", "application/json"};
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    private String calculateSecretHash() throws InvalidKeyException, NoSuchAlgorithmException {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        byte[] bytes = clientSecret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec signingKey = new SecretKeySpec(bytes, HMAC_SHA256_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(signingKey);
        mac.update(username.getBytes(StandardCharsets.UTF_8));
        byte[] rawHmac = mac.doFinal(clientId.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public  String sendNotification(String senderId, Long messageId)
            throws URISyntaxException, IOException, InterruptedException {

        if(!enable) return "Push Notifications is DISABLED";

        URI uri = new URI(String.format(notificationApi, senderId, messageId));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .headers(getHeaders())
                .uri(uri)
                .build();
        HttpResponse<Void> send = HttpClient.newHttpClient()
                .send(httpRequest, HttpResponse.BodyHandlers.discarding());
        return send.statusCode() == 200 ? "Notification sed" : "ERROR";

    }
}

