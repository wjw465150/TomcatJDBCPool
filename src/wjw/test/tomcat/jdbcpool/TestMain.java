package wjw.test.tomcat.jdbcpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.ext.TomcatDataSourceFactory;

//@wjw_note: 要使用Tomcat的ClassLoaderLogManager来替代JDK的LogManager,必须在启动java虚拟机时
//传递参数:-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager Djava.util.logging.config.file=./logging.properties
public class TestMain {
  private static Log log = LogFactory.getLog(TestMain.class);

  public static void main(String[] args) {
    try {
      log.info("开始:" + TestMain.class.getCanonicalName());

      javax.sql.DataSource datasource = TomcatDataSourceFactory.createDataSource("classpath:resource-jdbc.xml");
      log.info("TomcatDataSource:" + datasource);

      Connection con = null;
      try {
        con = datasource.getConnection();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("select * from user");
        int cnt = 1;
        while (rs.next()) {
          log.info((cnt++) + ". Host:" + rs.getString("Host") +
              " User:" + rs.getString("User") + " Password:" + rs.getString("Password"));
        }
        rs.close();
        st.close();
        Thread.sleep(1000 * 100);
      } finally {
        if (con != null) {
          try {
            con.close();
          } catch (Exception ignore) {
          }
        }

        if (datasource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
          ((org.apache.tomcat.jdbc.pool.DataSource) datasource).close();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
