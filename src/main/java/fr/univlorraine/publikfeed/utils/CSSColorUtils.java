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
package fr.univlorraine.publikfeed.utils;

import com.helger.css.decl.CSSHSL;
import com.helger.css.decl.CSSRGB;
import com.helger.css.utils.CSSColorHelper;

/**
 * Outils pour la gestion des couleurs CSS.
 * @author Adrien Colson
 */
public final class CSSColorUtils {

	private static final String PATTERN_HEX_COLOR_SHORT = "^#[0-9a-fA-F]{3}$";
	private static final String PATTERN_HEX_COLOR_LONG = "^#[0-9a-fA-F]{6}$";

	private CSSColorUtils() {
	}

	/**
	 * @param  color chaine de caractères
	 * @return       true si la chaine passée est une couleur css supportée
	 */
	public static boolean isSupportedColor(final String color) {
		return color != null && (color.matches(PATTERN_HEX_COLOR_SHORT)
			|| color.matches(PATTERN_HEX_COLOR_LONG)
			|| CSSColorHelper.isRGBColorValue(color)
			|| CSSColorHelper.isHSLColorValue(color));
	}

	/**
	 * @param  color couleur hex, rgb ou hsl
	 * @return       couleur rgb correspondante
	 */
	public static CSSRGB getRGBColor(final String color) {
		if (color.matches(PATTERN_HEX_COLOR_SHORT)) {
			return new CSSRGB(
				Integer.parseInt(color.substring(1, 2).repeat(2), 16),
				Integer.parseInt(color.substring(2, 3).repeat(2), 16),
				Integer.parseInt(color.substring(3).repeat(2), 16));
		}
		if (color.matches(PATTERN_HEX_COLOR_LONG)) {
			return new CSSRGB(
				Integer.parseInt(color.substring(1, 3), 16),
				Integer.parseInt(color.substring(3, 5), 16),
				Integer.parseInt(color.substring(5), 16));
		}
		if (CSSColorHelper.isRGBColorValue(color)) {
			return CSSColorHelper.getParsedRGBColorValue(color);
		}
		if (CSSColorHelper.isHSLColorValue(color)) {
			CSSHSL hslColor = CSSColorHelper.getParsedHSLColorValue(color);
			int[] rgbValues = CSSColorHelper.getHSLAsRGBValue(
				Float.parseFloat(hslColor.getHue()),
				Float.parseFloat(hslColor.getSaturation().replace("%", "")),
				Float.parseFloat(hslColor.getLightness().replace("%", "")));
			return new CSSRGB(rgbValues[0], rgbValues[1], rgbValues[2]);
		}
		throw new IllegalArgumentException(String.format("La couleur %s n'est pas de type hex, rgb ou hsl.", color));
	}

	/**
	 * @param  color   couleur hex, rgb ou hsl
	 * @param  percent taux d'opacité
	 * @return         couleur rgba ou hsla correspondante
	 */
	public static String getAlphaColor(final String color, final float percent) {
		if (color.matches(PATTERN_HEX_COLOR_SHORT)) {
			return CSSColorHelper.getRGBAColorValue(
				Integer.parseInt(color.substring(1, 2).repeat(2), 16),
				Integer.parseInt(color.substring(2, 3).repeat(2), 16),
				Integer.parseInt(color.substring(3).repeat(2), 16),
				percent);
		}
		if (color.matches(PATTERN_HEX_COLOR_LONG)) {
			return CSSColorHelper.getRGBAColorValue(
				Integer.parseInt(color.substring(1, 3), 16),
				Integer.parseInt(color.substring(3, 5), 16),
				Integer.parseInt(color.substring(5), 16),
				percent);
		}
		if (CSSColorHelper.isRGBColorValue(color)) {
			return CSSColorHelper.getParsedRGBColorValue(color).getAsRGBA(percent).getAsCSSString();
		}
		if (CSSColorHelper.isHSLColorValue(color)) {
			return CSSColorHelper.getParsedHSLColorValue(color).getAsHSLA(percent).getAsCSSString();
		}
		throw new IllegalArgumentException(String.format("La couleur %s n'est pas de type hex, rgb ou hsl.", color));
	}

}
