/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package com.bc.ceres.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.StringTokenizer;

/**
 * Helper class providing some utilities for Swing programming.
 */
public class SwingHelper {

    /**
     * Centers the given component over another component.
     * <p> The method performs the alignment by setting a newly computed location for the component. It does not alter
     * the component's size.
     *
     * @param comp      the component whose location is to be altered
     * @param alignComp the component used for the alignment of the first component, if <code>null</code> the component
     *                  is ceneterd within the screen area
     *
     * @throws IllegalArgumentException if the component is <code>null</code>
     */
    public static void centerComponent(Component comp, Component alignComp) {

        if (comp == null) {
            throw new IllegalArgumentException("comp must not be null");
        }

        Dimension compSize = comp.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x1, y1;

        if (alignComp != null && !new Rectangle(alignComp.getSize()).isEmpty()) {
            Point alignCompOffs = alignComp.getLocation();
            Dimension alignCompSize = alignComp.getSize();
            x1 = alignCompOffs.x + (alignCompSize.width - compSize.width) / 2;
            y1 = alignCompOffs.y + (alignCompSize.height - compSize.height) / 2;
        } else {
            x1 = (screenSize.width - compSize.width) / 2;
            y1 = (screenSize.height - compSize.height) / 2;
        }

        int x2 = x1 + compSize.width;
        int y2 = y1 + compSize.height;

        if (x2 >= screenSize.width) {
            x1 = screenSize.width - compSize.width - 1;
        }
        if (y2 >= screenSize.height) {
            y1 = screenSize.height - compSize.height - 1;
        }
        if (x1 < 0) {
            x1 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }

        comp.setLocation(x1, y1);
    }

    /**
     * Creates a &lt;html&gt; wrapped text with line breaks (&lt;br&gt;) which then can be used for example to
     * display word wrapped multi line tool tips. If the number of characters in a line exceeds 120
     * a line brake is inserted at the end of the current word. If you need a line break after an
     * other number of characters use {@link #createWordWrappedHtmlTextForSwingComponents(String, int)}
     *
     * @param text to be wrapped
     * @return &lt;html&gt; wrapped text
     * @see #createWordWrappedHtmlTextForSwingComponents(String, int)
     */
    public static String createWordWrappedHtmlTextForSwingComponents(String text) {
        final int defaultMaxLength = 120; // 120 characters
        return createWordWrappedHtmlTextForSwingComponents(text, defaultMaxLength);
    }

    /**
     * Creates a &lt;html&gt; wrapped text with line breaks (&lt;br&gt;) which then can be used for example to
     * display word wrapped multi line tool tips. If the number of characters in a line exceeds the given maxLength
     * a line brake is inserted at the end of the current word. You can also use the default line break after
     * exceeding a count of 120 characters ... use {@link #createWordWrappedHtmlTextForSwingComponents(String)}
     *
     * @param text to be wrapped
     * @param maxLength num of characters
     * @return &lt;html&gt; wrapped text
     * @see #createWordWrappedHtmlTextForSwingComponents(String)
     */
    public static String createWordWrappedHtmlTextForSwingComponents(String text, int maxLength) {
        if (text.trim().length() <= maxLength) return text;

        final StringBuilder builder = new StringBuilder("<html>");

        StringTokenizer tokenizer = new StringTokenizer(text, " ", true);
        int length = 0;
        while (tokenizer.hasMoreTokens()) {
            String string = tokenizer.nextToken();
            if (length == 0 && string.trim().length() == 0) {
                continue;
            }
            length += string.length();
            builder.append(string);
            if (length >= maxLength) {
                builder.append("<br>");
                length = 0;
            }
        }

        builder.append("</html>");
        return builder.toString();
    }
}
