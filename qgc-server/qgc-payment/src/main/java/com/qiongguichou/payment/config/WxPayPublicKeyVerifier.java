package com.qiongguichou.payment.config;

import com.github.binarywang.wxpay.v3.auth.Verifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** Verifies API v3 responses with the merchant-platform public key. */
final class WxPayPublicKeyVerifier implements Verifier {

    private final String publicKeyId;
    private final PublicKey publicKey;

    WxPayPublicKeyVerifier(String publicKeyId, String publicKeyPath) {
        if (publicKeyId == null || publicKeyId.trim().isEmpty()) {
            throw new IllegalArgumentException("微信支付公钥ID不能为空");
        }
        this.publicKeyId = publicKeyId.trim();
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    @Override
    public boolean verify(String serialNumber, byte[] message, String signature) {
        if (!publicKeyId.equals(serialNumber) || message == null || signature == null) {
            return false;
        }
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(message);
            return verifier.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    /** A public-key verifier has no X509 certificate object to expose. */
    @Override
    public X509Certificate getValidCertificate() {
        return null;
    }

    private static PublicKey loadPublicKey(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("微信支付公钥路径不能为空");
        }
        try {
            String pem = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.US_ASCII)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] encoded = Base64.getDecoder().decode(pem);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
        } catch (Exception e) {
            throw new IllegalStateException("无法读取微信支付公钥: " + path, e);
        }
    }
}
