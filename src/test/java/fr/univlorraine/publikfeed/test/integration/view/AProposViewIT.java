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
