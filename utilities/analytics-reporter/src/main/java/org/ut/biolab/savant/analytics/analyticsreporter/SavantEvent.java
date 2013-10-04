package org.ut.biolab.savant.analytics.analyticsreporter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mfiume
 */
class SavantEvent {


    private int id;
    private String eventText;
    private Map<String,String> eventKeyValueMap;

    public SavantEvent(int id, String eventText) {
        this.id = id;
        this.eventText = eventText;
        this.eventKeyValueMap = parseKeyValueMapFromEventText(eventText);
    }

    public int getId() {
        return id;
    }

    public String getEventText() {
        return eventText;
    }

    public String getEventValueForKey(String key) throws NoKeyForEeventException {
        if (eventKeyValueMap.containsKey(key)) {
            return eventKeyValueMap.get(key);
        } else {
            throw new NoKeyForEeventException(id, key);
        }
    }

    private static Map<String, String> parseKeyValueMapFromEventText(String eventText) {
        Map<String,String> map = new HashMap<String,String>();
        String pairSep = "\\|";
        String kvpSep = "=>";

        for (String kvp : eventText.split(pairSep,-1)) {
            String[] components = kvp.split(kvpSep);
            map.put(components[0], components.length > 1 ? components[1] : "");
        }

        return map;
    }

    boolean hasValueForKey(String key,String value) {
        return hasKey(key) && eventKeyValueMap.get(key).equals(value);
    }

    boolean hasKey(String key) {
        return eventKeyValueMap.containsKey(key);
    }

    public static class NoKeyForEeventException extends Exception {
        private final String key;
        private final int id;

        public NoKeyForEeventException(int id, String key) {
            this.id = id;
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public int getId() {
            return id;
        }
    }

    @Override
    public String toString() {
        return "SavantEvent{" + "id=" + id + ", eventText=" + eventText + '}';
    }


}
