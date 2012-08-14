/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration for a DOM property.
 * <p>
 * For internal use only.
 */
public enum Property {
	PropertyInnerHTML, PropertyAddedInnerHTML, PropertyValue, PropertyDisabled, PropertyChecked, PropertySelected, PropertySelectedIndex, PropertyMultiple, PropertyTarget, PropertyIndeterminate, PropertySrc, PropertyText, PropertyScript, PropertyColSpan, PropertyRowSpan, PropertyReadOnly, PropertyTabIndex, PropertyClass, PropertyStyle, PropertyStyleWidthExpression, PropertyStylePosition, PropertyStyleZIndex, PropertyStyleFloat, PropertyStyleClear, PropertyStyleWidth, PropertyStyleHeight, PropertyStyleLineHeight, PropertyStyleMinWidth, PropertyStyleMinHeight, PropertyStyleMaxWidth, PropertyStyleMaxHeight, PropertyStyleLeft, PropertyStyleRight, PropertyStyleTop, PropertyStyleBottom, PropertyStyleVerticalAlign, PropertyStyleTextAlign, PropertyStylePadding, PropertyStylePaddingTop, PropertyStylePaddingRight, PropertyStylePaddingBottom, PropertyStylePaddingLeft, PropertyStyleMarginTop, PropertyStyleMarginRight, PropertyStyleMarginBottom, PropertyStyleMarginLeft, PropertyStyleCursor, PropertyStyleBorderTop, PropertyStyleBorderRight, PropertyStyleBorderBottom, PropertyStyleBorderLeft, PropertyStyleColor, PropertyStyleOverflowX, PropertyStyleOverflowY, PropertyStyleOpacity, PropertyStyleFontFamily, PropertyStyleFontStyle, PropertyStyleFontVariant, PropertyStyleFontWeight, PropertyStyleFontSize, PropertyStyleBackgroundColor, PropertyStyleBackgroundImage, PropertyStyleBackgroundRepeat, PropertyStyleBackgroundAttachment, PropertyStyleBackgroundPosition, PropertyStyleTextDecoration, PropertyStyleWhiteSpace, PropertyStyleTableLayout, PropertyStyleBorderSpacing, PropertyStyleZoom, PropertyStyleVisibility, PropertyStyleDisplay, PropertyStyleBoxSizing;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
