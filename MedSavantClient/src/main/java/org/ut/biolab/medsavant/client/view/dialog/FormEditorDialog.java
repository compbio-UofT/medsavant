
package org.ut.biolab.medsavant.client.view.dialog;

import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import org.ut.biolab.medsavant.shared.format.CustomField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.JButton;
import javax.swing.JDialog;
import org.ut.biolab.medsavant.client.util.FormController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.util.FixedLengthTextFilter;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.client.view.util.SpringUtilities;

/**
 * A generic dialog for editing forms. 
 * 
 * @author jim
 * @see FormController
 */
public class FormEditorDialog extends javax.swing.JDialog {        
    
    //5 pixels of space between each form field.
    private static final int XPADDING = 5;
    private static final int YPADDING = 5;
    
    //maximum number of characters to allow in a textfield before a textarea
    //is used instead.
    private static final int MAX_TEXTFIELD_CHARS = 100;
    
    //maximum display width of a textfield or textarea.
    private static final int MAX_TEXTFIELD_DISPLAYCHARS = 20;
        
    //maximum number of rows in a textarea.   
    private static final int MAX_TEXTAREA_ROWS = 3; 
    
    //maximum number of radio buttons before we use a combo-box instead.
    private static final int MAX_NUM_RADIOBUTTONS = 3;
           
    //The label to give a radio button representing a 'missing' value.
    //This is unused for now.
    private static final String RADIOBUTTON_MISSING_LABEL = "N/A";
    
    
    private int numCols;
    
    
    private FormController controller;
    
    
    private CustomField[] fields;    
    private String[] fieldValues;
    
