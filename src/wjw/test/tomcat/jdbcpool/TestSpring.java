package wjw.test.tomcat.jdbcpool;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestSpring {
  private static Log log = LogFactory.getLog(TestSpring.class);

  public static void main(String[] args) {
    try {
      java.util.logging.LogManager.getLogManager().readConfiguration(new FileInputStream("./logging.properties")); //java.util.logging.LogManager.getLogManager().readConfiguration(TestMain.class.getResourceAsStream("/logging.properties"));
      log.info("¿ªÊ¼:" + TestSpring.class.getCanonicalName());

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
