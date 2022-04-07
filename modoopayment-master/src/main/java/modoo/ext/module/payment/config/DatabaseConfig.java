package modoo.ext.module.payment.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@PropertySource("file:./config/application.properties")
public class DatabaseConfig {
	
	private static final String PROPERTIES = "spring.datasource.hikari";
	public static final String MASTER_DATASOURCE = "dataSource";
	public static final String READER_DATASOURCE = "readerDataSource";
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private Environment environment;
	
	/**
	 *  2020-10-26 09:17:34,727 997  [main] DEBUG com.zaxxer.hikari.HikariConfig - HikariPool-1 - configuration:
		2020-10-26 09:17:34,728 998  [main] DEBUG com.zaxxer.hikari.HikariConfig - allowPoolSuspension.............false
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - autoCommit......................true
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - catalog.........................none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionInitSql...............none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTestQuery.............none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - connectionTimeout...............10000
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSource......................none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceClassName.............none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceJNDI..................none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - dataSourceProperties............{password=<masked>}
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - driverClassName................."com.mysql.cj.jdbc.Driver"
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - exceptionOverrideClassName......none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckProperties...........{}
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - healthCheckRegistry.............none
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - idleTimeout.....................10000
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - initializationFailTimeout.......1
		2020-10-26 09:17:34,729 999  [main] DEBUG com.zaxxer.hikari.HikariConfig - isolateInternalQueries..........false
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - jdbcUrl.........................jdbc:mysql://127.0.0.1:3306/modoo_dev_db?characterEncoding=UTF-8&serverTimezone=UTC
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - leakDetectionThreshold..........300000
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - maxLifetime.....................420000
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - maximumPoolSize.................10
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricRegistry..................none
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - metricsTrackerFactory...........none
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - minimumIdle.....................2
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - password........................<masked>
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - poolName........................"HikariPool-1"
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - readOnly........................false
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - registerMbeans..................false
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - scheduledExecutor...............none
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - schema..........................none
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - threadFactory...................internal
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - transactionIsolation............default
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - username........................"modoo_user"
		2020-10-26 09:17:34,730 1000 [main] DEBUG com.zaxxer.hikari.HikariConfig - validationTimeout...............10000
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = PROPERTIES)
	public HikariConfig hikariConfig() {
		return new HikariConfig();
	}
	
	@Bean(destroyMethod = "close")
	public DataSource dataSource() {	
		DataSource dataSource = new HikariDataSource(hikariConfig());
		return dataSource;
	}
	
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources(environment.getProperty("modoo.database.mapper.path")));
		return sqlSessionFactoryBean.getObject();
	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
}
