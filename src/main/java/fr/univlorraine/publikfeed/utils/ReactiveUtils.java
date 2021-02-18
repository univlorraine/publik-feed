package fr.univlorraine.publikfeed.utils;

import java.util.function.Function;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.Command;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class ReactiveUtils {

	public static void addDisposableWhenAttached(final Component component, final Function<AttachEvent, Disposable> disposableFunction) {
		component.addAttachListener(attachEvent -> {
			Disposable disposable = disposableFunction.apply(attachEvent);
			component.addDetachListener(detachEvent -> disposable.dispose());
		});
	}

	public static void subscribeWhenAttached(final Component component, final Flux<Command> commandFlux) {
		addDisposableWhenAttached(component, attachEvent -> commandFlux.subscribe(attachEvent.getUI()::access));
	}
}
