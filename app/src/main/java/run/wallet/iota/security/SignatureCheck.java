package run.wallet.iota.security;

/**
 * Created by coops on 27/01/18.
 */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SignatureCheck {

    //we store the hash of the signture for a little more protection
    private static final String APP_SIGNATURE = "5296792B809B73ED126366C08298CDE97D9545E1";
    private static final String APP_SIGNATURE2="52:96:79:2B:80:9B:73:ED:12:63:66:C0:82:98:CD:E9:7D:95:45:E1";

    /**
     * Query the signature for this application to detect whether it matches the
     * signature of the real developer. If it doesn't the app must have been
     * resigned, which indicates it may been tampered with.
     *
     * @param context
     * @return true if the app's signature matches the expected signature.
     * @throws NameNotFoundException
     */
    public static boolean validateAppSignature(Context context)  {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            //packageInfo.
            //note sample just checks the first signature
            for (Signature signature : packageInfo.signatures) {
                // SHA1 the signature
                //Log.e("SIG", String.valueOf(signature.toByteArray()));
                String sha1 = getSHA1(signature.toByteArray());
                //Log.e("SIG", sha1);
                //Log.e("SIG", getCertificateSHA1Fingerprint(context));

                // check is matches hardcoded value
                return APP_SIGNATURE.equals(sha1);
            }
        } catch (Exception e) {
            Log.e("SIG", "ex: "+e.getMessage());
        }
        return false;
    }

    //computed the sha1 hash of the signature
    public static String getSHA1(byte[] sig) {
        MessageDigest digest = null;
        try {
            digest=MessageDigest.getInstance("SHA1", "BC");
            digest.update(sig);
            byte[] hashtext = digest.digest();
            return bytesToHex(hashtext);
        } catch (Exception e) {
            Log.e("SIG", "ex: "+e.getMessage());
        }
        return null;
    }

    //util method to convert byte array to hex string
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static String getCertificateSHA1Fingerprint(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        String packageName = mContext.getPackageName();
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    public static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }
}