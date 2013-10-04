package org.ut.biolab.savant.analytics.analyticsreporter;

/**
 *
 * @author mfiume
 */
class ReportLog {

    public enum Type { INFO, ERROR };

    private Type type;
    private String message;

    public ReportLog(Type type, String message) {
        this.type = type;
        this.message = message;
        System.out.println(this.toString());
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ReportLog{" + "type=" + type + ", message=" + message + '}';
    }

    
}
