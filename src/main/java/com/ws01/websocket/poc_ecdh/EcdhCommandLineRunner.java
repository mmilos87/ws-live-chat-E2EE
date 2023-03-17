package com.ws01.websocket.poc_ecdh;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EcdhCommandLineRunner // implements CommandLineRunner
{

//    private KeyService keyService;
//    private SymmetricEncryptionService encryptionService;
//
//    @Autowired
//    public EcdhCommandLineRunner(SymmetricEncryptionService encryptionService, KeyService keyService) {
//        this.encryptionService = encryptionService;
//        this.keyService = keyService;
//    }

//    @Override
//    public void run(String... args) throws Exception {
//        // Generate an ephemeral PKCS8 key pair
//        final KeyPair aliceKeyPair = keyService.generateKeyPair();
//        final KeyPair aliceKeyPairE = keyService.generateKeyPair();
//
//
//        final KeyPair bobKeyPair = keyService.generateKeyPair();
//        final KeyPair bobKeyPairE = keyService.generateKeyPair();
//
//
//        // Extract the public and private keys
//
//        // alice Identity keys
//        final ECPublicKey alicePubI = (ECPublicKey) aliceKeyPair.getPublic();
//        final ECPrivateKey alicePrivI = (ECPrivateKey) aliceKeyPair.getPrivate();
//
//        // alice ephemeral keys
//        final ECPublicKey alicePubE = (ECPublicKey) aliceKeyPairE.getPublic();
//        final ECPrivateKey alicePrivE = (ECPrivateKey) aliceKeyPairE.getPrivate();
//
//        // boob identity keys
//        final ECPublicKey bobPubI = (ECPublicKey) bobKeyPair.getPublic();
//        final ECPrivateKey bobPrivI = (ECPrivateKey) bobKeyPair.getPrivate();
//
//        // bob ephemeral keys
//        final ECPublicKey bobPubE = (ECPublicKey) bobKeyPairE.getPublic();
//        final ECPrivateKey bobPrivE = (ECPrivateKey) bobKeyPairE.getPrivate();
//
//        // Alice computed keys
//        final byte[] secretAlice1 = keyService.deriveSharedSecret(bobPubI.getEncoded(), alicePrivE);
//        final byte[] secretAlice2 = keyService.deriveSharedSecret(bobPubE.getEncoded(), alicePrivI);
//        final byte[] secretAlice3 = keyService.deriveSharedSecret(bobPubE.getEncoded(), alicePrivE);
//        log.info("Alice's secret 1: {}", Hex.encodeHexString(secretAlice1));
//        log.info("Alice's secret 1-: {}", secretAlice1);
//        log.info("Alice's secret 1--: {}", Hex.decodeHex(Hex.encodeHexString(secretAlice1)));
//        log.info("Alice's secret 2: {}", Hex.encodeHexString(secretAlice2));
//        log.info("Alice's secret 3: {}", Hex.encodeHexString(secretAlice3));
//
//
//        // boob computed keys
//        final byte[] secretBob1 = keyService.deriveSharedSecret(alicePubE.getEncoded(), bobPrivI);
//        final byte[] secretBob2 = keyService.deriveSharedSecret(alicePubI.getEncoded(), bobPrivE);
//        final byte[] secretBob3 = keyService.deriveSharedSecret(alicePubE.getEncoded(), bobPrivE);
//        log.info("Bob's secret 1: {}", Hex.encodeHexString(secretBob1));
//        log.info("Bob's secret 2: {}", Hex.encodeHexString(secretBob2));
//        log.info("Bob's secret 3: {}", Hex.encodeHexString(secretBob3));
//
//
//
//
//        // Derive a shared secret using the generated keys
//        final byte[] secret1 = keyService.deriveSharedSecret(bobPubI.getEncoded(), alicePrivI);
//        final byte[] secret2 = keyService.deriveSharedSecret(alicePubI.getEncoded(), bobPrivI);
//
//        // Print the secrets
//        log.info("Do both the secrets match? {}", Hex.encodeHexString(secret1).equals(Hex.encodeHexString(secret2)));
//        log.info("Alice's secret: {}", Hex.encodeHexString(secret1));
//        log.info("Bob's secret: {}", Hex.encodeHexString(secret2));
//        log.info("Bob's secret s: {}", secret1);
//        log.info("Bob's secret ss: {}", secret2);
//
//        // Encrypt some text.
//        final byte[] content = encryptionService.encrypt(secret1, "This is a test".getBytes(StandardCharsets.UTF_8));
//        log.info("Encrypted: {}", new String(content));
//        log.info("Decrypted: {}", new String(encryptionService.decrypt(secret2, content)));
//    }

}