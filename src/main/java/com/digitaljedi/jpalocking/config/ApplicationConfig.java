package com.digitaljedi.jpalocking.config;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.digitaljedi.jpalocking.listener.LoggingRetryListener;

@Configuration
@ComponentScan(basePackages = { "com.digitaljedi.jpalocking.service", "com.digitaljedi.jpalocking.repo",
		"com.digitaljedi.jpalocking.component" })
@EnableJpaRepositories(basePackages = { "com.digitaljedi.jpalocking.repo" })
@EnableTransactionManagement
@EnableConfigurationProperties
@EnableJpaAuditing
@EnableRetry
public class ApplicationConfig {

	@Bean
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.HSQL).build();
		return db;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setPackagesToScan("com.digitaljedi.jpalocking.domain.**");
		factory.setDataSource(dataSource);

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		vendorAdapter.setShowSql(true);
		factory.setJpaVendorAdapter(vendorAdapter);

		Properties jpaProperties = new Properties();
		jpaProperties.setProperty("hibernate.show_sql", "false");
		jpaProperties.setProperty("hibernate.format_sql", "false");
		jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

		factory.setJpaProperties(jpaProperties);

		return factory;
	}

	@Bean
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory);
		return txManager;
	}

	@Bean
	@Scope("prototype")
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();

		pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				System.out.println("REJECTED!");
			}
		});

		pool.setCorePoolSize(4);
		pool.setMaxPoolSize(16);
		pool.setWaitForTasksToCompleteOnShutdown(true);
		pool.initialize();
		return pool;
	}

	@Bean
	public Random random() {
		return new Random(27);
	}

//	@Bean
//	public RetryTemplate retryTemplate() {
//		RetryTemplate retryTemplate = new RetryTemplate();
//		retryTemplate.registerListener(new LoggingRetryListener());
//		return retryTemplate;
//	}
	
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
         
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000l);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
 
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(10);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        retryTemplate.registerListener(new LoggingRetryListener());
        return retryTemplate;
    }
}
