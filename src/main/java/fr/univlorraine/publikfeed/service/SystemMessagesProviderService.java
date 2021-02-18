package fr.univlorraine.publikfeed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.CustomizedSystemMessages;
import com.vaadin.flow.server.SystemMessages;
import com.vaadin.flow.server.SystemMessagesInfo;
import com.vaadin.flow.server.SystemMessagesProvider;

/**
 * Service des messages systèmes Vaadin.
 * @author Adrien Colson
 */
@Service
@SuppressWarnings("serial")
public class SystemMessagesProviderService implements SystemMessagesProvider {

	@Autowired
	private transient MessageSource messageSource;

	@Override
	public SystemMessages getSystemMessages(final SystemMessagesInfo systemMessagesInfo) {
		CustomizedSystemMessages systemMessages = new CustomizedSystemMessages();
		systemMessages.setSessionExpiredCaption(messageSource.getMessage("vaadin.sessionExpired.caption", null, systemMessagesInfo.getLocale()));
		systemMessages.setSessionExpiredMessage(messageSource.getMessage("vaadin.sessionExpired.message", null, systemMessagesInfo.getLocale()));
		systemMessages.setInternalErrorCaption(messageSource.getMessage("vaadin.internalError.caption", null, systemMessagesInfo.getLocale()));
		systemMessages.setInternalErrorMessage(messageSource.getMessage("vaadin.internalError.message", null, systemMessagesInfo.getLocale()));
		systemMessages.setCookiesDisabledCaption(messageSource.getMessage("vaadin.cookiesDisabled.caption", null, systemMessagesInfo.getLocale()));
		systemMessages.setCookiesDisabledMessage(messageSource.getMessage("vaadin.cookiesDisabled.message", null, systemMessagesInfo.getLocale()));
		return systemMessages;
	}

}
