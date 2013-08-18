package org.ut.biolab.medsavant.shared.model;

import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 *
 */
public class AnnotatedColumn extends CustomField {

    private int annotationId;
    private int position;

    public AnnotatedColumn(String name, String typeStr, boolean filterable, String alias, String description) {
        super(name, typeStr, filterable, alias, description);
    }

    public AnnotatedColumn(int annotationId, int position,String name, String typeStr, boolean filterable, String alias, String description) {
        super(name, typeStr, filterable, alias, description);
        this.annotationId = annotationId;
        this.position = position;
    }

    public AnnotatedColumn(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt, boolean filterable, String alias, String description) {
        super(n, t, l, autoInc, notNull, indexed, dflt, filterable, alias, description);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
