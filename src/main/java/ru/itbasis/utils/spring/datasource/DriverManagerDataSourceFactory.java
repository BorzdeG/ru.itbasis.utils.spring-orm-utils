package ru.itbasis.utils.spring.datasource;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;
import static org.springframework.util.StringUtils.isEmpty;

// FIXME Вынести создание туннеля в отдельный бин с уникальностью по {@link ru.itbasis.utils.spring.datasource.AbstractDataSourceProperties.SshProxyProperties#toString()}
@Slf4j
public class DriverManagerDataSourceFactory extends AbstractFactoryBean<DriverManagerDataSource> {
	private static final BiPredicate<Function<AbstractDataSourceProperties, String>, AbstractDataSourceProperties>  STRING_IS_NOT_EMPTY = (fn, e) -> !isEmpty(
		fn.apply(e));
	private static final BiPredicate<Function<AbstractDataSourceProperties, Integer>, AbstractDataSourceProperties> INTEGER_IS_NOT_ZERO = (fn, e) ->
		fn.apply(e) > 0;

	@Getter
	private AbstractDataSourceProperties[] dataSourceProperties;

	private Session session;

	private void applyPortForwardingL() throws JSchException {
		final String[] portForwardingL = session.getPortForwardingL();
		/* lport:host:hostPort */
		final int    tunnelLocalPort  = getProperty(e -> e.getTunnel().getLocalPort(), INTEGER_IS_NOT_ZERO, INTEGER_ZERO);
		final String tunnelHost       = getProperty(e -> e.getTunnel().getHost(), STRING_IS_NOT_EMPTY, EMPTY);
		final int    tunnelRemotePort = getProperty(e -> e.getTunnel().getRemotePort(), INTEGER_IS_NOT_ZERO, INTEGER_ZERO);
		final String tunnel           = tunnelLocalPort + ":" + tunnelHost + ":" + tunnelRemotePort;
		if (ArrayUtils.contains(portForwardingL, tunnel)) {
			log.warn("tunnel '{}' is exists.", tunnel);
			return;
		}

		log.debug("create tunnel: {}", tunnel);
		session.setPortForwardingL(tunnelLocalPort, tunnelHost, tunnelRemotePort);
	}

	private void applySshKey(final JSch jSch) throws JSchException {
		String sshKey = getProperty(e -> e.getSsh().getKey(), STRING_IS_NOT_EMPTY, null);
		if (StringUtils.trimToNull(sshKey) != null) {
			sshKey = sshKey.replaceAll("\\$\\{user.home}", SystemUtils.USER_HOME);
			Assert.isTrue(new File(sshKey).exists());
			jSch.addIdentity(sshKey, getProperty(e -> e.getSsh().getPassphrase(), STRING_IS_NOT_EMPTY, null));
		}
	}

	private void applyStrictHostKeyChecking() {
		final String strictHostKeyChecking = getProperty(e -> e.getSsh().getStrictHostKeyChecking(), STRING_IS_NOT_EMPTY, null);
		if (StringUtils.trimToNull(strictHostKeyChecking) != null) {
			session.setConfig("StrictHostKeyChecking", strictHostKeyChecking);
		}
	}

	@Override
	protected DriverManagerDataSource createInstance() throws Exception {
		DriverManagerDataSource instance = BeanUtils.instantiateClass(getObjectType());
		Assert.notNull(instance);

		openSshTunnel();

		instance.setDriverClassName(getProperty(e -> e.getJdbc().getDriverName(), STRING_IS_NOT_EMPTY, EMPTY));
		instance.setUsername(getProperty(e -> e.getJdbc().getUsername(), STRING_IS_NOT_EMPTY, EMPTY));
		instance.setPassword(getProperty(e -> e.getJdbc().getPassword(), STRING_IS_NOT_EMPTY, EMPTY));
		instance.setUrl(getProperty(e -> e.getJdbc().getUrl(), STRING_IS_NOT_EMPTY, EMPTY));

		return instance;
	}

	@Override
	public void destroy() throws Exception {
		if (session != null) { session.disconnect(); }
		super.destroy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<DriverManagerDataSource> getObjectType() {
		return DriverManagerDataSource.class;
	}

	private <T> T getProperty(final Function<AbstractDataSourceProperties, T> function,
														final BiPredicate<Function<AbstractDataSourceProperties, T>, AbstractDataSourceProperties> predicate,
														final T defaultValue) {
		final Optional<AbstractDataSourceProperties> dsp = Arrays.stream(getDataSourceProperties())
																														 .filter(e -> predicate.test(function, e))
																														 .findFirst();
		return dsp.isPresent() ? function.apply(dsp.get()) : defaultValue;
	}

	private void openSshTunnel() throws JSchException {
		if (!getProperty(e -> e.getSsh().isEnabled(), Function::apply, FALSE)) { return; }

		final JSch jSch = new JSch();

		applySshKey(jSch);

		final AbstractDataSourceProperties.SshProxyProperties sshProxy = getProperty(e -> e.getSsh().getProxy(), (fn, e) -> fn.apply(e) != null, null);
		Assert.notNull(sshProxy);
		if (session == null) {
			session = jSch.getSession(sshProxy.getUsername(), sshProxy.getHost());
		}

		if (!session.isConnected()) {
			applyStrictHostKeyChecking();
			session.setPassword(sshProxy.getPassword());
			session.connect();
		}
		Assert.isTrue(session.isConnected());

		log.info("open ssh tunnel from proxy: {}", sshProxy);

		applyPortForwardingL();
	}

	@Required
	public void setDataSourceProperties(AbstractDataSourceProperties... dataSourceProperties) {
		this.dataSourceProperties = dataSourceProperties;
	}
}
