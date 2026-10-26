package ec.gob.cce.global;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;

/**
 * Librería que descrifra tokens con su respectiva firma.
 * A la misma vez, descifra con la llave privada el sub-encriptado con la llave pública.
 * Fuente: https://www.jokecamp.com/blog/examples-of-creating-base64-hashes-using-hmac-sha256-in-different-languages/
 *
 * @author Wilfredo Martel
 */
public class DescifradorUtil {
    static final String ALGORITHM = "HmacSHA256";
    static final String SHA256 =  ServerConfiguration.getInstance().getSHA256Decifrador();// "@M5gD?CX4Hn#TDbdrT@#Mtk25F0!FS%baUI";
    static final String PRIVATE_KEY = ServerConfiguration.getInstance().getPrivateKeyDecifrador();
//            "MIICXQIBAAKBgQDObZtjxfplZYRgo6TKZM9E6b3RVQpXTpKTOiqefTKEpT9//ru1\r\n" + 
//            "x0rHgqpsjcw1BoXFX3SuYRPn3ijCM/C9WHnc2PDjEgGu0KezIxvqE7nCjbHed7pf\r\n" + 
//            "f6fov6ZajFsiwcf2r3oOwCjWMW1ChHP0ZYF2Ai1HmInarJutHTwE+Elb3QIDAQAB\r\n" + 
//            "AoGAHLkVmRFwIPG6NLQwdtUGHiGj/t+lW7acII5EZd8ny1su9cFdHxMG7bHZwtcM\r\n" + 
//            "JgitTmRU2Pq7CVVZOIR/p+kKs5cPE6gH5UGlYi0MTZV3sD4hSWMRs+baNiRkaWLE\r\n" + 
//            "COxk0o4Xe80StheoL89K2dIwFrc28BAowdvrHHjeqvYRsgECQQDymDTCXjoHGAhG\r\n" + 
//            "T77gnAVmOY9QsB00mIeVxC06LHwC1TfM51k8QgalUDIp/pxaCZp5F0l4Uy2Tzg+B\r\n" + 
//            "phr2+OVdAkEA2dXIh0rSJM8zw312MJT5YFwNkDpgmMN/aN1FPAVHtLVoMn8d7p8p\r\n" + 
//            "w7DUEQ5sl+Sk5XwWWa9AEckkG3SRW3BogQJBAL5Flwvj78tklAjhvypX9PwqpTd6\r\n" + 
//            "Ck4YXC+hQH/iKBnote1mftz+REwgzFeXtXYBFkFnfF59jr/g3NSpPXj72pkCQCan\r\n" + 
//            "LZ78Is/PSIMexxMVzC5SB0IZabyRrBECel+NHE0vh162eaw25+VGgkrIgXJuaugh\r\n" + 
//            "naGqXDcLtvF8PLK5/oECQQDkQl/IAPBbI5QXLmfiseu7duRtZ9px/7HYDOEDtjia\r\n" + 
//            "sOyBGW2Ml7FYxYTPJX6uIhxfIpX1Bt1Kdtue3Wu3OMRC";

    public static String decrypt(String data, String base64PrivateKey)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        return decryptAlg(java.util.Base64.getDecoder().decode(data.getBytes()), getPrivateKey(base64PrivateKey));
    }

    public static String decryptAlg(byte[] data, PrivateKey privateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(data));
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        base64PrivateKey = base64PrivateKey.replaceAll("\\s+", "");

        final byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public String descifrarMensaje(String token)
            throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {

        final String payLoad = token.split("\\.")[1];
        final String decodedString = new String(Base64.getDecoder().decode(payLoad), StandardCharsets.UTF_8);
        final JsonObject json = new JsonParser().parse(decodedString).getAsJsonObject();
        final String datoCifrado = json.get("dato").getAsString();
        return decrypt(datoCifrado, PRIVATE_KEY);
    }

    public Boolean verificarFirmaToken(String token)
            throws NoSuchAlgorithmException, InvalidKeyException {

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String header = parts[0];
        String payload = parts[1];
        String tokenSignature = parts[2];

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey =
                new SecretKeySpec(SHA256.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        hmacSha256.init(secretKey);

        String data = header + "." + payload;

        byte[] computed = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));

        String computedSignature =
                Base64.getUrlEncoder()
                      .withoutPadding()
                      .encodeToString(computed);

        return computedSignature.equals(tokenSignature);
    }
    
   
}
