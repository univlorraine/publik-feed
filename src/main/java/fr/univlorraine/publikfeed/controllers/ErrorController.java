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
