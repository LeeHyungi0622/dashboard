package io.dtonic.dhubingestmodule.common.configuration;

import io.dtonic.dhubingestmodule.common.code.Constants;
import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode;
import javax.sql.DataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Application DB connection setting class.
 *
 * @FileName DataSourceConfiguration.java
 * @Project citydatahub_datacore_ui
 * @Brief
 * @Version 1.0
 * @Date 2022. 3. 24.
 * @Author Elvin
 */
@Configuration
@EnableTransactionManagement
@MapperScan(Constants.BASE_PACKAGE)
public class DataSourceConfiguration {

    // @Value("${datasource.driverClassName}")
    // private String driverClassName;

    // @Value("${datasource.url}")
    // private String jdbcUrl;

    // @Value("${datasource.username}")
    // private String jdbcUserName;

    // @Value("${datasource.password}")
    // private String jdbcPassword;

    @Value("${spring.datasource.retrieve.use.yn}")
    private String secondaryDatasourceUseYn;

    // @Value("${datasource.secondary.driverClassName}")
    // private String secondaryDriverClassName;

    // @Value("${datasource.secondary.url}")
    // private String secondaryJdbcUrl;

    // @Value("${datasource.secondary.username}")
    // private String secondaryJdbcUserName;

    // @Value("${datasource.secondary.password}")
    // private String secondaryJdbcPassword;

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        return dataSourceBuilder.build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.retrieve")
    @Qualifier("retrieveDatasource")
    public DataSource retrieveDatasource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        return dataSourceBuilder.build();
    }

    @Bean
    @Autowired
    public DataSourceTransactionManager dataSourceTransactionManager(
        @Qualifier("dataSource") DataSource datasource
    ) {
        return new DataSourceTransactionManager(datasource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // bean.setMapperLocations(
        // new PathMatchingResourcePatternResolver()
        // .getResources("classpath:mapper/*Mapper.xml")
        // );
        // bean.setConfigLocation(
        // new DefaultResourceLoader().getResource("classpath:mybatis.xml")
        // );
        return bean.getObject();
    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    @Qualifier("batchSqlSession")
    public SqlSessionTemplate batchSqlSession(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

    @Bean
    @Qualifier("retrieveSqlSession")
    public SqlSessionTemplate secondarySqlSession(SqlSessionFactory sqlSessionFactory)
        throws Exception {
        // Using secondary datasource
        if (DataCoreUiCode.UseYn.YES.toString().equals(secondaryDatasourceUseYn)) {
            SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
            bean.setDataSource(retrieveDatasource());
            return new SqlSessionTemplate(bean.getObject());
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }
}
