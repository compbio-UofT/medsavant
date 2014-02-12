package org.ut.biolab.medsavant.client.view.util.form;

import org.ut.biolab.medsavant.client.api.Listener;

import java.util.ArrayList;

/**
* Created by mfiume on 2/2/2014.
*/
public class NiceFormModel {

    private final ArrayList<NiceFormFieldGroup> groups;
    private Listener<NiceFormModel> listener;

    public NiceFormModel() {
        this.groups = new ArrayList<NiceFormFieldGroup>();
    }

    public void addGroup(NiceFormFieldGroup group) {
        this.groups.add(group);

        if (listener != null) {
            listener.handleEvent(this);
        }
    }

    public void setListener(Listener<NiceFormModel> l) {
        this.listener = l;
    }

    public ArrayList<NiceFormFieldGroup> getGroups() {
        return groups;
    }
}
