package fr.univlorraine.publikfeed.ui.component;

import com.helger.css.decl.CSSRGB;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;

import fr.univlorraine.publikfeed.utils.CSSColorUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;

@CssImport(value = "./styles/app-color.css")
@Tag("style")
@NoArgsConstructor
@SuppressWarnings("serial")
public class AppColorStyle extends Component {

	private static final String STYLE_TEMPLATE = "html {--app-color-rgb: %s,%s,%s;}";

	@Getter
	private String color;

	public AppColorStyle(final String color) {
		setColor(color);
	}

	public void setColor(final String color) {
		this.color = color;
		CSSRGB rgbColor = CSSColorUtils.getRGBColor(color);
		getElement().setText(String.format(STYLE_TEMPLATE, rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue()));
	}

}
