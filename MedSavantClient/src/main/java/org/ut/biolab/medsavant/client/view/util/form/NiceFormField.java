package org.ut.biolab.medsavant.client.view.util.form;

/**
* Created by mfiume on 2/2/2014.
*/
public class NiceFormField {

    private final Object value;
    private String name;
    private NiceForm.FieldType type;
    private boolean isRequired;

    public NiceFormField(boolean isRequired, String name, NiceForm.FieldType type) {
        this(isRequired, name, type, null);
    }

    public NiceFormField(boolean isRequired, String name, NiceForm.FieldType type, Object value) {
        this.isRequired = isRequired;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public NiceForm.FieldType getType() {
        return type;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
