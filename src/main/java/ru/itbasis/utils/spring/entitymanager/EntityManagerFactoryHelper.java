package ru.itbasis.utils.spring.entitymanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import ru.itbasis.utils.spring.datasource.AbstractDataSourceProperties;
import ru.itbasis.utils.spring.datasource.functions.LocalContainerEntityManagerFactoryBeanFunction;

import javax.sql.DataSource;

@Component
public class EntityManagerFactoryHelper {
	@Autowired
	private LocalContainerEntityManagerFactoryBeanFunction entityManagerFactoryBeanFunction;

	public LocalContainerEntityManagerFactoryBean createEntityManagerFactoryBean(final String unitName,
																																							 final DataSource dataSource,
																																							 final JpaVendorAdapter jpaVendorAdapter,
																																							 final String[] entityPackagesToScan,
																																							 AbstractDataSourceProperties... dataSourceProperties) {
		final LocalContainerEntityManagerFactoryBean bean = entityManagerFactoryBeanFunction.apply(dataSource, dataSourceProperties);
		bean.setJpaVendorAdapter(jpaVendorAdapter);
		bean.setPersistenceUnitName(unitName);
		bean.setPackagesToScan(entityPackagesToScan);

		return bean;
	}
}
