package com.ws01.websocket.poc_ecdh;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@NoArgsConstructor
@Service
public class KeyService {

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256); // Set p=256

        return generator.generateKeyPair();
    }

    public byte[] deriveSharedSecret(byte[] otherPublicKey, ECPrivateKey yourPrivateKey) throws
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        final KeyFactory ec = KeyFactory.getInstance("EC");
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(otherPublicKey);
        final PublicKey publicKey = ec.generatePublic(keySpec);
        KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
        ecdh.init(yourPrivateKey);
        ecdh.doPhase(publicKey, true);

        return ecdh.generateSecret();
    }

}

