package fr.univlorraine.publikfeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Point d'entrée de l'application Spring Boot.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	/**
	 * Configure le déploiement dans un container web. (WAR)
	 * @see org.springframework.boot.web.servlet.support.SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)
	 */
	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	/**
	 * Configure le lancement de l'application via un serveur web embarqué. (JAR)
	 * @param args paramètres
	 */
	public static void main(final String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
