/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.list;

import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListColorScheme;
import com.explodingpixels.painter.GradientWithBorderPainter;
import com.explodingpixels.painter.MacWidgetsPainter;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author mfiume
 */
class CustomColorScheme implements SourceListColorScheme {

     private static final MacWidgetsPainter<Component> ACTIVE_FOCUSED_SELECTION_PAINTER =
            createSourceListActiveFocusedSelectionPainter();

    private static final MacWidgetsPainter<Component> ACTIVE_UNFOCUSED_SELECTION_PAINTER =
            createSourceListActiveUnfocusedSelectionPainter();

    private static final MacWidgetsPainter<Component> INACTIVE_FOCUSED_SELECTION_PAINTER =
            createSourceListInactiveSelectionPainter();

    private static Color ACTIVE_BACKGROUND_COLOR = new Color(245,245,245);
    private static Color INACTIVE_BACKGROUND_COLOR = new Color(245,245,245);

    private static final Color CATEGORY_FONT_COLOR = new Color(64,64,64);
    private static final Color CATEGORY_FONT_SHADOW_COLOR = new Color(0, 0, 0, 0);
    private static final Color ITEM_FONT_COLOR = new Color(64,64,64);
    private static final Color SELECTED_ITEM_FONT_COLOR = CATEGORY_FONT_COLOR;
    private static final Color SELECTED_ITEM_FONT_SHADOW_COLOR = CATEGORY_FONT_SHADOW_COLOR;

    private static final Icon COLLAPSED_ICON = new ImageIcon(
            SourceList.class.getResource(
                    "/com/explodingpixels/macwidgets/images/source_list_right_arrow.png"));

    private static final Icon EXPANDED_ICON = new ImageIcon(
            SourceList.class.getResource(
                    "/com/explodingpixels/macwidgets/images/source_list_down_arrow.png"));

    public MacWidgetsPainter<Component> getActiveFocusedSelectedItemPainter() {
        return ACTIVE_FOCUSED_SELECTION_PAINTER;
    }

    public MacWidgetsPainter<Component> getActiveUnfocusedSelectedItemPainter() {
        return ACTIVE_UNFOCUSED_SELECTION_PAINTER;
    }

    public MacWidgetsPainter<Component> getInactiveSelectedItemPainter() {
        return INACTIVE_FOCUSED_SELECTION_PAINTER;
    }

    public Color getCategoryTextColor() {
        return CATEGORY_FONT_COLOR;
    }

    public Color getCategoryTextShadowColor() {
        return CATEGORY_FONT_SHADOW_COLOR;
    }

    public Color getUnselectedItemTextColor() {
        return ITEM_FONT_COLOR;
    }

    public Color getSelectedItemTextColor() {
        return SELECTED_ITEM_FONT_COLOR;
    }

    public Color getSelectedItemFontShadowColor() {
        return SELECTED_ITEM_FONT_SHADOW_COLOR;
    }

    public Color getActiveBackgroundColor() {
        return ACTIVE_BACKGROUND_COLOR;
    }

    public Color getInactiveBackgroundColor() {
        return INACTIVE_BACKGROUND_COLOR;
    }

    public Icon getUnselectedCollapsedIcon() {
        return COLLAPSED_ICON;
    }

    public Icon getUnselectedExpandedIcon() {
        return EXPANDED_ICON;
    }

    public Icon getSelectedCollapsedIcon() {
        return COLLAPSED_ICON;
    }

    public Icon getSelectedExpandedIcon() {
        return EXPANDED_ICON;
    }

    public Color getBadgeTextColor() {
        return getActiveBackgroundColor();
    }

    public Color getSelectedBadgeColor() {
        return Color.gray;
    }

    public Color getActiveUnselectedBadgeColor() {
        return Color.gray;
    }

    public Color getInativeUnselectedBadgeColor() {
        return Color.gray;
    }

    private static MacWidgetsPainter<Component> createSourceListActiveFocusedSelectionPainter() {
        Color topLineColor = new Color(188,192,198);
        Color topColor = new Color(200,204,211);
        Color bottomColor = new Color(174,179,188);
        return new GradientWithBorderPainter(topLineColor, bottomColor, topColor, bottomColor);
    }

    private static MacWidgetsPainter<Component> createSourceListActiveUnfocusedSelectionPainter() {
        return createSourceListInactiveSelectionPainter();
    }

    private static MacWidgetsPainter<Component> createSourceListInactiveSelectionPainter() {
        Color topLineColor = new Color(0x979797);
        Color topColor = new Color(0xb4b4b4);
        Color bottomColor = new Color(0x8a8a8a);
        return new GradientWithBorderPainter(topLineColor, bottomColor, topColor, bottomColor);
    }
}
