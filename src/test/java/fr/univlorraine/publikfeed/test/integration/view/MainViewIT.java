package fr.univlorraine.publikfeed.test.integration.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import fr.univlorraine.publikfeed.test.integration.AbstractIT;

public class MainViewIT extends AbstractIT {

	@BeforeEach
	private void beforeEach() {
		webDriverGet("");
	}

	@Test
	public void btnNotif() {
		WebElement button = webDriver.findElement(By.xpath("//vaadin-button[text()='Tester les notifications']"));
		Assertions.assertNotNull(button, "Le bouton 'Tester les notifications' est introuvable.");

		button.click();
		button.click();

		webDriverWait(1).withMessage("Après deux clics, deux notifications devraient être visibles.")
			.until(ExpectedConditions.numberOfElementsToBe(By.tagName("vaadin-notification"), 2));
	}

}
