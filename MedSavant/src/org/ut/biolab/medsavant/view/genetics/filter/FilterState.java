/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.util.Map;

/**
 *
 * @author Andrew
 */
public class FilterState {
    
    private String name;
    private String id;
    private Map<String, String> values;
    private FilterType type;
    
    public static enum FilterType {NUMERIC, STRING, BOOLEAN, COHORT, GENELIST, GENERIC, PLUGIN};
    
    public FilterState(FilterType type, String name, String id, Map<String, String> values){
        this.name = name;
        this.id = id;
        this.values = values;
        this.type = type;
    }
    
    public String getName(){
        return name;
    }
    
    public String getId(){
        return id;
    }
    
    public Map<String, String> getValues(){
        return values;
    }
    
    public FilterType getType() {
        return type;
    }
    
    public String generateXML(){
        String s = "\t\t<filter name=\"" + name + "\" id=\"" + id + "\" type=\"" + type + "\" >\n";
        for(String key : values.keySet()){
            s += "\t\t\t<param key=\"" + key + "\" value=\"" + values.get(key) + "\" />\n";
        }
        s += "\t\t</filter>";
        return s;
    }
    
}
