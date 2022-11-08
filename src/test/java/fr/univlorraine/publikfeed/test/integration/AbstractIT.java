/**
 *
 * Copyright (c) 2022 Université de Lorraine, 18/02/2021
 *
 * dn-sied-dev@univ-lorraine.fr
 *
 * Ce logiciel est un programme informatique servant à alimenter Publik depuis des groupes LDAP.
 *
 * Ce logiciel est régi par la licence CeCILL 2.1 soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL 2.1, et que vous en avez accepté les
 * termes.
 *
 */
package fr.univlorraine.publikfeed.test.integration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.util.UriComponentsBuilder;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "spring.config.location = src/test/resources/application.properties")
@Import({ RequestParameterUserFilter.class, FlywayCleanMigrateStrategy.class })
@Slf4j
public abstract class AbstractIT {

	protected static final String TEST_USER = "user";

	@LocalServerPort
	private transient int port;

	protected static WebDriver webDriver;

	protected static InetAddress ip = getIP();

	protected static InetAddress getIP() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("univ-lorraine.fr", 80));
			return socket.getLocalAddress();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@BeforeAll
	public static void setup() {
		ChromeOptions chromeOptions = new ChromeOptions().setHeadless(true);

		String seleniumRemoteUrl = System.getenv("SELENIUM_REMOTE_URL");
		if (seleniumRemoteUrl == null) {
			log.info("Propriété SELENIUM_REMOTE_URL non renseignée, utilise le ChromeDriver local.");
			WebDriverManager.chromedriver().setup();
			webDriver = new ChromeDriver(chromeOptions);
		} else {
			log.info("Connexion au Selenium distant {}...", seleniumRemoteUrl);
			try {
				webDriver = new RemoteWebDriver(new URL(seleniumRemoteUrl), chromeOptions);
			} catch (MalformedURLException e) {
				e.setStackTrace(new StackTraceElement[] {});
				log.error("Propriété SELENIUM_REMOTE_URL incorrecte : {}", seleniumRemoteUrl, e);
			}
		}
	}

	protected String getUrl(final String route, final String user, final String... authorities) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(route)
			.scheme("http")
			.host(ip.getHostAddress())
			.port(port)
			.queryParam(RequestParameterUserFilter.USERNAME_PARAMETER, user);
		for (String authority : authorities) {
			builder.queryParam(RequestParameterUserFilter.AUTHORITY_PARAMETER, authority);
		}
		return builder.build().toUriString();
	}

	protected void webDriverGet(final String route, final String... authorities) {
		webDriver.get(getUrl(route, TEST_USER, authorities));
	}

	protected WebDriverWait webDriverWait(final long timeOutInSeconds) {
		return new WebDriverWait(webDriver, timeOutInSeconds);
	}

	@AfterAll
	public static void tear() {
		if (webDriver != null) {
			webDriver.quit();
		}
	}

}
