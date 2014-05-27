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
package org.ut.biolab.medsavant.client.app;

import java.net.URL;

/**
 *
 * @author mfiume
 */
public class AppInfo implements Comparable {

    private String name;
    private String category;
    private String version;
    private String compatibleWith;
    private String newInVersion;
    private String shortdesc;
    private String description;
    private String author;
    private String web;
    private URL downloadURL;
    private String id;

    public AppInfo(String name, String version, String category, String compatibleWith, String newInVersion, String shortdesc, String description, String author, String web, URL downloadURL) {
        this.name = name;
        this.version = version;
        this.category = category;
        this.compatibleWith = compatibleWith;
        this.newInVersion = newInVersion;
        this.shortdesc = shortdesc;
        this.description = description;
        this.author = author;
        this.web = web;
        this.downloadURL = downloadURL;
    }

    public String getShortDescription(){
        return shortdesc;
    }

    public String getNewInVersion(){
        return newInVersion;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getCategory() {
        return category;
    }

    public String getSDKVersion() {
        return compatibleWith;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getWeb() {
        return web;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppInfo other = (AppInfo) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof AppInfo) {
            AppInfo other = (AppInfo) t;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return name.compareTo(other.name);
            }
        }
        return -1;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    @Override
    public String toString() {
        return "AppInfo{" + "name=" + name + ", category=" + category + ", version=" + version + ", compatibleWith=" + compatibleWith + ", description=" + description + ", author=" + author + ", web=" + web + ", downloadURL=" + downloadURL + '}';
    }
}
