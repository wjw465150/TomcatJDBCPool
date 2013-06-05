package wjw.test.tomcat.jdbcpool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

//@wjw_note: 要使用Tomcat的ClassLoaderLogManager来替代JDK的LogManager,必须在启动java虚拟机时
//传递参数:-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager Djava.util.logging.config.file=./logging.properties
public class TestSpring {
  private static Log log = LogFactory.getLog(TestSpring.class);

  public static void main(String[] args) {
    try {
      log.info("开始:" + TestSpring.class.getCanonicalName());

      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

      javax.sql.DataSource datasource = (javax.sql.DataSource) context.getBean("dataSource");
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
        Thread.sleep(1000 * 10);
      } finally {
        if (con != null) {
          try {
            con.close();
          } catch (Exception ignore) {
          }
        }

        context.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
