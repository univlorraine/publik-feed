package fr.univlorraine.publikfeed.ui.view.error;

import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouteNotFoundError;

/**
 * En cas de route non trouvée, renvoie vers la page d'accueil.
 * @author Adrien Colson
 */
@SuppressWarnings("serial")
public class RouteNotFoundErrorView extends RouteNotFoundError {

	private static final String TARGET_ROUTE = "";

	/**
	 * @see com.vaadin.flow.router.RouteNotFoundError#setErrorParameter(com.vaadin.flow.router.BeforeEnterEvent, com.vaadin.flow.router.ErrorParameter)
	 */
	@Override
	public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<NotFoundException> parameter) {
		/* Redirige */
		event.rerouteTo(TARGET_ROUTE);

		/* Met à jour l'url */
		event.getUI().getPage().getHistory().replaceState(null, TARGET_ROUTE);

		/* Affiche une notification */
		Notification.show(getTranslation("error.routenotfound"));

		/* Renvoie le statut ok */
		return HttpServletResponse.SC_ACCEPTED;
	}

}
