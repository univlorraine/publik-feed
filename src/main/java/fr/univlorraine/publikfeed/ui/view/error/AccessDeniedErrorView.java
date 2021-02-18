package fr.univlorraine.publikfeed.ui.view.error;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;

@Tag(Tag.DIV)
@SuppressWarnings("serial")
public class AccessDeniedErrorView extends Component implements HasErrorParameter<AccessDeniedException> {

	/**
	 * @see com.vaadin.flow.router.HasErrorParameter#setErrorParameter(com.vaadin.flow.router.BeforeEnterEvent, com.vaadin.flow.router.ErrorParameter)
	 */
	@Override
	public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<AccessDeniedException> parameter) {
		/* Redirige */
		event.rerouteTo(AccessDeniedView.class);

		/* Renvoie le statut forbidden */
		return HttpServletResponse.SC_FORBIDDEN;
	}

}
