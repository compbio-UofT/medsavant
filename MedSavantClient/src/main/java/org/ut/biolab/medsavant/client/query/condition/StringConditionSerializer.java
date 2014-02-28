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
package org.ut.biolab.medsavant.client.query.condition;

import java.util.Arrays;
import java.util.List;
import org.ut.biolab.medsavant.client.query.view.SearchConditionEditorView;

/**
 *
 * @author mfiume
 */
public class StringConditionSerializer implements ConditionSerializer {

    private final SearchConditionEditorView editor;

    public StringConditionSerializer(SearchConditionEditorView editor) {
        this.editor = editor;
    }

    @Override
    public SearchConditionEditorView getEditor() {
        return editor;
    }

    String DELIM = ",";

    @Override
    public String serializeConditions(Object s) {
        List values = (List) s;
        StringBuilder result = new StringBuilder();
        for (Object string : values) {
            result.append(string);
            result.append(DELIM);
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
    }

    @Override
    public Object deserializeConditions(String s) {
        String[] arr = s.split(DELIM);
        return Arrays.asList(arr);
    }
}
