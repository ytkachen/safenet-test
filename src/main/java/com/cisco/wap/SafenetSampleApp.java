package com.cisco.wap;

/**
 * @author Yuri Tkachenko
 */
public class SafenetSampleApp {

    public static void main(String[] args) throws Exception {
        String v = "||CMC_ENCRYPTION||RzH//wAAAAF4BPv/Gpr/UDwicwdCMTMefEo2sL4uqHumXKVk8Ugj5jV1GPdSNFGQ/xpIkOv7b5EBLCCNmv+jc3kj8NeyAP7h";
        System.out.println(v);
        System.out.println(EncDecUtil.decryptPassword(v));
    }

}
