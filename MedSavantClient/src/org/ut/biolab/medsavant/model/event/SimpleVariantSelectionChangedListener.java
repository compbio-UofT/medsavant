package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspectorDialog;

public interface SimpleVariantSelectionChangedListener {

    public void simpleVariantSelectionChanged(VariantInspectorDialog.SimpleVariant r);

}