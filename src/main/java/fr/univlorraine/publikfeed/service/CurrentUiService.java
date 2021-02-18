package fr.univlorraine.publikfeed.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.publisher.UnicastProcessor;

@UIScope
@Service
public class CurrentUiService {

	@Value("${app.color}")
	private transient String appColorDefault;

	@PostConstruct
	private void init() {
		setAppColor(appColorDefault);
		setDarkModeFromMedia();
	}

	/* Theme : Couleur principale */

	private final UnicastProcessor<String> appColorProcessor = UnicastProcessor.create();
	private final FluxSink<String> appColorSink = appColorProcessor.sink(OverflowStrategy.LATEST);
	@Getter
	private final Flux<String> appColorFlux = appColorProcessor.replay(1).autoConnect();

	public void setAppColor(final String value) {
		appColorSink.next(value);
	}

	/* Theme : Mode sombre */

	private final UnicastProcessor<Boolean> darkModeProcessor = UnicastProcessor.create();
	private final FluxSink<Boolean> darkModeSink = darkModeProcessor.sink(OverflowStrategy.LATEST);
	@Getter
	private final Flux<Boolean> darkModeFlux = darkModeProcessor.replay(1).autoConnect();

	public void setDarkMode(final Boolean value) {
		darkModeSink.next(value);
	}

	/**
	 * Détecte la préférence du mode sombre.
	 */
	public void setDarkModeFromMedia() {
		UI.getCurrent()
			.getPage()
			.executeJs("return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches")
			.then(Boolean.class, this::setDarkMode);
	}

}
