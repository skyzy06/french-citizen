package net.atos.frenchcitizen.util;

import net.atos.frenchcitizen.exception.InternalServerErrorException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * source: https://www.oodlestechnologies.com/blogs/Encrypt-And-Decrypt-In-Java/
 */
public class PasswordUtils {

    private static SecretKeySpec secretKey;

    private PasswordUtils() {
    }

    public static void setKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new InternalServerErrorException("setKey", e.getClass().getName());
        }
    }

    public static String encrypt(String stringToEncrypt, String secret) {
        setKey(secret);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new InternalServerErrorException("encrypt", e.getMessage());
        }
    }

    public static String decrypt(String stringToDecrypt, String secret) {
        setKey(secret);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(stringToDecrypt)));
        } catch (Exception e) {
            throw new InternalServerErrorException("decrypt", e.getMessage());
        }
    }
}
