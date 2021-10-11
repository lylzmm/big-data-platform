package utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.Serializable;
import java.net.URI;
import java.sql.*;
import java.util.Properties;


public class JdbcUtils implements Serializable {


    private static String url;
    private static String user;
    private static String password;


    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static String getPassword() {
        return password;
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


    public static Connection getConnection(String configName, int jump_port) throws Exception {
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://node01:9820"), new Configuration(), "hadoop");
        FSDataInputStream fsDataInputStream = fileSystem.open(new Path("hdfs://node01:9820/resource/" + configName));
        Properties properties = new Properties();
        properties.load(fsDataInputStream);

        // 通过SSH连接mysql =================================================
        // 数据库
        String db_ip = properties.getProperty("ip");
        int db_port = Integer.parseInt(properties.getProperty("port"));
        String db_name = properties.getProperty("db");
        String db_username = properties.getProperty("name");
        String db_password = properties.getProperty("password");

        // 转发的服务器
        String host_ip = properties.getProperty("host_ip");
        int host_port = Integer.parseInt(properties.getProperty("host_port"));
        String host_name = properties.getProperty("host_name");
        String host_password = properties.getProperty("host_password");

        // 转发的端口
        int localPort = OpenSSH.openSSH(host_ip, host_port, host_name, host_password, db_ip, db_port, jump_port);

        String driverClass = "com.mysql.jdbc.Driver";
        url = "jdbc:mysql://" + db_ip + ":" + localPort + "/" + db_name + "?characterEncoding=utf8&useSSL=false";
        user = db_username;
        password = db_password;

        Class.forName(driverClass);//注册加载驱动

        return DriverManager.getConnection(url, user, password);
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

        OpenSSH.closeSSH();
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


}
