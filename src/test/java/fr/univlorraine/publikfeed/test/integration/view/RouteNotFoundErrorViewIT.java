package fr.univlorraine.publikfeed.test.integration.view;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import fr.univlorraine.publikfeed.test.integration.AbstractIT;

public class RouteNotFoundErrorViewIT extends AbstractIT {

	@BeforeEach
	private void beforeEach() {
		webDriverGet("unavailable");
	}

	@Test
	public void routeNotFound() {
		String currentRoute = URI.create(webDriver.getCurrentUrl()).getPath();
		Assertions.assertEquals("/", currentRoute, "Le navigateur devrait être redirigé vers l'accueil.");

		webDriverWait(1).withMessage("Une notification devrait être visible.")
			.until(ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-notification")));
	}

}
