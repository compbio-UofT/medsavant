/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.font;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 *
 * @author mfiume
 */
public class FontFactory {

    private static final Font titleFontPrimary = getFont("HelveticaNeue-Medium").deriveFont(20f);
    private static final Font titleFont = (titleFontPrimary != null) ? titleFontPrimary : loadFont("/font/OpenSans-Regular.ttf").deriveFont(20f);
    private static final Font sectionHeaderFont = titleFont.deriveFont(24f);//loadFont("/font/ostrich-regular.ttf").deriveFont(36f); //titleFont;
    private static final Font generalFontPrimary = getFont("HelveticaNeue-Light");
    private static final Font generalFont = (generalFontPrimary != null) ? generalFontPrimary : loadFont("/font/OpenSans-Regular.ttf").deriveFont(13f);
    
    public static Font getTitleFont() {
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
