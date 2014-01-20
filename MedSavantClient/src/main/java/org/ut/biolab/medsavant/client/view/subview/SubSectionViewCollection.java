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
package org.ut.biolab.medsavant.client.view.subview;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class SubSectionViewCollection extends AppSubSection {

    Map<String, AppSubSection> subsectionMap;
    private JPanel view;
    private List<Component> menuComponents = new ArrayList<Component>();
    private final ButtonGroup buttonGroup;
    private JPanel contentPanel;
    private AppSubSection currentView;
    private String firstPageName;
    boolean firstPageShown = false;

    public SubSectionViewCollection(MultiSectionApp parent, String page) {
        super(parent, page);
        subsectionMap = new HashMap<String, AppSubSection>();

        buttonGroup = new ButtonGroup();

        initView();

    }

    @Override
    public Component[] getSubSectionMenuComponents() {
        Component[] arr = new Component[menuComponents.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = menuComponents.get(i);
        }
        return arr;
    }

    public void addSubSectionView(AppSubSection v) {

        this.subsectionMap.put(v.getPageName(), v);

        final String title = v.getPageName();
        JRadioButton button = new JRadioButton(v.getPageName()) {

            {
                this.setOpaque(false);
                this.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            over = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            over = false;
                            repaint();
                        }
                    });
              }
            private boolean over = false;

            @Override
            public void paintComponent(Graphics g) {

                String title = this.getText();

                Color textColor = Color.white;
                Color bgColor = new Color(255,255,255,255);

                if (this.isSelected()) {
                    textColor = Color.white;
                    bgColor = Color.darkGray;

                } else if (over) {
                    bgColor = Color.gray;
                    //textColor = Color.black;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                //g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

                //g2d.setColor(getBackground());
                //g2d.fillRect(0, 0, getWidth(), getHeight());

                int width = g2d.getFontMetrics().stringWidth(title);
                int height = g2d.getFontMetrics().getAscent();

                int hpad = 8;
                int vpad = 4;

                if (over || this.isSelected()) {
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
        menuComponents.add(button);

        if (subsectionMap.keySet().size() == 1) {
            button.setSelected(true);
            firstPageName = v.getPageName();
        }

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                setPage(title);
            }

        });

        menuComponents.add(Box.createHorizontalStrut(5));



        //menuPanel.add(glue);
    }

    int blah = 0;

    void setPage(String pageName) {
        if (currentView != null) {
            currentView.viewDidUnload();
        }
        currentView = subsectionMap.get(pageName);
        contentPanel.removeAll();
        contentPanel.add(currentView.getView(), BorderLayout.CENTER);
        currentView.viewWillLoad();
    }

    @Override
    public JPanel getView() {
        return view;
    }

    @Override
    public void viewWillLoad() {
        if (!firstPageShown && firstPageName != null) {
            setPage(firstPageName);
            firstPageShown = true;
        }
        if (currentView != null) {
            currentView.viewWillLoad();
        }
    }

    @Override
    public void viewDidUnload() {
        for (AppSubSection v : subsectionMap.values()) {
            v.viewDidUnload();
        }
    }

    private void initView() {
        view = new JPanel();
        view.setLayout(new BorderLayout());

        //menuPanel = new JPanel();//ViewUtil.getQuaternaryBannerPanel();
        //menuPanel.setBorder(ViewUtil.getSmallBorder());
        //ViewUtil.applyHorizontalBoxLayout(menuPanel);
        //menuPanel.add(Box.createHorizontalGlue());

        //glue = Box.createHorizontalGlue();
        //menuPanel.add(glue);

        //panel.add(menuPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        //contentPanel.setBackground(Color.red);
        contentPanel.setLayout(new BorderLayout());

        view.add(contentPanel, BorderLayout.CENTER);
    }
}
