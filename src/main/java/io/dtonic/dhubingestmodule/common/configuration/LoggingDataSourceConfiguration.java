package io.dtonic.dhubingestmodule.common.configuration;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(value="io.dtonic.dhubingestmodule.aop.logging.mapper", sqlSessionFactoryRef="loggingSqlSessionFactory")
public class LoggingDataSourceConfiguration {

    @Bean(name = "loggingDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.logging")
    public DataSource loggingDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "loggingSqlSessionFactory")
    public SqlSessionFactory loggingSqlSessionFactory(@Qualifier("loggingDataSource") DataSource loggingDataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(loggingDataSource);
        sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:io/dtonic/dhubingestmodule/aop/logging/mapper/*.xml"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "loggingSqlSessionTemplate")
    public SqlSessionTemplate loggingSqlSessionTemplate(SqlSessionFactory loggingSqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(loggingSqlSessionFactory);

    }
}
