package utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.Serializable;
import java.net.URI;
import java.sql.*;
import java.util.Properties;


public class JdbcUtils implements Serializable {
    private static Session session;

    private static String url;


    public static String getUrl() {
        return url;
    }


    /**
     * 获取 Connetion
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection(String url, Properties properties) throws Exception {
        String db_username = properties.getProperty("user");
        String db_password = properties.getProperty("password");
        return DriverManager.getConnection(url, db_username, db_password);
    }


    public static Connection getConnection() throws Exception {
        // 通过SSH连接mysql =================================================

        // 数据库
        String db_ip = PropertyUtils.getProperty("ip");
        int db_port = Integer.parseInt(PropertyUtils.getProperty("port"));
        String db_name = PropertyUtils.getProperty("db");
        String db_username = PropertyUtils.getProperty("name");
        String db_password = PropertyUtils.getProperty("password");

        int localPort = getLocalPort();
        // 转发的服务器
        url = "jdbc:mysql://" + db_ip + ":" + localPort + "/" + db_name + "?characterEncoding=utf8&useSSL=false";

        Class.forName("com.mysql.jdbc.Driver");//注册加载驱动

        return DriverManager.getConnection(url, db_username, db_password);
    }


    /**
     * 关闭ResultSet资源
     *
     * @param rs
     */

    public static void close(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭Statement资源
     *
     * @param stmt
     */

    public static void close(Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭close资源
     *
     * @param conn
     */
    public static void close(Connection conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        session.disconnect();
    }

    /**
     * 关闭资源
     *
     * @param rs
     * @param stmt
     * @param conn
     */

    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        close(rs);
        close(stmt);
        close(conn);
    }

    public static void closeSSH() {
        session.disconnect();
    }

    private static int getLocalPort() throws Exception {
        String db_ip = PropertyUtils.getProperty("ip");
        int db_port = Integer.parseInt(PropertyUtils.getProperty("port"));

        int i = (int) (1 + Math.random() * 10);
        int jump_port = Integer.parseInt("331" + i);
        System.out.println(jump_port);

        String host_ip = PropertyUtils.getProperty("host_ip");
        int host_port = Integer.parseInt(PropertyUtils.getProperty("host_port"));
        String host_name = PropertyUtils.getProperty("host_name");
        String host_password = PropertyUtils.getProperty("host_password");
        // 转发的端口
        JSch jSch = new JSch();
        session = jSch.getSession(host_name, host_ip, host_port);
        session.setPassword(host_password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        System.out.println(session.getServerVersion());
        int assinged_port = session.setPortForwardingL(jump_port, db_ip, db_port);
        System.out.println("localhost:" + assinged_port);
        return assinged_port;

    }

}
