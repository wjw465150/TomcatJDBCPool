package org.apache.tomcat.jdbc.ext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * @author wjw465150@gmail.com<br/>
 *         从XML格式的配置文件里,或者java.util.Properties对象里生成并配置好org.apache.tomcat.jdbc.pool.DataSource<br/>
 *         Tomcat自己的 org.apache.tomcat.jdbc.pool.DataSourceFactory,
 *         是可以使用createDataSource方法从Properties创建org
 *         .apache.tomcat.jdbc.pool.DataSource<br/>
 *         org.apache.tomcat.jdbc.pool.
 *         DataSourceFactory的缺点是不校验Properties里的键名是否正确,这样会导致创建的DataSource没有正确配置.<br/>
 *         TomcatDataSourceFactory类的目的就是能从兼容tomcat的server.xml的<Resource>标签里创建org
 *         .apache.tomcat.jdbc.pool.DataSource;<br/>
 *         并把<Resource>标签里的属性值赋给org.apache.tomcat.jdbc.pool.DataSource,<br/>
 *         同时校验<Resource>里的属性名是否存在于tomcat jdbc
 *         pool的属性集里,如果不存在会给出警告,防止配置属性名时出现的输入此错误!<br/>
 * 
 *         <pre>
 * <font color="#000000"><span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 1 </font></span>&#20363;&#22914;:&#23646;&#24615;&#25991;&#20214;&#21517;:resource-jdbc.xml,&#20869;&#23481;&#26159;:
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 2 </font></span>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 3 </font></span>&lt;!--
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 4 </font></span>  说&#26126;: tomcat jdbc pool&#30340;&#21442;&#25968;,&#26082;&#21487;&#20197;&#20316;&#20026;Resource&#30340;&#23646;&#24615;,&#20063;&#21487;&#20197;&#20316;&#20026;Resource&#30340;&#23376;&#20803;&#32032;;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066"> 5 </font></span>  &#20363;&#22914;: &lt;Resource driverClassName=&quot;com.mysql.jdbc.Driver&quot;/&gt;  
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 6 </font></span>     &#25110;者 &lt;Resource&gt;&lt;driverClassName&gt;com.mysql.jdbc.Driver&lt;/driverClassName&gt;&lt;/Resource&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 7 </font></span>    &#22914;&#26524;&#21516;&#26102;&#20986;&#29616;,那&#20040;&#20803;&#32032;&#20540;&#20250;覆&#30422;&#23646;&#24615;&#20540;.
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 8 </font></span>--&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 9 </font></span>&lt;Resource name=&quot;jdbc/TestDB&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">10 </font></span>              auth=&quot;Container&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">11 </font></span>              type=&quot;javax.sql.DataSource&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">12 </font></span>              factory=&quot;org.apache.tomcat.jdbc.pool.DataSourceFactory&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">13 </font></span>              testWhileIdle=&quot;true&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">14 </font></span>              testOnBorrow=&quot;true&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">15 </font></span>              testOnReturn=&quot;false&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">16 </font></span>              validationQuery=&quot;SELECT 1&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">17 </font></span>              validationInterval=&quot;30000&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">18 </font></span>              timeBetweenEvictionRunsMillis=&quot;30000&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">19 </font></span>              maxActive=&quot;100&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">20 </font></span>              minIdle=&quot;10&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">21 </font></span>              maxWait=&quot;10000&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">22 </font></span>              initialSize=&quot;10&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">23 </font></span>              removeAbandonedTimeout=&quot;60&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">24 </font></span>              removeAbandoned=&quot;true&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">25 </font></span>              logAbandoned=&quot;true&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">26 </font></span>              minEvictableIdleTimeMillis=&quot;30000&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">27 </font></span>              jmxEnabled=&quot;true&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">28 </font></span>              username=&quot;root&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">29 </font></span>              password=&quot;masterkey&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">30 </font></span>              driverClassName=&quot;com.mysql.jdbc.Driver&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">31 </font></span>              url=&quot;jdbc:mysql://127.0.0.1:3306/mysql&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">32 </font></span>              &gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">33 </font></span>  &lt;jdbcInterceptors&gt;StatementFinalizer;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">34 </font></span>  SlowQueryReportJmx(notifyPool=false,maxQueries=1000,threshold=1)&lt;/jdbcInterceptors&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">35 </font></span>&lt;/Resource&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">36 </font></span>java&#20195;&#30721;&#26159;:      javax.sql.DataSource datasource = TomcatDataSourceFactory.createDataSource(&quot;classpath:resource-jdbc.xml&quot;);
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">37 </font></span>
 * </font>
 * </pre>
 * 
 *         <pre>
 * &#22312;Spring&#29615;&#22659;里&#30340;配&#32622;&#26041;&#24335;&#22914;&#22914;&#19979;:
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">2 </font></span>&lt;bean id=&quot;dataSource&quot; class=&quot;org.apache.tomcat.jdbc.ext.TomcatDataSourceFactory&quot; 
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">3 </font></span>        factory-method=&quot;createDataSource&quot;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">4 </font></span>        destroy-method=&quot;close&quot;&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">5 </font></span>    &lt;constructor-arg value=&quot;classpath:resource-jdbc.xml&quot; /&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">6 </font></span>  &lt;/bean&gt;
 * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">7 </font></span>
 * </font>
 * </pre>
 */
