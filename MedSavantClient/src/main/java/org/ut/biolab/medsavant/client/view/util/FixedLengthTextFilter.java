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
package org.ut.biolab.medsavant.client.view.util;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A Document filter to limit the size of textfields (e.g. JTextField).
 * Usage: 
 * ((AbstractDocument)somecomponent.getDocument()).setDocumentFilter(
 *      new FixedLengthTextFilter(20)); 
 * 
 * Based on code at
 * http://www.coderanch.com/t/434129/GUI/java/Reg-Validatin-JTextFields
 * 
 * @author jim 
 */
public class FixedLengthTextFilter extends DocumentFilter{
    private int maxLength;
    
    public FixedLengthTextFilter(int maxLength){
        this.maxLength = maxLength;
    }
    
    public void insertString(DocumentFilter.FilterBypass filtby, int ofs, 
            String text, AttributeSet attrSet) throws BadLocationException{
        if(filtby.getDocument().getLength() + text.length() <= maxLength){
            filtby.insertString(ofs, text, attrSet);
        }else{
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    public void replace(DocumentFilter.FilterBypass filtby, int ofs, int length, 
            String text, AttributeSet attr) throws BadLocationException{
        if(filtby.getDocument().getLength() + text.length() - length 
                <= maxLength){
            filtby.replace(ofs, length, text, attr);
        }else{
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
}
