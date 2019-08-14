package com.cisco.wap;

/**
 * @author Yuri Tkachenko
 */
public enum EncryptionType {
    NONE(0),
    ENCRYPT_WITH_FILE_KEY(1),
    ENCRYPT_WITH_KM(2);

    private int value;

    public int getValue() {
        return value;
    }

    private EncryptionType (int value) {
        this.value = value;
    }
}