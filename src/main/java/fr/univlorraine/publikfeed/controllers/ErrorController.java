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
package fr.univlorraine.publikfeed.controllers;

import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.server.VaadinService;

import fr.univlorraine.publikfeed.ldap.entity.PeopleLdap;
import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;
import fr.univlorraine.publikfeed.model.app.services.UserErrHisService;
import fr.univlorraine.publikfeed.service.I18NProviderService;
import fr.univlorraine.publikfeed.utils.Utils;
import lombok.extern.slf4j.Slf4j;

@Component(value="errorController")
@Slf4j
public class ErrorController {

	@Value("${anomalie.contact.erreurmail}")
	private transient String mailContactAnomalieMail;

	@Value("${anomalie.mail.from}")
	private transient String from;

	@Resource
	private UserErrHisService userErrHisService;

	@Resource
	private MailController mailController;

	@Autowired
	private transient MessageSource messageSource;



	public void check(Exception e, PeopleLdap p) {
		//sauvegarde de l'erreur dans la base
		UserErrHis erreur = new UserErrHis();
		erreur.setLogin(p.getUid());
		erreur.setTrace(e.getMessage());
		erreur = userErrHisService.save(erreur);

		// Si erreur de mail invalide
		if(e.getMessage()!=null && e.getMessage().contains(Utils.ANOMALIE_PUBLIK_MAIL)) {
			log.info("Envoi mail anomalie -{}-",Utils.ANOMALIE_PUBLIK_MAIL );
			String message =messageSource.getMessage("anomalie.mailinvalide", new String[] {p.getDisplayName(), p.getUid(), p.getMail()}, Locale.FRANCE);
			// Envoi mail annuaire-contact
			mailController.sendMail(mailContactAnomalieMail, "Anomalie mail invalide", message, from);
		}
	} 

}
