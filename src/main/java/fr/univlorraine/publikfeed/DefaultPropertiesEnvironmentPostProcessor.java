package fr.univlorraine.publikfeed;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Charge les properties par défaut.
 * @author Adrien Colson
 */
public class DefaultPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private final PropertiesPropertySourceLoader propertiesLoader = new PropertiesPropertySourceLoader();

	private final YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();

	/**
	 * @see org.springframework.boot.env.EnvironmentPostProcessor#postProcessEnvironment(org.springframework.core.env.ConfigurableEnvironment,
	 *      org.springframework.boot.SpringApplication)
	 */
	@Override
	public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
		/* Charge la configuration par défaut */
		environment.getPropertySources().addLast(load("defaults", new ClassPathResource("defaults.yml"), yamlLoader));

		/* Charge les informations de compilation */
		environment.getPropertySources().addLast(load("build-info", new ClassPathResource("META-INF/build-info.properties"), propertiesLoader));
	}

	/**
	 * @param  name   property source name
	 * @param  path   resource path
	 * @param  loader property source loader
	 * @return        property source
	 */
	private PropertySource<?> load(final String name, final Resource path, final PropertySourceLoader loader) {
		if (!path.exists()) {
			throw new IllegalArgumentException(path + " does not exist");
		}
		try {
			return loader.load(name, path).get(0);
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to load configuration from " + path, ex);
		}
	}

}
