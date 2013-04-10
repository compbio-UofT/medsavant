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