public abstract class TomcatDataSourceFactory {
  private static final transient Log _log = LogFactory.getLog(TomcatDataSourceFactory.class);

  /** Pseudo URL prefix for loading from the class path: "classpath:" */
  public static final String CLASSPATH_URL_PREFIX = "classpath:";

  /** URL prefix for loading from the file system: "file:" */
  public static final String FILE_URL_PREFIX = "file:";

  /** URL protocol for a file in the file system: "file" */
  public static final String URL_PROTOCOL_FILE = "file";

  /** URL protocol for an entry from a jar file: "jar" */
  public static final String URL_PROTOCOL_JAR = "jar";

  /** URL protocol for an entry from a zip file: "zip" */
  public static final String URL_PROTOCOL_ZIP = "zip";

  /** URL protocol for an entry from a JBoss jar file: "vfszip" */
  public static final String URL_PROTOCOL_VFSZIP = "vfszip";

  /** URL protocol for an entry from a WebSphere jar file: "wsjar" */
  public static final String URL_PROTOCOL_WSJAR = "wsjar";

  /** URL protocol for an entry from an OC4J jar file: "code-source" */
  public static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

  /** Separator between JAR URL and file path within the JAR */
  public static final String JAR_URL_SEPARATOR = "!/";

  private static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    } catch (Throwable ex) {
      // Cannot access thread context ClassLoader - falling back to system class loader...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = TomcatDataSourceFactory.class.getClassLoader();
    }
    return cl;
  }

  private static URL getURL(String resourceLocation) throws FileNotFoundException {
    if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
      String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
      URL url = getDefaultClassLoader().getResource(path);
      if (url == null) {
        String description = "class path resource [" + path + "]";
        throw new FileNotFoundException(
            description + " cannot be resolved to URL because it does not exist");
      }
      return url;
    }
    try {
      // try URL
      return new URL(resourceLocation);
    } catch (MalformedURLException ex) {
      // no URL -> treat as file path
      try {
        return new File(resourceLocation).toURI().toURL();
      } catch (MalformedURLException ex2) {
        throw new FileNotFoundException("Resource location [" + resourceLocation +
            "] is neither a URL not a well-formed file path");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Properties loadCofig(InputStream input, String encoding)
      throws XMLStreamException {
    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    XMLEventReader xmler = xmlif.createXMLEventReader(input, encoding);
    Properties properties = new Properties();
    try {
      XMLEvent event;
      String rootPath = "";
      StartElement startElement;
      EndElement endElement;
      javax.xml.stream.events.Attribute attr;
      StringBuilder sbPath = new StringBuilder();
      StringBuilder sbText = new StringBuilder();
      while (xmler.hasNext()) {
        event = xmler.nextEvent();
        if (event.isStartElement()) { //如果解析的是起始标记
          startElement = event.asStartElement();
          if (rootPath.length() == 0) {
            rootPath = "/" + startElement.getName().getLocalPart();
          }
          sbPath.append("/").append(startElement.getName().getLocalPart());
          sbText.setLength(0); //重置sbText

          if (sbPath.toString().equals(rootPath)) {
            java.util.Iterator<javax.xml.stream.events.Attribute> iterator = startElement.getAttributes();
            while (iterator.hasNext()) {
              attr = iterator.next();
              properties.put(attr.getName().getLocalPart(), attr.getValue());
            }
          }
        } else if (event.isCharacters()) { //如果解析的是文本内容
          sbText.append(event.asCharacters().getData());
        } else if (event.isEndElement()) { //如果解析的是结束标记
          endElement = event.asEndElement();
          if (!sbPath.toString().equals(rootPath)) {
            String myText = sbText.toString().replaceAll("\n\r", "\n"); //防止StAX的对CDATA数据的一个bug
            //myText = myText.replaceAll("[\n\r]", "");  //去掉回车换行
            sbText.setLength(0); //重置sbText
            properties.put(endElement.getName().getLocalPart(), myText);
          }

          sbPath.delete(sbPath.lastIndexOf("/" + endElement.getName()), sbPath.length());
        }
      }
    } finally {
      xmler.close();
    }
    return properties;
  }

  /**
   * 从XML配置文件里创建并配置org.apache.tomcat.jdbc.pool.DataSource实例.
   * 
   * @param xmlConfigLocation
   *          XML格式的根标签是<Resource>的配置文件位置. the resource location to resolve:
   *          either a "classpath:" URL, a "file:" URL, or a plain file path.<br/>
   *          格式参见Tomcat的server.xml里的<Resource>标签,例如:<br/>
   * 
   *          <pre>
   * <font color="#000000"><span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 1 </font></span>&lt;Resource name=&quot;jdbc/TestDB&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 2 </font></span>              auth=&quot;Container&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 3 </font></span>              type=&quot;javax.sql.DataSource&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 4 </font></span>              factory=&quot;org.apache.tomcat.jdbc.pool.DataSourceFactory&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066"> 5 </font></span>              testWhileIdle=&quot;true&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 6 </font></span>              testOnBorrow=&quot;true&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 7 </font></span>              testOnReturn=&quot;false&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 8 </font></span>              validationQuery=&quot;SELECT 1&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000"> 9 </font></span>              validationInterval=&quot;30000&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">10 </font></span>              timeBetweenEvictionRunsMillis=&quot;30000&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">11 </font></span>              maxActive=&quot;100&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">12 </font></span>              minIdle=&quot;10&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">13 </font></span>              maxWait=&quot;10000&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">14 </font></span>              initialSize=&quot;10&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">15 </font></span>              removeAbandonedTimeout=&quot;60&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">16 </font></span>              removeAbandoned=&quot;true&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">17 </font></span>              logAbandoned=&quot;true&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">18 </font></span>              minEvictableIdleTimeMillis=&quot;30000&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">19 </font></span>              jmxEnabled=&quot;true&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">20 </font></span>              jdbcInterceptors=
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">21 </font></span>&quot;org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">22 </font></span>              username=&quot;root&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">23 </font></span>              password=&quot;password&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">24 </font></span>              driverClassName=&quot;com.mysql.jdbc.Driver&quot;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#990066">25 </font></span>              url=&quot;jdbc:mysql://localhost:3306/mysql&quot;/&gt;
   * <span style="background:#dbdbdb; border-right:solid 2px black; margin-right:5px; "><font color="#000000">26 </font></span>
   * </font>
   * </pre>
   * @return 配置好好的org.apache.tomcat.jdbc.pool.DataSource实例.
   */
  public static org.apache.tomcat.jdbc.pool.DataSource createDataSource(String xmlConfigLocation) {
    InputStream input = null;
    try {
      if (xmlConfigLocation == null || xmlConfigLocation.trim().length() == 0) {
        throw new RuntimeException("xmlConfigLocation field can not empty!");

      }
      input = getURL(xmlConfigLocation).openStream();

      Properties properties = loadCofig(input, "UTF-8");

      return createDataSource(properties);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException ioEx) {
        }
      }
    }
  }

  /**
   * 从Properties里创建并配置org.apache.tomcat.jdbc.pool.DataSource实例.
   * 
   * @param properties 包含org.apache.tomcat.jdbc.pool.DataSource属性的键和值. 
   * @return 配置好好的org.apache.tomcat.jdbc.pool.DataSource实例.
   */
  public static org.apache.tomcat.jdbc.pool.DataSource createDataSource(Properties properties) {
    try {
      PoolProperties poolProperties = new PoolProperties();
      Method[] methods = PoolProperties.class.getDeclaredMethods();
      String fieldName;
      String value;
      java.util.Map<String, String> fieldMap = new java.util.HashMap<String, String>();
      for (Method method : methods) {
        if (method.getModifiers() != java.lang.reflect.Modifier.PUBLIC) {
          continue;
        }
        if (!method.getName().startsWith("set")) {
          continue;
        }
        fieldName = method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
        fieldMap.put(fieldName, fieldName);
        if (fieldName.equals("name")) { //@wjw_note: 忽略name属性,防止在JMX里如果有多个重名的连接池而引起的冲突
          continue;
        }

        if (!properties.containsKey(fieldName)) {
          continue;
        }
        value = properties.getProperty(fieldName);
        Class<?> cls = method.getParameterTypes()[0];
        if (cls.getName().equals("java.lang.String")) {
          method.invoke(poolProperties, value);
        } else if (cls.getName().equals("boolean")) {
          method.invoke(poolProperties, Boolean.valueOf(value));
        } else if (cls.getName().equals("int")) {
          method.invoke(poolProperties, Integer.valueOf(value));
        } else if (cls.getName().equals("long")) {
          method.invoke(poolProperties, Long.valueOf(value));
        }
      } //end: for (Method method : methods) {

      //校验配置参数
      for (String prop : properties.stringPropertyNames()) {
        if (!fieldMap.containsKey(prop)) {
          if (!prop.equals("type") && !prop.equals("factory") && !prop.equals("auth")) {
            _log.warn("PoolProperties里没有的属性名:" + prop);
          }
        }
      }

      org.apache.tomcat.jdbc.pool.DataSource tomcatDatasource = new org.apache.tomcat.jdbc.pool.DataSource();
      tomcatDatasource.setPoolProperties(poolProperties);
      tomcatDatasource.createPool(); //@wjw_note: 调用此方法的目的是不要在首次获取Connection时初始化连接池,而是立即建立initialSize大小的Connection连接.

      if (tomcatDatasource.isJmxEnabled()) {
        try {
          tomcatDatasource.preRegister(null, new javax.management.ObjectName(":name=" + tomcatDatasource.getName()));
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      return tomcatDatasource;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
