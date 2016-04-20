package ru.itbasis.utils.spring.datasource;

import dummy.ru.itbasis.utils.spring.datasource.PrefixCoreDataSourceProperties;
import dummy.ru.itbasis.utils.spring.datasource.PrefixOneDataSourceProperties;
import dummy.ru.itbasis.utils.spring.datasource.PrefixTwoDataSourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.orm.jpa.vendor.Database;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DataSourcePropertiesTest {
	@Test
	public void testCore() throws Exception {
		final SpringApplication springApplication = new SpringApplication(PrefixCoreDataSourceProperties.class);
		springApplication.setAdditionalProfiles("two");
		final ConfigurableApplicationContext context              = springApplication.run();
		final AbstractDataSourceProperties   dataSourceProperties = context.getBean(PrefixCoreDataSourceProperties.class);

		Assert.assertTrue(dataSourceProperties.getSsh().isEnabled());
	}

	@Test
	public void testOne() throws Exception {
		final SpringApplication springApplication = new SpringApplication(PrefixOneDataSourceProperties.class);
		springApplication.setAdditionalProfiles("one");
		final ConfigurableApplicationContext context              = springApplication.run();
		final AbstractDataSourceProperties   dataSourceProperties = context.getBean(PrefixOneDataSourceProperties.class);

		final JpaProperties jpa = dataSourceProperties.getJpa();
		Assert.assertEquals(jpa.getDatabase(), Database.H2);

		final AbstractDataSourceProperties.SshProxyProperties proxy = dataSourceProperties.getSsh().getProxy();
		Assert.assertEquals(proxy.getHost(), "test");
		Assert.assertEquals(proxy.getPort(), 20);
	}

	@Test
	public void testTwo() throws Exception {
		final SpringApplication springApplication = new SpringApplication(PrefixTwoDataSourceProperties.class);
		springApplication.setAdditionalProfiles("two");
		final ConfigurableApplicationContext context              = springApplication.run();
		final AbstractDataSourceProperties   dataSourceProperties = context.getBean(PrefixTwoDataSourceProperties.class);

		final JpaProperties jpa = dataSourceProperties.getJpa();
		Assert.assertEquals(jpa.getDatabase(), Database.DB2);

		final AbstractDataSourceProperties.SshProxyProperties proxy = dataSourceProperties.getSsh().getProxy();
		Assert.assertEquals(proxy.getHost(), "test2");
		Assert.assertEquals(proxy.getPort(), 22);
	}

}