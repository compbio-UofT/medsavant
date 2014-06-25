/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.dashboard.LaunchableApp;
import org.ut.biolab.medsavant.client.view.dialog.ChangePasswordDialog;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.StandardFixableWidthAppPanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class AccountManagerApp implements LaunchableApp {
    
    private JPanel accountBlock;
    private JPanel colorBlock;
    
    public AccountManagerApp() {
    }
    
    private StandardFixableWidthAppPanel view;
    
    @Override
    public JPanel getView() {
        return view;
    }
    
    private void initView() {
        if (view == null) {
            view = new StandardFixableWidthAppPanel();
            accountBlock = view.addBlock("Account Information");
            colorBlock = view.addBlock("Colors");
            
            initColorBlock();
        }
    }
    
    @Override
    public void viewWillUnload() {
    }
    
    @Override
    public void viewWillLoad() {
        initView();
        refreshInfo();
    }
    
    @Override
    public void viewDidUnload() {
    }
    
    @Override
    public void viewDidLoad() {
    }
    
    @Override
    public String getName() {
        return "My Account";
    }
    
    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_ACCOUNT);
    }
    
    @Override
    public void didLogout() {
    }
    
    @Override
    public void didLogin() {
    }
    
    private void refreshInfo() {
        accountBlock.removeAll();
        
        accountBlock.setLayout(new MigLayout("insets 0, wrap"));
        KeyValuePairPanel kvp = new KeyValuePairPanel(1, true);
        
        JButton b = ViewUtil.getSoftButton("Change");
        b.addActionListener(new ActionListener() {
            
            private final String OLDPASS_LABEL = "Enter Current Password";
            private final String NEWPASS_LABEL1 = "Enter New Password";
            private final String NEWPASS_LABEL2 = "Confirm New Password";
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog jd = new ChangePasswordDialog();
                jd.setVisible(true);
            }
        });
        
        kvp.addKeyWithValue("Username", LoginController.getInstance().getUserName());
        kvp.addKeyWithValue("Password", ViewUtil.bulletStringOfLength(LoginController.getInstance().getPassword().length()));
        
        kvp.setAdditionalColumn("Password", 0, b);
        
        accountBlock.add(kvp);
        
        final ActionListener signOutActionListener = new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                MedSavantFrame.getInstance().requestLogoutAndRestart();
            }
        };
        
        JButton signOut = new JButton("Sign Out");
        signOut.setFocusable(false);
        
        signOut.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                signOutActionListener.actionPerformed(null);
            }
            
        });
        accountBlock.add(ViewUtil.alignRight(signOut));
    }
    
    private void initColorBlock() {
        KeyValuePairPanel colorKVP = new KeyValuePairPanel(1);
        
        final JColorChooser chooser = new JColorChooser(MedSavantFrame.getInstance().getDashboard().getBackground());
        JButton dashBoardColorButton = getButtonThatShows(chooser);
        
        JButton fileChooser = ViewUtil.getSoftButton("Choose Image");
        fileChooser.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose Image..");
                
                FileNameExtensionFilter filter
                        = new FileNameExtensionFilter("Supports Images (.jpg,.png)", "jpg", "png");
                fc.setFileFilter(filter);
                int returnVal = fc.showDialog(null, "Choose..");
                File file = null;
                
                switch (returnVal) {
                    case JFileChooser.APPROVE_OPTION:
                        file = fc.getSelectedFile();
                        break;
                    case JFileChooser.CANCEL_OPTION:
                    default:
                        return;
                }

                Image image = null;
                
                try {
                    image = ImageIO.read(file);
                    MedSavantFrame.getInstance().getDashboard().setBackgroundImage(image);
                } catch (IOException ex) {
                }
            }
            
        });
        
        colorKVP.addKeyWithValue("Dashboard", dashBoardColorButton);
        colorKVP.setAdditionalColumn("Dashboard", 0, fileChooser);
        
        chooser.getSelectionModel().addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                MedSavantFrame.getInstance().getDashboard().setBackground(chooser.getColor());
            }
            
        });
        
        colorBlock.setLayout(new MigLayout("insets 0,wrap"));
        colorBlock.add(ViewUtil.getGrayItalicizedLabel("Beta Feature"));
        colorBlock.add(colorKVP);
    }
    
    private JButton getButtonThatShows(final JColorChooser chooser) {
        final JButton b = ViewUtil.getSoftButton("Color Chooser");
        final JPopupMenu m = new JPopupMenu();
        m.add(chooser);
        b.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                m.show(b, 0, 0);
            }
            
        });
        
        return b;
    }
    
}
