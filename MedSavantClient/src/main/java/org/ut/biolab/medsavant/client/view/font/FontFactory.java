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
package org.ut.biolab.medsavant.client.view.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mfiume
 */
public class FontFactory {
    
    private static Log LOG = LogFactory.getLog(FontFactory.class);

    //fonts, in order of preference.        
    private static final Font titleFontPrimary = initFont(new String[]{"HelveticaNeue-Medium", "Arial", "Lucida Sans Regular", "Times New Roman"}, 20f);
    private static final Font titleFont = (titleFontPrimary != null) ? titleFontPrimary : loadFont("/font/OpenSans-Regular.ttf").deriveFont(20f);
    private static final Font sectionHeaderFont = titleFont.deriveFont(24f);

    private static final Map<TextAttribute, Object> fontAttributeMap = new Hashtable<TextAttribute, Object>();

    private static final Font serifFontPrimary = loadFont("/font/Oswald-Light.otf").deriveFont(13f);
    private static final Font serifFont = (serifFontPrimary != null) ? serifFontPrimary : initFont(new String[]{"Times New Roman"}, 13f);

    static{
        fontAttributeMap.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
    }

    private static final Font generalFontPrimary = initFont(new String[]{"HelveticaNeue-Light", "Arial", "Lucida Sans Regular", "Times New Roman"}, fontAttributeMap);
    private static final Font generalFontSecondary = loadFont("/font/OpenSans-Regular.ttf").deriveFont(13f);
    private static final Font generalFont = 
            (generalFontPrimary != null) ? generalFontPrimary : 
            ((generalFontSecondary != null) ? generalFontSecondary : 
            (new JLabel().getFont())); // last resort

    private static Font initFont(String[] fontsToTry, Map<? extends AttributedCharacterIterator.Attribute, ?> attributes) {
        Font f = initFont(fontsToTry);
        return f.deriveFont(attributes);
    }

    private static Font initFont(String[] fontsToTry, float size) {
        Font f = initFont(fontsToTry);
        return f.deriveFont(size);
    }

    private static Font initFont(String[] fontsToTry) {
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
        return f;
    }

    public static Font getMenuTitleFont() {
        return titleFont;
    }

    public static Font getSectionHeaderFont() {
        return sectionHeaderFont;
    }

    public static Font getGeneralFont() {
        return generalFont;
    }

    public static Font getSerifFont() {
        return serifFont;
    }
    
    private static Font loadFont(String resourcePath) {
        try {
            InputStream is = FontFactory.class.getResourceAsStream(resourcePath);
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
            System.out.println("Loaded font " + font.getFamily());
            return font;
        } catch (FontFormatException ex) {
            LOG.info("Error loading font");
            return null;
        } catch (IOException ex) {
            LOG.info("Error loading font");
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
