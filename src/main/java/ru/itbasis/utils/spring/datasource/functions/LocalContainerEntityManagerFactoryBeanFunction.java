package ru.itbasis.utils.spring.datasource.functions;

import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.stereotype.Component;
import ru.itbasis.utils.spring.datasource.AbstractDataSourceProperties;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.function.BiFunction;

@Component
public class LocalContainerEntityManagerFactoryBeanFunction
	implements BiFunction<DataSource, AbstractDataSourceProperties[], LocalContainerEntityManagerFactoryBean> {
	@Override
	public LocalContainerEntityManagerFactoryBean apply(final DataSource dataSource, final AbstractDataSourceProperties... dataSourceProperties) {
		final LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();

		bean.setJpaDialect(getJpaDialect());
		bean.setDataSource(dataSource);

		final Properties properties = new Properties();
		for (AbstractDataSourceProperties dataSourceProperty : dataSourceProperties) {
			properties.putAll(dataSourceProperty.getJpa().getProperties());
		}
		bean.setJpaProperties(properties);

		return bean;
	}

	@SuppressWarnings("WeakerAccess")
	protected JpaDialect getJpaDialect() {
		return new HibernateJpaDialect();
	}
}
