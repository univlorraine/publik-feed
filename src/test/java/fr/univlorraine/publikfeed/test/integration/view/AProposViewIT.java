package fr.univlorraine.publikfeed.test.integration.view;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import fr.univlorraine.publikfeed.test.integration.AbstractIT;

public class AProposViewIT extends AbstractIT {

	private static final String ROLE_TEST1 = "ROLE_TEST1";
	private static final String ROLE_TEST2 = "ROLE_TEST2";

	@BeforeEach
	private void beforeEach() {
		webDriverGet("apropos", ROLE_TEST1, ROLE_TEST2);
	}

	@Test
	public void userForm() {
		List<WebElement> textFields = webDriver.findElements(By.tagName("vaadin-text-field"));

		WebElement usernameTF = textFields.stream()
			.filter(textField -> "Nom d'utilisateur".equals(textField.getAttribute("label")))
			.findFirst()
			.orElse(null);
		Assertions.assertNotNull(usernameTF, "Champ 'Nom d'utilisateur' introuvable");
		Assertions.assertEquals(TEST_USER, usernameTF.getAttribute("value"), "Nom d'utilisateur incorrect");

		WebElement authoritiesTF = textFields.stream()
			.filter(textField -> "Rôles".equals(textField.getAttribute("label")))
			.findFirst()
			.orElse(null);
		Assertions.assertNotNull(authoritiesTF, "Champ 'Rôles' introuvable");
		Assertions.assertEquals(ROLE_TEST1 + ", " + ROLE_TEST2, authoritiesTF.getAttribute("value"), "Rôles incorrects");
	}

}
