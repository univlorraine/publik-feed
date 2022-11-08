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
package fr.univlorraine.publikfeed.utils.logging;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.helpers.CyclicBuffer;

/**
 * Appender qui regroupe les événements à mailer, dans une limite de temps et de nombre d'événements.
 * @author Adrien Colson
 */
@SuppressWarnings("serial")
public class GroupEventsSMTPAppender extends SMTPAppender implements Serializable {

	/** Message indiquant une demande d'envoi de mail. */
	public static final String SEND_MAIL_MESSAGE = "GroupEventsSMTPAppender_sendMail";

	/** Délai des mail en secondes. */
	private int mailDelaySeconds = 1;

	/** Timer entre chaque envoi de mails. */
	private final Timer timer = new Timer();

	/** Tâche timer courante. */
	private transient TimerTask currentTimerTask;

	/** Dernier événement de log. */
	private ILoggingEvent lastEventObject;

	/**
	 * @return the mailDelaySeconds
	 */
	public int getMailDelaySeconds() {
		return mailDelaySeconds;
	}

	/**
	 * Défini le délai d'envoi de mail.
	 * @param mailDelaySecondsSet délai en secondes
	 */
	public void setMailDelaySeconds(final int mailDelaySecondsSet) {
		synchronized (this) {
			mailDelaySeconds = mailDelaySecondsSet;
		}
	}

	/**
	 * @see ch.qos.logback.core.net.SMTPAppenderBase#append(java.lang.Object)
	 */
	@Override
	protected final void append(final ILoggingEvent eventObject) {
		/* Vérifie si l'événement est une demande d'envoi de mail */
		if (SEND_MAIL_MESSAGE.equals(eventObject.getMessage())) {
			sendMail();
			return;
		}

		/* Vérifie si l'événement est à traiter */
		boolean isEventToProcess;
		try {
			isEventToProcess = eventEvaluator.evaluate(eventObject);
		} catch (final EvaluationException e) {
			isEventToProcess = false;
		}

		if (isEventToProcess) {
			if (cbTracker == null) {
				/* S'il n'y a pas de CyclicBuffer on envoie un mail */
				super.append(eventObject);
			} else {
				/* Sinon on délaie l'envoi de mail */
				processEvent(eventObject);
			}
		}
	}

	/**
	 * Délaie l'envoi d'email.
	 * @param eventObject événement traité
	 */
	private void processEvent(final ILoggingEvent eventObject) {
		synchronized (this) {
			final String key = discriminator.getDiscriminatingValue(eventObject);
			final CyclicBuffer<ILoggingEvent> cb = cbTracker.getOrCreate(key, System.currentTimeMillis());

			/* S'il y avait déjà un dernier événement, on le place dans le CycliBuffer */
			if (lastEventObject != null) {
				subAppend(cb, lastEventObject);
			}
			lastEventObject = eventObject;

			if (cb.length() >= cb.getMaxSize()) {
				/* Si le CyclicBuffer a atteint sa capacité maximale, on annule le timer et on envoie le mail */
				if (currentTimerTask != null) {
					currentTimerTask.cancel();
					currentTimerTask = null;
				}
				sendMail();
			} else if (currentTimerTask == null) {
				/* Sinon si un timer n'est pas programmé, on en programme un */
				currentTimerTask = new TimerTask() {
					/**
					 * @see java.util.TimerTask#run()
					 */
					@Override
					public void run() {
						sendMail();
						currentTimerTask = null;
					}
				};
				timer.schedule(currentTimerTask, TimeUnit.MILLISECONDS.convert(mailDelaySeconds, TimeUnit.SECONDS));
			}
		}
	}

	/**
	 * Envoie le mail.
	 */
	private void sendMail() {
		if (lastEventObject != null) {
			super.append(lastEventObject);
			lastEventObject = null;
		}
	}

	/**
	 * @see ch.qos.logback.core.net.SMTPAppenderBase#stop()
	 */
	@Override
	public void stop() {
		synchronized (this) {
			sendMail();
			super.stop();
		}
	}

}
