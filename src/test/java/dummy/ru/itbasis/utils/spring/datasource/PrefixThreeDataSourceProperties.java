package dummy.ru.itbasis.utils.spring.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.itbasis.utils.spring.datasource.AbstractDataSourceProperties;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "three")
public class PrefixThreeDataSourceProperties extends AbstractDataSourceProperties {
}
