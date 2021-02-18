package fr.univlorraine.publikfeed.ui.layout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class PageTitleFormatter {

	@Autowired
	private transient BuildProperties buildProperties;

	public String format(final String title) {
		return title + " - " + buildProperties.getName();
	}

}
