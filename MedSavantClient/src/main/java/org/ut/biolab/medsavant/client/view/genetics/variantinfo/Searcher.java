/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author mfiume
 */
class Searcher {

    static String pubmedPrefix = "http://www.ncbi.nlm.nih.gov/pubmed?term=";
    static String googlePrefix = "http://www.google.com/search?hl=en&q=";
    static String googleScholarPrefix = "http://scholar.google.com/scholar?q=";

    static String charset = "UTF-8";

    static void searchGoogle(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(googlePrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }

    static void searchGoogleScholar(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(googleScholarPrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }

    static void searchPubmed(String search) throws UnsupportedEncodingException, MalformedURLException, IOException, URISyntaxException {
        URL url = new URL(pubmedPrefix + URLEncoder.encode(search, charset));
        java.awt.Desktop.getDesktop().browse(url.toURI());
    }
}
