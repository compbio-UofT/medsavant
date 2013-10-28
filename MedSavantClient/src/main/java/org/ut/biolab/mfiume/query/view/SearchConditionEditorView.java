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
package org.ut.biolab.mfiume.query.view;

import java.awt.Dimension;
import java.io.Serializable;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import org.ut.biolab.mfiume.query.SearchConditionItem;

/**
 *
 * @author mfiume
 */
public abstract class SearchConditionEditorView extends JPanel implements Serializable {

    protected final SearchConditionItem item;

    public SearchConditionEditorView(SearchConditionItem i) {
        this.setOpaque(false);        
        this.item = i;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    protected Dimension getDialogDimension(){
        return null;
    }
     
    public void saveSearchConditionParameters(String encoding) {
        item.setSearchConditionEncoding(encoding);
    }
    public abstract void loadViewFromSearchConditionParameters(String encoding) throws ConditionRestorationException;
    public void loadViewFromExistingSearchConditionParameters() throws ConditionRestorationException {
        String encoding = item.getSearchConditionEncoding();
        loadViewFromSearchConditionParameters(encoding);
    }

    public class ConditionRestorationException extends Exception {
        public ConditionRestorationException(String msg) {
            super(msg);
        }
    }

    /*
    public Dimension getDialogSize(){ //todo 
        
    } */
}
