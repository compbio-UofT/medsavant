/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author khushi
 */
public class EntrezButton extends JButton{
    static String charset = "UTF-8";
    private String geneName;
    public EntrezButton(final String geneName){
        super(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
        this.geneName = geneName;
        ViewUtil.makeSmall(this);
        this.putClientProperty("JButton.buttonType", "textured");

        this.setVerticalTextPosition(SwingConstants.CENTER);
        this.setHorizontalTextPosition(SwingConstants.LEFT);
        this.setToolTipText("Lookup Gene on Entrez");
        this.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    URL destinationURL= getURL();
                    java.awt.Desktop.getDesktop().browse(destinationURL.toURI());
               } catch (Exception ex) {
                    DialogUtils.displayError("Problem launching website.");
               }
           }
       });

    }
    
    public URL getURL() throws MalformedURLException, UnsupportedEncodingException, IOException {
        String baseUrl1 = "http://www.ncbi.nlm.nih.gov/gene?term=";
        String baseUrl2 = "http://www.ncbi.nlm.nih.gov";
        URL url = new URL(baseUrl1 + URLEncoder.encode(geneName, charset));
        Document doc = Jsoup.parse(url, 20 * 1000);
        Element link = doc.select("div.rslt:has(em:contains(Homo))").first();
        return new URL(baseUrl2 + link.select("a").attr("href"));
    }
}
