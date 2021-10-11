package utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class KerberosUtils {

    public static void loginUserFromKeytab(String model) {
        String name = "";
        if ("local".equals(model)) name = "local";
        if ("cluster".equals(model)) name = "cluster";
        System.out.println(PropertyUtils.getProperty("defaultFS"));
        System.setProperty("java.security.krb5.conf", PropertyUtils.getProperty(name + ".krb5.path"));
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", PropertyUtils.getProperty("defaultFS"));
        conf.setBoolean("hadoop.security.authorization", true);
        conf.set("hadoop.security.authentication", "Kerberos");
        try {
            System.out.println("");
            String user = PropertyUtils.getProperty(name + ".user");
            String path = PropertyUtils.getProperty(name + ".path");
            System.out.println("=================================================" + user + "===============" + path);
            UserGroupInformation.loginUserFromKeytab(user, path);
        } catch (IOException e) {
            System.out.println("登录失败");
            e.printStackTrace();
        }
    }
}
