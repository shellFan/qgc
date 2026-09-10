package com.qiongguichou.payment.config;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WxPayPublicKeyVerifierTest {

    @Test
    public void verifiesWechatSignatureWithConfiguredPublicKeyId() throws Exception {
        KeyPair keyPair = generateKeyPair();
        Path publicKey = Files.createTempFile("wechat-pay-public-key", ".pem");
        Files.write(publicKey, pem(keyPair).getBytes(StandardCharsets.US_ASCII));

        WxPayPublicKeyVerifier verifier = new WxPayPublicKeyVerifier("PUBLIC_KEY_ID", publicKey.toString());
        byte[] message = "wechat-pay-response".getBytes(StandardCharsets.UTF_8);
        String signature = sign(keyPair, message);

        assertTrue(verifier.verify("PUBLIC_KEY_ID", message, signature));
        assertFalse(verifier.verify("OTHER_KEY_ID", message, signature));
        assertFalse(verifier.verify("PUBLIC_KEY_ID", "tampered".getBytes(StandardCharsets.UTF_8), signature));
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String pem(KeyPair keyPair) {
        return "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyPair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----\n";
    }

    private static String sign(KeyPair keyPair, byte[] message) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(message);
        return Base64.getEncoder().encodeToString(signer.sign());
    }
}
