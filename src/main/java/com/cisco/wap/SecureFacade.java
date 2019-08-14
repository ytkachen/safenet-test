package com.cisco.wap;

import com.cisco.cca.facade.SecureEncryption;
import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.charset.Charset;

/**
 * @author Yuri Tkachenko
 */
public class SecureFacade {
    public static final String CHARSET_NAME_UTF8 = "UTF-8";
    public static final Charset CHARSET_UTF8 = Charset.forName(CHARSET_NAME_UTF8);

    public static final String SCENE_FROMCMC_DB_CONN_PWD = "FromCMC_DatabaseConnectionPassword";

    public static final String KEYTYPE_CMC_ViaFileKey = "CMCIntegrationFileKey";
    public static final String KEYTYPE_CMC_ViaKM = "APP_CMC_TOKEN";

    public static final String PREFIX_CMC_KM_ENCRYPTION = "||CMC_ENCRYPTION||";
    public static final String PREFIX_CMC_FK_ENCRYPTION = "||CMC_FK_ENCRYPTION||";

    public static final String DEPENDENCY_LOCATION = "/opt/webex/secureconnlib";
    public static final String ENCRYPTION_CLASS_FULLNAME = "com.cisco.cca.facade.SecureEncryption";

    private static final String METHODNAME_ENCRYPT = "encrypt";
    private static final String METHODNAME_DECRYPT = "decrypt";

    /**
     *
     * @param encryptedStr
     * @return
     */
    public static EncryptionType getCMCEncryptionType (String encryptedStr) {
        if (encryptedStr != null) {
            if (encryptedStr.trim().startsWith(PREFIX_CMC_KM_ENCRYPTION)) {
                return EncryptionType.ENCRYPT_WITH_KM;
            }

            if (encryptedStr.trim().startsWith(PREFIX_CMC_FK_ENCRYPTION)) {
                return EncryptionType.ENCRYPT_WITH_FILE_KEY;
            }
        }
        return EncryptionType.NONE;
    }

    /**
     *
     * @param inputPwd
     * @return
     * @throws Exception
     */
    public static String decryptPasswordWithKM(String inputPwd) throws Exception {
        return decryptPasswordWithKeyType(inputPwd, KEYTYPE_CMC_ViaKM, PREFIX_CMC_KM_ENCRYPTION);
    }

    /**
     *
     * @param inputPwd
     * @return
     * @throws Exception
     */
    public static String decryptPasswordWithFileKey(String inputPwd) throws Exception {
        return decryptPasswordWithKeyType(inputPwd, KEYTYPE_CMC_ViaFileKey, PREFIX_CMC_FK_ENCRYPTION);
    }

    /**
     *
     * @param inputString
     * @return
     * @throws Exception
     */
    public static String encryptPasswordWithKM(String inputString) throws Exception {
        return encryptPasswordWithKeyType(inputString, KEYTYPE_CMC_ViaKM, PREFIX_CMC_KM_ENCRYPTION);
    }

    /**
     *
     * @param inputString
     * @return
     * @throws Exception
     */
    public static String encryptPasswordWithFileKey(String inputString) throws Exception {
        return encryptPasswordWithKeyType(inputString, KEYTYPE_CMC_ViaFileKey, PREFIX_CMC_FK_ENCRYPTION);
    }

    private static String encryptPasswordWithKeyType(String inputString, String keyType, String prefix) throws Exception {
        byte[] inputBytes = inputString.getBytes(CHARSET_UTF8);
        byte[] outputBytes = conductEncryption(SCENE_FROMCMC_DB_CONN_PWD, inputBytes, keyType);

        String encryptedStr = DatatypeConverter.printBase64Binary(outputBytes);
        String cmcEncryptStr = prefix + encryptedStr;
        return cmcEncryptStr;
    }

    private static String decryptPasswordWithKeyType(String inputPwd, String keyType, String prefix) throws Exception {
        String encryptedStr = inputPwd;
        if (prefix != null) {
            encryptedStr = inputPwd.substring(prefix.length());
        }
        byte[] inputBytes = DatatypeConverter.parseBase64Binary(encryptedStr);
        byte[] plaintextBytes = conductDecryption(SCENE_FROMCMC_DB_CONN_PWD, inputBytes, keyType);
        return new String(plaintextBytes, CHARSET_UTF8);
    }

    private static byte[] conductEncryption(String scene, byte[] inputBytes, String keyType) throws Exception {
        return invokeCryptoMethodFromSecUtilLib(scene, inputBytes, keyType, true);
    }

    private static byte[] conductDecryption(String scene, byte[] inputBytes, String keyType) throws Exception {
        return invokeCryptoMethodFromSecUtilLib(scene, inputBytes, keyType, false);
    }

    private static byte[] invokeCryptoMethodFromSecUtilLib(String scene, byte[] inputBytes, String keyType, boolean isEncrypt) throws Exception {
        byte[] result = invokeCryptoMethodViaExtLibClassLoader(scene, inputBytes, keyType, isEncrypt);
        if (result == null) {
            result = invokeCryptoMethodViaDefaultClassLoader(scene, inputBytes, keyType, isEncrypt);
        }
        return result;
    }

    private static byte[] invokeCryptoMethodViaDefaultClassLoader(String scene, byte[] inputBytes, String keyType, boolean isEncrypt) throws Exception {
        if (isEncrypt) {
            return SecureEncryption.encrypt(scene, inputBytes, keyType);
        }
        return SecureEncryption.decrypt(scene, inputBytes, keyType);
    }

    private static byte[] invokeCryptoMethodViaExtLibClassLoader(String scene, byte[] inputBytes, String keyType, boolean isEncrypt) throws Exception {
        ExtLibClassLoaderContainer container = new ExtLibClassLoaderContainer(DEPENDENCY_LOCATION);
        try {
            URLClassLoader loader = container.getURLClassLoader();
            if (loader == null) {
                return null;
            }

            Class<?> clazz = loader.loadClass(ENCRYPTION_CLASS_FULLNAME);
            if (clazz == null) {
                return null;
            }
            Method method = clazz.getMethod(isEncrypt ? METHODNAME_ENCRYPT : METHODNAME_DECRYPT, String.class, byte[].class, String.class);
            return (byte[]) method.invoke(null, scene, inputBytes, keyType);
        } finally {
            container.destroyClassLoader();
        }
    }
}