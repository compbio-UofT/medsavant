/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ut.biolab.medsavant.client.view.dialog.FormEditorDialog;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 * A controller for a form. A form is a collection of CustomFields together with their constraints. This class registers
 * customFields and constraints, and provides methods for communicating these to the view (e.g. FormEditorDialog). For
 * each form, this class should be extended to define the submitForm dialog and setup any required constraints.
 * 
 * @see CustomField
 * @see CustomFieldConstraint
 * @see FormEditorDialog
 * @author jim
 */
public abstract class FormController{
    private CustomField[] fields;

    private Map<String, CustomFieldConstraint> constraints = new HashMap<String, CustomFieldConstraint>();

    private Set<String> enumColumns;

    private String autoIncKey;

    /**
     * Constructs a new form controller with the given fields. "AutoIncrement" fields should be included in 'fields',
     * and also specified in autoIncKey.
     * 
     * @param fields The fields for this form, including any "AutoIncrement" fields
     * @param autoIncKey The AutoIncrement field, if applicable, null otherwise.
     */
    public FormController(CustomField[] fields, CustomField autoIncKey) {
        this.fields = fields;
        if (autoIncKey != null) {
            this.autoIncKey = autoIncKey.getColumnName();
        } else {
            this.autoIncKey = null;
        }

        for (CustomField f : fields) {
            addDefaultConstraint(f);
        }
    }

    /**
     * Checks whether the given value 'val' satisfies the constraints defined on the field 'cf'.
     * 
     * @param err If non null, this String array will be set to an error message if the constraint fails.
     * @return true if the value satisfies the registered constraints, false otherwise.
     */
    public final boolean isValid(CustomField cf, String val, String[] err) {
        CustomFieldConstraint cfc = this.constraints.get(cf.getColumnName());
        if (cfc != null) {
            boolean ret = cfc.isValid(val);
            if (!ret && (err != null)) {
                err[0] = cfc.getErrorMessage();
            }
            return ret;
        }
        return true;
    }

    /**
     * Checks whether the given value 'val' satisfies the constraints defined on the field 'cf'.
     * 
     * @return true if the value satisfies the registered constraints, false otherwise.
     */
    public final boolean isValid(CustomField cf, String val) {
        return isValid(cf, val, null);
    }

    /**
     * @return The value/label pairs allowed by the enum constraint defined on the field 'cf', or null if the field has
     *         no enum constraint. e.g. {{Value,Label},{Value,Label},...}
     */
    public final String[][] allowedValues(CustomField cf) {
        if (this.enumColumns != null && this.enumColumns.contains(cf.getColumnName())) {
            EnumConstraint ec = (EnumConstraint) this.constraints.get(cf.getColumnName());
            return ec.getAllowedValues();
        }

        return null;
    }

    // Called when form is submitted.
    public abstract void submitForm(List<CustomField> cf, List<String> validatedValues);

    /**
     * @return The fields associated with this form.
     */
    public CustomField[] getFields() {
        return this.fields;
    }

    /**
     * Ideally, this method would be part of CustomField.
     * 
     * @return indicates whether the field is an 'autoincrement' field.
     */
    public boolean isAutoInc(CustomField f) {
        if (this.autoIncKey != null && this.autoIncKey.equalsIgnoreCase(f.getColumnName())) {
            return true;
        }
        return false;
    }

    /**
     * Sets up a default constraint on the given field with a generic error message, according to that field's type. For
     * example, boolean values are constrained to 0 or 1, dates are constrained to conform to YYYY-MM-DD format, etc.
     */
    protected final void addDefaultConstraint(CustomField cf) {
        String cn = cf.getAlias();
        String errMsg;
        switch (cf.getColumnType()) {
            case BOOLEAN:
                errMsg = cn + " is not a valid true/false value";
                setConstraint(cf.getColumnName(), new EnumConstraint(new String[][] { {"1", "True"}, {"0", "False"}},
                    cf.getColumnLength(), errMsg));
                break;
            case INTEGER:
                errMsg = cn + " is not a valid integer";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_INTEGER, cf.getColumnLength(), errMsg));
                break;
            case DATE:
                errMsg = cn + " is not a valid date";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_DATE, cf.getColumnLength(), errMsg));
                break;
            case DECIMAL:
                errMsg = cn + " is not a valid decimal value";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_DECIMAL, cf.getColumnLength(), errMsg));
                break;
            case FLOAT:
                errMsg = cn + " is not a valid decimal value";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_DECIMAL, cf.getColumnLength(), errMsg));
                break;
            case VARCHAR:
                errMsg = cn + " is not a valid string";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_VARCHAR, cf.getColumnLength(), errMsg));
                break;
            case TEXT:
                errMsg = cn + " is not a valid string";
                setConstraint(cf.getColumnName(),
                    new RegexpConstraint(RegexpConstraint.REGEXP_DEFAULT_VARCHAR, cf.getColumnLength(), errMsg));
                break;
        }
    }

    /**
     * Convenience method that adds or modifies constraints for all of the given fields so that missing values are no
     * longer allowed for that field.
     */
    protected final void setRequiredFields(CustomField[] fields) {
        for (CustomField f : fields) {
            setRequiredField(f);
        }
    }

    /**
     * Convenience method that adds or modifies constraints for the given fields so that missing values are no longer
     * allowed for that field.
     */
    protected final void setRequiredField(CustomField cf) {
        String columnName = cf.getColumnName();
        CustomFieldConstraint cfc = this.constraints.get(columnName);
        if (cfc != null) {
            cfc.setRequired(true);
        } else {
            cfc = new CustomFieldConstraint(true, cf.getColumnLength());
        }
        this.constraints.put(columnName, cfc);
    }

    /**
     * Sets a constraint for a CustomField, overwriting any existing constraint.
     * 
     * @see CustomFieldConstraint
     */
    protected final void setConstraint(CustomField cf, CustomFieldConstraint cfc) {
        setConstraint(cf.getColumnName(), cfc);
    }

    /**
     * Sets an enum constraint for a CustomField, overwriting any existing constraint.
     * 
     * @see EnumConstraint
     */
    protected final void setConstraint(CustomField cf, EnumConstraint ec) {
        setConstraint(cf.getColumnName(), ec);
    }

    private void setConstraint(String colName, CustomFieldConstraint cfc) {
        this.constraints.put(colName, cfc);
    }

    private void setConstraint(String colName, EnumConstraint ec) {
        setConstraint(colName, (CustomFieldConstraint) ec);
        if (this.enumColumns == null) {
            this.enumColumns = new HashSet<String>();
        }
        this.enumColumns.add(colName);
    }
}
