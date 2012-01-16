package org.ut.biolab.medsavant.view.subview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SubSectionViewCollection extends SubSectionView {

    private final String name;
    Map<String, SubSectionView> subsectionMap;
    private JPanel panel;
    private JPanel menuPanel;
    private Component glue;
    private final ButtonGroup buttonGroup;
    private JPanel contentPanel;

    public SubSectionViewCollection(SectionView parent, String name) {
        super(parent);
        this.name = name;
        subsectionMap = new HashMap<String, SubSectionView>();

        buttonGroup = new ButtonGroup();

        initView();

    }

    public void addSubSectionView(SubSectionView v) {

        this.subsectionMap.put(v.getName(), v);

        menuPanel.remove(glue);

        // init the view
        v.getView(true);

        final String title = v.getName();
        JRadioButton button = new JRadioButton(v.getName()) {
            
            private boolean mouseOver = false;
            private boolean init = false;
            
            private boolean checkListener(){
                if(!init){
                    this.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            mouseOver = true;
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            mouseOver = false;
                        }
                    });
                    init = true;
                }
                return mouseOver;
            }
            
            @Override
            public void paintComponent(Graphics g) {
                                
                boolean over = checkListener();
                
                String title = this.getText();

                Color textColor = Color.BLACK;
                Color bgColor = new Color(255,255,255,255);
                
                if (this.isSelected()) {
                    textColor = Color.white;
                    bgColor = Color.gray;
                } else if (over) {
                    bgColor = Color.lightGray;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());

                int width = g2d.getFontMetrics().stringWidth(title);
                int height = g2d.getFontMetrics().getAscent();

                int hpad = 8;
                int vpad = 4;
                
                if (over || this.isSelected()){
                    g2d.setColor(bgColor);
                    int recWidth = width+2*hpad;
                    int recHeight = height+2*vpad;
                    g2d.fillRoundRect((getWidth()-recWidth)/2, (getHeight()-recHeight)/2, recWidth, recHeight, 20, 20);
                }

                g2d.setColor(textColor);
                g2d.drawString(title, (getWidth()-width)/2, (getHeight()+height)/2-2);
                
            }
        };
        
        buttonGroup.add(button);
        menuPanel.add(button);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setPage(title);
            }

        });

        menuPanel.add(Box.createHorizontalStrut(5));
        menuPanel.add(glue);
    }

    int blah = 0;

    void setPage(String pageName) {
        SubSectionView view = this.subsectionMap.get(pageName);      
        contentPanel.removeAll();

        contentPanel.add(view.getView(true),BorderLayout.CENTER);
        view.viewDidLoad();
        contentPanel.updateUI();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getView(boolean update) {
        return panel;
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
    }

    private void initView() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuPanel = new JPanel();//ViewUtil.getQuaternaryBannerPanel();
        menuPanel.setBorder(ViewUtil.getSmallBorder());
        ViewUtil.applyHorizontalBoxLayout(menuPanel);
        menuPanel.add(Box.createHorizontalGlue());

        glue = Box.createHorizontalGlue();
        menuPanel.add(glue);

        panel.add(menuPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        //contentPanel.setBackground(Color.red);
        contentPanel.setLayout(new BorderLayout());

        panel.add(contentPanel, BorderLayout.CENTER);
    }
}
