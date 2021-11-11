package net.atos.frenchcitizen.helper;

import net.atos.frenchcitizen.exception.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * source: https://www.oodlestechnologies.com/blogs/Encrypt-And-Decrypt-In-Java/
 */
@Component
public class PasswordHelper {

    private SecretKeySpec secretKey;
    @Value("${encryption.password.key}")
    private String encryptionKey;

    public void setKey() {
        MessageDigest sha;
        try {
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new InternalServerErrorException("setKey", e.getClass().getName());
        }
    }

    public String encrypt(String password) {
        setKey();
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new InternalServerErrorException("encrypt", e.getMessage());
        }
    }

    public String decrypt(String encodedPassword) {
        setKey();
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encodedPassword)));
        } catch (Exception e) {
            throw new InternalServerErrorException("decrypt", e.getMessage());
        }
    }
}
