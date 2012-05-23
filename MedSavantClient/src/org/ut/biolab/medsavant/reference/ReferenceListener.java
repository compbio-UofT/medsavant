package org.ut.biolab.medsavant.reference;

public interface ReferenceListener {

    public void referenceAdded(String name);

    public void referenceRemoved(String name);

    public void referenceChanged(String name);
}