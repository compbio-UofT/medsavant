package org.ut.biolab.medsavant.client.view.util.form;

import org.ut.biolab.medsavant.client.api.Listener;

import java.util.ArrayList;

/**
* Created by mfiume on 2/2/2014.
*/
public class NiceFormModel {

    private final ArrayList<NiceFormField> fields;
    private Listener<NiceFormModel> listener;

    public NiceFormModel() {
        this.fields = new ArrayList<NiceFormField>();
    }

    public void addField(NiceFormField field) {
        this.fields.add(field);

        if (listener != null) {
            listener.handleEvent(this);
        }
    }

    public void setListener(Listener<NiceFormModel> l) {
        this.listener = l;
    }

    public ArrayList<NiceFormField> getFields() {
        return fields;
    }
}
