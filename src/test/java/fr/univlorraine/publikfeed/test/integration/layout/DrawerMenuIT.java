package fr.univlorraine.publikfeed.test.integration.layout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import fr.univlorraine.publikfeed.service.AppUserDetailsService;
import fr.univlorraine.publikfeed.test.integration.AbstractIT;

public class DrawerMenuIT extends AbstractIT {

	@Test
	public void drawerMenuUser() {
		webDriverGet("");

		Assertions.assertNotNull(webDriver.findElement(By.xpath("//*[text()='Accueil']/ancestor::a")),
			"Le bouton 'Accueil' est introuvable.");
		Assertions.assertNotNull(webDriver.findElement(By.xpath("//*[text()='Documentation']/ancestor::a")),
			"Le bouton 'Documentation' est introuvable.");

		Assertions.assertThrows(NoSuchElementException.class,
			() -> webDriver.findElement(By.xpath("//*[text()='Connexions']/ancestor::a")),
			"Le bouton 'Connexions' ne doit pas être accessible aux simples utilisateurs.");
	}

	@Test
	public void drawerMenuAdmin() {
		webDriverGet("", AppUserDetailsService.ROLE_SUPERADMIN, AppUserDetailsService.ROLE_USER);

		Assertions.assertNotNull(webDriver.findElement(By.xpath("//*[text()='Connexions']/ancestor::a")),
			"Le bouton 'Connexions' est introuvable.");
	}

}
