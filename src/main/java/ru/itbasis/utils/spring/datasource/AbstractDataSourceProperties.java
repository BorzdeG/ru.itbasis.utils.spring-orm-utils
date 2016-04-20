package ru.itbasis.utils.spring.datasource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Slf4j
@Data
@ConfigurationProperties
abstract public class AbstractDataSourceProperties {
	@NestedConfigurationProperty
	private JdbcProperties   jdbc   = new JdbcProperties();
	@NestedConfigurationProperty
	private SshProperties    ssh    = new SshProperties();
	@NestedConfigurationProperty
	private TunnelProperties tunnel = new TunnelProperties();
	@NestedConfigurationProperty
	private JpaProperties    jpa    = new JpaProperties();

	@Data
	@SuppressWarnings("WeakerAccess")
	public static class JdbcProperties {
		private String driverName;
		private String username;
		private String password;
		private String url;
	}

	@Data
	@SuppressWarnings("WeakerAccess")
	public static class SshProperties {
		private boolean            enabled;
		private String             key;
		private String             passphrase;
		@NestedConfigurationProperty
		private SshProxyProperties proxy;
		private String             strictHostKeyChecking;
	}

	@Data
	@SuppressWarnings("WeakerAccess")
	public static class SshProxyProperties {
		private String host;
		private String username;
		private String password;
		private int    port;

		@Override
		public String toString() {
			return "sshProxy[" + username + "@" + host + ":" + port + "]";
		}
	}

	@Data
	@SuppressWarnings("WeakerAccess")
	public static class TunnelProperties {
		private String host;
		private int    remotePort;
		private int    localPort;

		public String getAsString() {
			return localPort + ":" + host + ":" + remotePort;
		}

		@Override
		public String toString() {
			return "tunnel[" + getAsString() + "]";
		}
	}
}
