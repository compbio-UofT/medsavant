package org.ut.biolab.mfiume.app;

import java.net.URL;

/**
 *
 * @author mfiume
 */
public class AppInfo implements Comparable {

    /*public enum State {
        LOADED {
            public String toString() {return "Loaded";}
        }, INSTALLED{
            public String toString() {return "Installed";}
        }, UNINSTALLED{
            public String toString() {return "Uninstalled";}
        }, UNKNOWN {
            public String toString() {return "Unknown";}
        };
    }*/

    //private State state;
    private String name;
    private String category;
    private String version;
    private String compatibleWith;
    private String description;
    private String author;
    private String web;
    private URL downloadURL;
    private String id;

    public AppInfo(String name, String version, String category, String compatibleWith, String description, String author, String web, URL downloadURL) {
        this.name = name;

        this.version = version;
        this.category = category;
        this.compatibleWith = compatibleWith;
        this.description = description;
        this.author = author;
        this.web = web;
        this.downloadURL = downloadURL;
        //state = State.UNKNOWN;
    }

    /*public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }*/

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getCategory() {
        return category;
    }

    public String getCompatibleWith() {
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
}
