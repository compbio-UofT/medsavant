package org.ut.biolab.medsavant.listener;

public interface ReferenceListener {

    public void referenceAdded(String name);

    public void referenceRemoved(String name);

    public void referenceChanged(String prnameojectName);
}