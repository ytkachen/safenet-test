package com.cisco.wap;

/**
 * @author Yuri Tkachenko
 */
public class EncDecUtil {
    private EncDecUtil() {
    }

    public static String decryptPassword(String password) throws Exception {
        String decryptedPwd = null;
        if (password == null) {
            return null;
        }

        EncryptionType type = SecureFacade.getCMCEncryptionType(password);
        switch (type) {
            case ENCRYPT_WITH_KM:
                decryptedPwd = SecureFacade.decryptPasswordWithKM(password.trim());
                break;

            case ENCRYPT_WITH_FILE_KEY:
                decryptedPwd = SecureFacade.decryptPasswordWithFileKey(password.trim());
                break;

            case NONE:
            default:
                decryptedPwd = password;
                break;
        }
        return decryptedPwd;
    }
}
