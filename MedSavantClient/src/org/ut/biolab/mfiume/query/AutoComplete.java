package org.ut.biolab.mfiume.query;


/**
 * Ref
 * http://elliotth.blogspot.com/2004/09/cocoa-like-search-field-for-java.html
 * for the grey placeholder
 * @author Thien Rong
 */
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.text.*;

public class AutoComplete extends JComboBox implements KeySelectionManager {

    public static void main(String arg[]) {
        JFrame f = new JFrame("AutoCompleteComboBox");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(200, 300);

        //String[] names = {"Beate", "Claudia", "Fjodor", "Fred",
        //    "Friedrich", "Fritz", "Frodo", "Hermann", "Willi"};
        //JComboBox cBox= new AutoComplete(names);
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        Locale[] locales = Locale.getAvailableLocales();//
        final AutoComplete cBox = new AutoComplete(model, "Search...");
        f.add(cBox, BorderLayout.NORTH);
        f.add(new JTextField("normal"));

        f.setVisible(true);

        for (Locale locale : locales) {
            //    model.addElement(locale);
        }
        JButton btnRandomAdd = new JButton("random add");
        btnRandomAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Random r = new Random();
                cBox.setText(r.nextLong() + "");
                cBox.addSearchToHistory();
            }
        });
        f.add(btnRandomAdd, BorderLayout.SOUTH);


    /*
    public static void main(String[] args) {
    JFrame f = new JFrame();
    // A JPanel with our JTextField and JButton...
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    final JTextField text = new JTextField("Hello, World");
    text.addActionListener(new ActionListener() {

    public void actionPerformed(ActionEvent e) {
    panel.add(new JButton(text.getText()));
    }
    });
    JButton button = new JButton("oh");
    panel.add(button);
    panel.add(text);

    /* and now for the main trick:
    We transfer the JTextField's border TO THE JPanel as this...
     *
    Border textBorder = text.getBorder();
    text.setBorder(null);
    panel.setBorder(textBorder);

    // ...and play a little whith the JButton

    button.setBackground(text.getBackground()); //setting the same border as the JTF
    button.setBorderPainted(false); //dispose of the JB border
    button.setContentAreaFilled(false); //don't paint the border..
    button.setMargin(new Insets(1, 1, 1, 1)); // set a smaller margin
    button.setOpaque(true);

    f.add(panel);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.pack();
    f.setVisible(true);
    }
     */
    }
    private String searchFor;
// timer for autocomplete
    private long lap;
    private JTextField tf;
    private DefaultComboBoxModel model;

    public class CBDocument extends PlainDocument {

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }
            super.insertString(offset, str, a);
            if (!isPopupVisible() && str.length() != 0) {
                fireActionEvent();
            }
        }
    }

    public String getText() {
        return tf.getText();
    }

    public void setText(String t) {
        tf.setText(t);
    }

    public synchronized void addTextActionListener(ActionListener l) {
        tf.addActionListener(l);
    }

    public AutoComplete(DefaultComboBoxModel model, String placeholdText) {
        super(model);
        this.model = model;
        this.setEditable(true);

        lap =
                new java.util.Date().getTime();
        setKeySelectionManager(this);

        if (getEditor() != null) {
            tf = (JTextField) getEditor().getEditorComponent();
            if (tf != null) {
                tf.setDocument(new CBDocument());
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        JTextField tf = (JTextField) getEditor().getEditorComponent();
                        String text = tf.getText();
                        ComboBoxModel aModel = getModel();
                        String current;

                        for (int i = 0; i <
                                aModel.getSize(); i++) {
                            current = aModel.getElementAt(i).toString();
                            if (current.toLowerCase().startsWith(text.toLowerCase())) {
                                tf.setText(current);
                                tf.setSelectionStart(text.length());
                                tf.setSelectionEnd(current.length());
                                break;

                            }


                        }
                    }
                });
            }

        }
        tf.addFocusListener(new PlaceholderText(placeholdText));
        initKeyListener();

    }

    public void addSearchToHistory() {
        String text = tf.getText();
        if (text.length() == 0) {
            return;
        }

        boolean found = false;
        for (int i = 0; i <
                model.getSize(); i++) {
            if (model.getElementAt(i).toString().equals(text)) {
                found = true;
                break;

            }


        }
        if (found == false) {
            model.addElement(text);
        }

        tf.setText(text);
    }

    public int selectionForKey(char aKey, ComboBoxModel aModel) {
        long now = new java.util.Date().getTime();
        if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE &&
                searchFor.length() > 0) {
            searchFor = searchFor.substring(0, searchFor.length() - 1);
        } else {
            if (lap + 1000 < now) {
                searchFor = "" + aKey;
            } else {
                searchFor = searchFor + aKey;
            }

        }
        lap = now;
        String current;

        for (int i = 0; i <
                aModel.getSize(); i++) {
            current = aModel.getElementAt(i).toString().toLowerCase();
            if (current.toLowerCase().startsWith(searchFor.toLowerCase())) {
                return i;
            }

        }
        return -1;
    }

    public void fireActionEvent() {
        super.fireActionEvent();
    }

    /**
     * Replaces the entered text with a gray placeholder string when the
     * search field doesn't have the focus. The entered text returns when
     * we get the focus back.
     */
    class PlaceholderText implements FocusListener {

        private String placeholderText;
        private String previousText = "";
        private Color previousColor;

        PlaceholderText(String placeholderText) {
            this.placeholderText = placeholderText;
            focusLost(null);
        }

        public void focusGained(FocusEvent e) {
            tf.setForeground(previousColor);
            tf.setText(previousText);
        }

        public void focusLost(FocusEvent e) {
            previousText = tf.getText();
            previousColor = getForeground();
            if (previousText.length() == 0) {
                tf.setForeground(Color.GRAY);
                tf.setText(placeholderText);
            }
        }
    }

    private void initKeyListener() {
        tf.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    tf.setText("");
                }

            }
        });
    }
}