    /**
     * Constructs a new dialog under the control of the given controller, using a
     * single column layout.  
     * 
     */
    public FormEditorDialog(FormController controller){     
        this(controller, 1);              
    }
    
    
    /**
     * Constructs a new dialog under the control of the given controller, using the
     * specified number of columns.
     * 
     * A "column" contains a set of fields 
     * together with their input components.  If numCols is set to 1, the form will
     * be oriented vertically:
     *  JLabel InputComponent
     *  JLabel InputComponent
     *  ...
     * If set to 2, the form will show up as;
     *  JLabel InputComponent  JLabel Input Component
     *  JLabel InputComponent  JLabel Input Component
     *  ...  
     */
    public FormEditorDialog(FormController controller, int numCols){
        super(MedSavantFrame.getInstance());
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.fields = controller.getFields();
        this.fieldValues = new String[fields.length];       
        this.controller = controller;
        this.numCols = numCols;        
        initForm();        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);  
        this.setLocationRelativeTo(MedSavantFrame.getInstance());
    }
              
    /**
     * Closes the dialog.
     */
    protected void close(){        
        ViewController.getInstance().refreshView();
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     *      
     * @return A 'tip' for the given field, which will be displayed when the mouse is rolled over the field label.
     */
    protected String getTip(CustomField f) {                    
            String s = f.getAlias() + " | " + f.getColumnType().toString().toLowerCase();
            switch(f.getColumnType()) {
                case DATE:
                    s += "(yyyy-mm-dd)";
                    break;
                case BOOLEAN:
                    s += "(true/false)";
                    break;
            }
        return s;        
    }

    //Initializes all the swing components for the form.  
    private void initForm(){       
        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new SpringLayout());
        
        //position of outerPanel relative to top-left of parent       
        int xpos = 5; 
        int ypos = 5;        
        
        //Number of fields visible may be less than fields.length if 
        //there are auto-increment fields. 
        int numFieldsVisible = fields.length;
                
        
        final List<ActionListener> listeners = new ArrayList(fields.length);
        
        
        for(int fi = 0; fi < fields.length; ++fi){            
            final int fieldIndex = fi;            
            CustomField field = fields[fieldIndex];

            if(controller.isAutoInc(field)){
                numFieldsVisible--;
                continue;
            }                       
            
           JLabel jl = new JLabel(field.getAlias());           
           jl.setToolTipText(getTip(field));
           
           outerPanel.add(jl);
 
           JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));           
                    
            // Only booleans and enums get radio buttons or comboboxes.
            if(controller.allowedValues(field) != null){                
                final ButtonGroup bg = new ButtonGroup();
                JPanel rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));                                
                
                String[][] allowedValues = controller.allowedValues(field);
                if(allowedValues.length <= MAX_NUM_RADIOBUTTONS){           
                    // If the number of choices is < MAX_NUM_RADIOBUTTONS, 
                    // use Radio buttons
                    for(String[] allowedValue : allowedValues){
                        JRadioButton jr;
                        if(allowedValue == null){
                            jr = new JRadioButton(RADIOBUTTON_MISSING_LABEL, false);
                            jr.setActionCommand(RADIOBUTTON_MISSING_LABEL);
                        }else{                        
                            jr = new JRadioButton(allowedValue[1],false);
                            jr.setActionCommand(allowedValue[0]);
                        }
                        
                        //always select the first button by default.
                        if(bg.getButtonCount() == 0){
                            jr.setSelected(true);
                        }
                        bg.add(jr);                        
                        rbPanel.add(jr);
                    }                         
                    innerPanel.add(rbPanel);
                     listeners.add(new ActionListener(){
                       @Override
                       public void actionPerformed(ActionEvent e){
                          Enumeration<AbstractButton> rbs = 
                                 bg.getElements();  
                          while(rbs.hasMoreElements()){  
                            JRadioButton rb = (JRadioButton)rbs.nextElement();
                            if(rb.isSelected()){ 
                               //fieldValues[fieldIndex] = (String)rb.getText();
                                fieldValues[fieldIndex] = rb.getActionCommand();
                            }
                          }
                       }
                    });
                }else{
                    // If the number of choices is >= MAX_NUM_RADIOBUTTONS, 
                    // Use Comboboxes.
                    final JComboBox jc = new JComboBox(allowedValues);                    
                    
                    innerPanel.add(jc);
                    
                    listeners.add(new ActionListener(){
                        @Override
                       public void actionPerformed(ActionEvent e){
                           fieldValues[fieldIndex] = (String)jc.getSelectedItem();
                       }
                    });
                }
            }else if(field.getColumnType() == ColumnType.VARCHAR || 
                    field.getColumnType() == ColumnType.TEXT ||
                    field.getColumnType() == ColumnType.INTEGER ||
                    field.getColumnType() == ColumnType.DECIMAL ||
                    field.getColumnType() == ColumnType.FLOAT ||
                    field.getColumnType() == ColumnType.DATE ||
                    field.getColumnType() == ColumnType.BLOB){
            
                //For all of these types, use either textfields or textareas.
                int colLength = field.getColumnLength();
                
                final JTextComponent jc;
                if(colLength > MAX_TEXTFIELD_CHARS){ 
                    //Use textareas, which may in general have fewer than MAX_TEXTAREA_ROWS, 
                    //depending on the values of MAX_TEXTFIELD_DISPLAYCHARS, MAX_TEXTAREA_ROWS, and
                    //colLength                   
                    int numRows = (int)(Math.ceil(colLength / MAX_TEXTFIELD_DISPLAYCHARS));
                    numRows = Math.min(numRows, MAX_TEXTAREA_ROWS);
                    JTextArea ja;                    
                    ja = new JTextArea(numRows, MAX_TEXTFIELD_DISPLAYCHARS);
                    
                    //Prevent user from entering too many characters for the field.
                    ((AbstractDocument)ja.getDocument()).setDocumentFilter(
                            new FixedLengthTextFilter(colLength));
                   
                    
                    //Make tab and shift-tab go to next and previous field, just like it 
                    //does for textfields.
                    ja.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
                    ja.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
                    
                    JScrollPane jsp =  new JScrollPane(ja);
                    jsp.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                   
                    innerPanel.add(jsp);                   
                    jc = ja;
                    
                }else{
                    //Use textfields.
                    
                    int textfieldWidth = Math.min(colLength, MAX_TEXTFIELD_DISPLAYCHARS);
                    JTextField jf = new JTextField(textfieldWidth);
                                
                    //Prevent user from entering too many characters for the field.
                    ((AbstractDocument)jf.getDocument()).setDocumentFilter(
                            new FixedLengthTextFilter(colLength));
                                
                    jf.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
                    
                    innerPanel.add(jf);                    
                    jc = jf;
                }                
                 listeners.add(new ActionListener(){
                     @Override
                       public void actionPerformed(ActionEvent e){
                           fieldValues[fieldIndex] = (String)jc.getText();
                       }
                 });                 
            }//end else if 
            outerPanel.add(innerPanel);            
        } // end for
        
        
        //Debugging error messages.
        /*
        System.out.println("vardump");
        System.out.println((int)Math.ceil(fields.length / (double)numCols));
        System.out.println(numCols * 2);
        System.out.println(xpos);
        System.out.println(ypos);
        System.out.println(XPADDING);
        System.out.println(YPADDING);
        System.out.println("DBG = "+dbg);
        */
        

        SpringUtilities.makeCompactGrid(outerPanel, 
                (int)Math.ceil(numFieldsVisible / (double)numCols), numCols * 2, 
                xpos, ypos, 
                XPADDING, YPADDING);
        
        outerPanel.setOpaque(true);

        
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));           

        JButton OKButton = new JButton("OK");
        
        final JDialog that = this;
        OKButton.addActionListener(new ActionListener(){
                       @Override
                       public void actionPerformed(ActionEvent e){      
                           //Trigger the action listeners on all the input components, so that they
                           //can get the user input, translate it into a String, and store 
                           //that string in the corresponding 'fieldValues' array entry.
                           for(ActionListener al : listeners){
                               al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "FormSubmit"));
                           }             
                            
                           //Validate Form Input, and collect any errors into the string 'errors'.
                            String errors =  "";
                            List<String> visibleFieldValues = new LinkedList<String>();
                            List<CustomField> visibleFields = new LinkedList<CustomField>();

                            String[] err = new String[1];
                            for(int i = 0; i < fields.length; ++i){   
                                if(!controller.isAutoInc(fields[i])){
                                    visibleFieldValues.add(fieldValues[i]);
                                    visibleFields.add(fields[i]);

                                    if(!controller.isValid(fields[i], fieldValues[i], err)){                                        
                                        errors += err[0]+"\n";  
                                    }
                                }                                
                            }
        
                            if(!errors.isEmpty()){
                                JOptionPane.showMessageDialog(that, errors, "Invalid Input",  JOptionPane.ERROR_MESSAGE);                                
                            }else{
                                controller.submitForm(visibleFields, visibleFieldValues);
                                close();
                            }
        
                       }
                 });      
        
            JButton cancelButton = new JButton("Cancel"); 
            cancelButton.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    close();
                }
            });
        
        buttonPanel.add(OKButton);
        buttonPanel.add(cancelButton);
        
        JPanel borderPanel  = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));        
        borderPanel.add(outerPanel);
        borderPanel.add(buttonPanel);
        this.setContentPane(borderPanel);
        
        pack();        
    }// end initForm()
    
    
        
}
