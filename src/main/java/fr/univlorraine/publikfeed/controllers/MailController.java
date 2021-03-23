package fr.univlorraine.publikfeed.controllers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Component(value="mailController")
@Slf4j
public class MailController {

	@Autowired
	public JavaMailSender mailSender;


	public void sendMail(String to, String sujet, String corps, String from) {

		//EN DEV mettre un to a null ou au developpeur
		if(StringUtils.hasText(to)){

			log.info("preparation du mail -{}- à {} de {}", sujet, to, from);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

			try {
				message.setFrom(from);

				message.setTo(to);
				/*if(StringUtils.hasText(cc)){
				message.setCc(cc);
				}*/
				message.setSubject(sujet);
				mimeMessage.setText(corps,"utf-8", "html");
				mimeMessage.setHeader("Content-Type", "text/html; charset=utf-8");

				log.info("Envoi du mail -{}- à {}...", sujet, to);

				mailSender.send(mimeMessage);

				log.info("Envoi du mail   OK");
			} catch (MessagingException e) {
				log.error("Envoi du mail Impossible",e);
			}

		}else{
			log.info("Annulation de l'envoi du mail car aucun destinataires");
		}
	}



}
