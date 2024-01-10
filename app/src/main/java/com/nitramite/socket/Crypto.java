package com.nitramite.socket;

import org.spongycastle.jcajce.provider.digest.SHA3;
import org.spongycastle.util.encoders.Hex;
import java.security.Security;
public class Crypto {
    private static final String TAG = Crypto.class.getSimpleName();
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }
    public static String sha3_512(final String password) {
        SHA3.DigestSHA3 sha3 = new SHA3.Digest512();
        byte[] digest = sha3.digest(password.getBytes());
        return Hex.toHexString(digest);
    }

} 