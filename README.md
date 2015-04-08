###### Tomcat 在 7.0 以前的版本都是使用commons-dbcp做为连接池的实现，但是 dbcp存在一些问题： 
>   
（1）dbcp 是单线程的，为了保证线程安全会锁整个连接池   
（2）dbcp 性能不佳   
（3）dbcp 太复杂，超过 60 个类，发展滞后。   
    
因此，通常J2EE中还会使用其它的高性能连接池，如 C3P0，还有阿里系的 druid 等。为此，Tomcat 从 7.0 开始引入一个新的模块： `Tomcat jdbc pool`
>  
tomcat jdbc pool 近乎兼容 dbcp ，性能更高 
异步方式获取连接 	
tomcat jdbc pool 是 tomcat 的一个模块，基于 tomcat JULI，使用 Tomcat 的日志框架 	
使用 javax.sql.PooledConnection 接口获取连接 	
支持高并发应用环境 	
超简单，核心文件只有8个，比 c3p0 还少 	
更好的空闲连接处理机制 	
支持 JMX 	
支持 XA Connection。 	
tomcat jdbc pool 可在 Tomcat 中直接使用，也可以在独立的应用中使用。 	
        
###### `wjw-tomcat-jdbc.jar`是简单的扩展!
包含了全部的tomcat jdbc pool的文件,并在org\apache\tomcat\jdbc\ext\目录下增加了一个TomcatDataSourceFactory.class类,  
TomcatDataSourceFactory类的目的就是能从tomcat独立出来并兼容tomcat的server.xml的<Resource>标签里创建org.apache.tomcat.jdbc.pool.DataSource;  
并把<Resource>标签里的属性值赋给org.apache.tomcat.jdbc.pool.DataSource,  
同时校验<Resource>里的属性名是否存在于tomcat jdbc pool的属性集里,如果不存在会给出警告,防止配置属性名时出现的输入此错误!  

###### 例如:属性文件名:resource-jdbc.xml,内容是:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
  说明: tomcat jdbc pool的参数,既可以作为Resource的属性,也可以作为Resource的子元素;
  例如: <Resource driverClassName="com.mysql.jdbc.Driver"/>  
     或者 <Resource><driverClassName>com.mysql.jdbc.Driver</driverClassName></Resource>
    如果同时出现,那么元素值会覆盖属性值.
-->
<Resource name="jdbc/TestDB"
              auth="Container"
              type="javax.sql.DataSource"
              factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              testWhileIdle="true"
              testOnBorrow="true"
              testOnReturn="false"
              validationQuery="SELECT 1"
              validationInterval="30000"
              timeBetweenEvictionRunsMillis="30000"
              maxActive="100"
              minIdle="10"
              maxWait="10000"
              initialSize="10"
              removeAbandonedTimeout="60"
              removeAbandoned="true"
              logAbandoned="true"
              minEvictableIdleTimeMillis="30000"
              jmxEnabled="true"
              username="root"
              password="masterkey"
              driverClassName="com.mysql.jdbc.Driver"
              url="jdbc:mysql://127.0.0.1:3306/mysql"
              >
  <jdbcInterceptors>StatementFinalizer;
  SlowQueryReportJmx(notifyPool=false,maxQueries=1000,threshold=1)</jdbcInterceptors>
</Resource>
```

###### java代码是: 

`javax.sql.DataSource datasource = TomcatDataSourceFactory.createDataSource("classpath:some/resource/path/resource-jdbc.xml");`	  
或者  
`javax.sql.DataSource datasource = TomcatDataSourceFactory.createDataSource("file:/some/resource/path/resource-jdbc.xml");`  
或者  
`javax.sql.DataSource datasource = TomcatDataSourceFactory.createDataSource("http://some/resource/path/resource-jdbc.xml");`  

###### 在Spring环境里的配置方式如如下:
```xml
   <bean id="dataSource" class="org.apache.tomcat.jdbc.ext.TomcatDataSourceFactory" 
         factory-method="createDataSource"
         destroy-method="close">
     <constructor-arg value="classpath:resource-jdbc.xml" />
   </bean>
```
或者
```xml
   <bean id="dataSource" class="org.apache.tomcat.jdbc.ext.TomcatDataSourceFactory" 
         factory-method="createDataSource"
         destroy-method="close">
      <constructor-arg>
        <props>
          <prop key="auth">Container</prop>
          <prop key="type">javax.sql.DataSource</prop>
          <prop key="factory">org.apache.tomcat.jdbc.pool.DataSourceFactory</prop>
          <prop key="testWhileIdle">true</prop>
          <prop key="testOnBorrow">true</prop>
          <prop key="testOnReturn">false</prop>
          <prop key="validationQuery">SELECT 1 FROM DUAL</prop>
          <prop key="validationInterval">30000</prop>
          <prop key="timeBetweenEvictionRunsMillis">30000</prop>
          <prop key="maxActive">10</prop>
          <prop key="minIdle">10</prop>
          <prop key="maxWait">10000</prop>
          <prop key="initialSize">5</prop>
          <prop key="removeAbandonedTimeout">60</prop>
          <prop key="removeAbandoned">true</prop>
          <prop key="logAbandoned">true</prop>
          <prop key="minEvictableIdleTimeMillis">30000</prop>
          <prop key="jmxEnabled">true</prop>
          <prop key="username">root</prop>
          <prop key="password">root123</prop>
          <prop key="driverClassName">com.mysql.jdbc.Driver</prop>
          <prop key="url">jdbc:mysql://192.168.1.2:3306/test?rewriteBatchedStatements=true&amp;characterEncoding=utf-8</prop>
          <prop key="jdbcInterceptors">StatementFinalizer;SlowQueryReportJmx(notifyPool=false,maxQueries=1000,threshold=300)</prop>
        </props> 
      </constructor-arg>
   </bean>
```
