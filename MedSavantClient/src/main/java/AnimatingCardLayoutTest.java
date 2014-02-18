/*
Copyright (c) 2004 
Sam Berlin sberlin@limepeer.com
Luca Lutterotti Luca.Lutterotti@ing.unitn.it
Dmitry Markman, PhD dmarkman@mac.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.javadev.effects.*;
import org.javadev.*;

/*  TO DO:
With your permission, Dmitry, I'd like to take your code and make it into an easily pluggable LayoutManager 
based on CardLayout.  The end result would be to have an API that enables someone to basically do:

  JPanel panel = new JPanel();
  AnimatingCardLayout layout = new AnimatingCardLayout();
  layout.setAnimation(new MyAnimation()); // if someone wants to provide their own animation
  panel.setLayout(layout);
  panel.add(comp1, "page1");
  panel.add(comp2, "page2");

and then all someone has to do is call "layout.show(container, "page1");" or layout.show(container, "page2");" and it would flip between the cards using the animation (in your case, the DashBoardEffect).  The DashBoardEffect would be one implementation of an animation that AnimatingCardLayout could use.

*/

public class AnimatingCardLayoutTest extends JPanel {
    CardLayout cardLayout;
    JPanel basePanel;
    JPanel  componentPanelTable;
    JPanel  componentPanelPref;
    boolean wasApplication = false;
    boolean wasInit = false;
    
    public static void main(String []args){
        JFrame frame = new JFrame("DashboardEffect");
        frame.setLocation(100,100);
        final AnimatingCardLayoutTest de = new AnimatingCardLayoutTest(true);
        de.init();
        
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(de,BorderLayout.CENTER);
        frame.setSize(300,350);
        frame.setVisible(true);
    }
    
    public AnimatingCardLayoutTest(){
        this(false);
    }
    
    public AnimatingCardLayoutTest(boolean wasApplication){
        super();
        this.wasApplication = wasApplication;
    }

	public void init() {
	    if(wasInit) return;
	    wasInit = true;
        this.setLayout(new BorderLayout(6, 6));
        componentPanelTable = new ComponentPanel2();
        componentPanelPref = new ComponentPanel1();

        basePanel = new JPanel();
        basePanel.setOpaque(false);
        this.add(basePanel, BorderLayout.CENTER);
        String mode = "fade";
        System.out.println("mode "+mode);
        if(mode != null && mode.equalsIgnoreCase("cube")){
            cardLayout = new AnimatingCardLayout(new CubeAnimation());
        }else if(mode != null && mode.equalsIgnoreCase("slide")){
            cardLayout = new AnimatingCardLayout(new SlideAnimation());
        }else if(mode != null && mode.equalsIgnoreCase("radial")){
            cardLayout = new AnimatingCardLayout(new RadialAnimation());
        }else if(mode != null && mode.equalsIgnoreCase("fade")){
            cardLayout = new AnimatingCardLayout(new FadeAnimation());
        }else if(mode != null && mode.equalsIgnoreCase("iris")){
            cardLayout = new AnimatingCardLayout(new IrisAnimation());
        }else{
            cardLayout = new AnimatingCardLayout(new DashboardAnimation());
        }
        ((AnimatingCardLayout)cardLayout).setAnimationDuration(200);
        basePanel.setLayout(cardLayout);

        basePanel.add(componentPanelTable,"component");
        basePanel.add(componentPanelPref,"preferences");
        cardLayout.show(basePanel,"component");
        
	}

    public void startComponentToPreferences(){
        cardLayout.show(basePanel,"preferences");
    }

    public void startPreferencesToComponent(){
        cardLayout.show(basePanel,"component");
    }
    



class ComponentPanel1 extends JPanel{

    public ComponentPanel1(){
        super();
        setLayout(new BorderLayout());
        setBackground(Color.red);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.red);
        add(buttonPanel,BorderLayout.SOUTH);
        JButton button = new JButton("Done");
		button.setOpaque(false);
        buttonPanel.add(button);
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                startPreferencesToComponent();
             }
        });
    }
}

class ComponentPanel2 extends JPanel{
  Object [][]tableData;
  Object []columnNames;

    public ComponentPanel2(){
        super();
        int nColumns = 10;
        int nRows = 30;
        setLayout(new BorderLayout());
        setBackground(Color.green);
        columnNames = new String[nColumns];
        for(int i = 0; i < columnNames.length; i++){
            columnNames[i] = "Column "+i;
        }
        tableData = new String[nRows][nColumns];
        for(int i = 0; i < nRows; i++){
            for(int j = 0; j < nColumns; j++){
                tableData[i][j] = "Row "+i+" Column "+j;
            }
        }
        JTable table = new JTable(tableData,columnNames);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane sp = new JScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(sp,BorderLayout.CENTER);
        final Corner corner = new Corner();
        sp.setCorner(JScrollPane.LOWER_RIGHT_CORNER,corner);
        table.addMouseListener(new MouseAdapter(){
            public void mouseExited(MouseEvent evt){
                corner.wasEntered = false;
                corner.repaint();
            }
            public void mouseEntered(MouseEvent evt){
                corner.wasEntered = true;
                corner.repaint();
            }
        });

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(buttonPanel,BorderLayout.SOUTH);
        JButton rotationButton = new JButton("Rotate");
		rotationButton.setOpaque(false);
        buttonPanel.add(rotationButton);
        rotationButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt){
                startComponentToPreferences();
            }
        });
    }

class Corner extends JPanel{
boolean wasEntered = false;
    public Corner(){
        super();
        addMouseListener(new MouseAdapter(){
            public void mouseReleased(MouseEvent evt){
                startComponentToPreferences();
            }
            public void mouseExited(MouseEvent evt){
                wasEntered = false;
                repaint();
            }
            public void mouseEntered(MouseEvent evt){
                wasEntered = true;
                repaint();
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        Color needColor = (wasEntered)?Color.gray:getBackground();
        g.setColor(needColor);
        g.fillRect(0,0,getSize().width,getSize().height);
    }
}
  }

}
