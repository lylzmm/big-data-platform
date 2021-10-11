package utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class OpenSSH {
    private static Session session;


    public static int openSSH(String host, int port, String username, String password, String db_ip, int db_port,int jump_port) throws Exception {

        JSch jSch = new JSch();
        session = jSch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        System.out.println(session.getServerVersion());
        int assinged_port = session.setPortForwardingL(jump_port, db_ip, db_port);
        System.out.println("localhost:" + assinged_port);
        return assinged_port;

    }

    public static Session getSession() {
        return session;
    }

    public static void closeSSH() {
        session.disconnect();
    }
}
