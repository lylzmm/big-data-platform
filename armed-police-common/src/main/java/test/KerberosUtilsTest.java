package test;

import utils.KerberosUtils;

public class KerberosUtilsTest {

    public static void main(String[] args) {
        KerberosUtils.loginUserFromKeytab("cluster");
    }
}
