/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.UIManager;

/**
 *
 * @author mfiume
 */
public class FontFactory {

    //fonts, in order of preference.    
    private static final String[] fontsToTry = new String[]{"HelveticaNeue-Medium", "Arial", "Lucida Sans Regular", "Times New Roman"};
    private static final Font titleFontPrimary;

    static {
        Font f = null;
        int i = 0;
        while ((f == null) && i < fontsToTry.length) {
            f = getFont(fontsToTry[i]);
            ++i;
        }

        if (f == null) {
            f = UIManager.getDefaults().getFont("TitledBorder.font");
        }
        //System.out.println("Setting font to " + f.getFontName());
        titleFontPrimary = f.deriveFont(20f);
    }
    private static final Font titleFont = (titleFontPrimary != null) ? titleFontPrimary : loadFont("/font/OpenSans-Regular.ttf").deriveFont(20f);
    private static final Font sectionHeaderFont = titleFont.deriveFont(24f);

    private static Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();

    {
        map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    private static final Font generalFontPrimary = getFont("HelveticaNeue-Light").deriveFont(map);
    private static final Font generalFont = (generalFontPrimary != null) ? generalFontPrimary : loadFont("/font/OpenSans-Regular.ttf").deriveFont(13f);

    public static Font getMenuTitleFont() {
        return titleFont;
    }

    public static Font getSectionHeaderFont() {
        return sectionHeaderFont;
    }

    public static Font getGeneralFont() {
        return generalFont;
    }

    private static Font loadFont(String resourcePath) {
        try {
            InputStream is = FontFactory.class.getResourceAsStream(resourcePath);
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            System.out.println("Loaded font " + font.getFamily());
            return font;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Font getFont(String fontName) {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();

        for (Font i : e.getAllFonts()) {
            String name = i.getFontName();

            if (name.equals(fontName)) {
                return i.deriveFont(13f);
            }
        }

        return null;
    }

}
