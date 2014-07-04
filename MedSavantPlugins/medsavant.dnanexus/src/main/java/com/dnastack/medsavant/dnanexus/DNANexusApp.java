/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnastack.medsavant.dnanexus;

import com.dnanexus.DXContainer;
import com.dnanexus.DXEnvironment;
import com.dnanexus.DXFile;
import com.dnanexus.DXHTTPRequest;
import com.dnanexus.DXJSON;
import com.dnanexus.DXSearch;

//Probably unnecessary dependencies.  Still getting used to DNANexus API
//and gson is more familiar than jackson.
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.shared.appapi.MedSavantDashboardApp;

import javax.swing.SpringLayout;
import org.ut.biolab.medsavant.client.view.util.SpringUtilities;

/**
 *
 * @author jim
 */
public class DNANexusApp extends MedSavantDashboardApp {

    private JPanel outerPanel;
    private static final String APP_TITLE = "DNA Nexus Integration";
    private static final String DNANEXUS_APP_ICON = "/dnanexus_app_icon.png";
    private static final int INIT_X = 6;
    private static final int INIT_Y = 6;
    private static final int XPAD = 6;
    private static final int YPAD = 6;
    private static final int USERNAME_NUM_COLUMNS = 25;
    private static final int PASSWORD_NUM_COLUMNS = 25;
    private static final String DEFAULT_AUTHSERVER = "https://auth.dnanexus.com";
    private JTextField usernameTextArea;
    private JPasswordField passwordTextArea;
    private DXEnvironment dxEnvironment;
    private String bearerToken = "QmgxhSDCqRpqPh8pvN9VSqaMarUgrv20"; //one of my bearer tokens
    private String projectId = "";
    
    private class LoginCredentials {

        final String username;
        final String password;

        public LoginCredentials() {
            this.username = usernameTextArea.getText();
            this.password = new String(passwordTextArea.getPassword());
        }
    }

    public class DownloadInfo{
       private String url;
       private Map<String,String> headers;
       public DownloadInfo(String url, Map<String, String> headers){
           this.url = url;
           this.headers = headers;
       }

        public String getUrl() {
            return url;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
       
    }
    
    private void download(String fileId){
        
        String u = dxEnvironment.getApiserverPath()+"/"+fileId+"/download";
        System.out.println("Got download path"+u);
        DXHTTPRequest dxRequest = new DXHTTPRequest(dxEnvironment);
        String resp = dxRequest.request("/"+fileId+"/download", "{}");
        System.out.println("Got response: "+resp);       
        
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        DownloadInfo d = gson.fromJson(resp, DownloadInfo.class);
        
        //DownloadInfo d = DXAPI.fileDownload(fileId, DownloadInfo.class, dxEnvironment);
        //System.out.println("Got download info "+d);
    }
    
    private String getLoginStringAsJSON() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String ret = gson.toJson(new LoginCredentials(), LoginCredentials.class);
        System.out.println("Returning json: "+ret);
        return ret;
    }

    public static void main(String[] args){
        DNANexusApp dna = new DNANexusApp();
        JDialog jd = new JDialog();
        jd.setContentPane(dna.getContent());
        jd.pack();
        jd.setSize(640,480);
        jd.setVisible(true);
    }
    
    private void login() {
        try {
            System.out.println("Debug bearer token");
            Thread.sleep(1000);
            Object securityContext =
                    DXJSON.getObjectBuilder().put("auth_token_type", "Bearer")
                            .put("auth_token", bearerToken).build();
            if(securityContext == null){
                System.out.println("Security context is null!");
            }
            
            System.out.println("Setting up environment");
            dxEnvironment = 
                    DXEnvironment.Builder.fromDefaults().setBearerToken(bearerToken).setApiserverProtocol("https").build();
            if(dxEnvironment == null){
                System.out.println("Environment is null!");
            }
            Thread.sleep(1000);
            System.out.println("Executing search");
            //withClassFile
            
            DXContainer dc = DXContainer.getInstanceWithEnvironment("project-BK5fYZ8005vkQzKxBky00Pj1", dxEnvironment);
            System.out.println("Got dx container "+dc);
            DXSearch.FindDataObjectsResult<DXFile> fdor = DXSearch.findDataObjectsWithEnvironment(dxEnvironment).inProject(dc).withClassFile().execute();
            Thread.sleep(1000);
            System.out.println("Initializing list ");
            List<DXFile> l = fdor.asList();
            Thread.sleep(1000);   
            System.out.println("Showing results");
            
            Thread.sleep(1000);
            
            for (DXFile f : fdor.asList()) {
                String filename = f.describe().getName();
                if(filename.toLowerCase().endsWith(".vcf")){
                    System.out.println("Got vcf file "+filename+" with id "+f.getId());
                    download(f.getId());                                       
                }
                /*
                System.out.println("Got file: "+f.describe().getName());
                System.out.println("mediatype: "+f.describe().getMediaType());
                System.out.println("Tags: ");
                for(String tag : f.describe().getTags()){
                    System.out.println("\ttag="+tag);
                }
                System.out.println("types: ");
                for(String type : f.describe().getTypes()){
                    System.out.println("\ttype="+type);
                }
                //System.out.println(o.getId());
                //System.out.println("project object toString: "+o.getProject().toString());
                */
            }
 /*
            DXSearch.PropertiesQuery propertiesQuery = 
                    DXSearch.PropertiesQuery.anyOf(new ArrayList<DXSearch.PropertiesQuery>());
            
            
            
            DXProject.getInstanceWithEnvironment(projectId, dxEnvironment);
            DXHTTPRequest dxrequest = new DXHTTPRequest();
            
        //username, password, expires
            //where expires is a 
            JsonNode jsnode = DXJSON.parseJson(getLoginStringAsJSON());
            JsonNode response = dxrequest.request(DEFAULT_AUTHSERVER + "/authorizations", jsnode);
            System.out.println("Logged in and got response " + response);*/
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    private JPanel getEmptyPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        return p;
    }

    @Override
    public ImageIcon getIcon() {
        URL resource = DNANexusApp.class.getResource(DNANEXUS_APP_ICON);
        return new ImageIcon(resource);
    }

    @Override
    public JPanel getContent() {
        if (outerPanel == null) {
            outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        }
        usernameTextArea = new JTextField(USERNAME_NUM_COLUMNS);
        passwordTextArea = new JPasswordField(PASSWORD_NUM_COLUMNS);

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

        JPanel springPanel = new JPanel();
        springPanel.setLayout(new SpringLayout());
        innerPanel.setLayout(new SpringLayout());

        springPanel.add(new JLabel("Username: "));
        springPanel.add(usernameTextArea);
        springPanel.add(new JLabel("Password: "));
        springPanel.add(passwordTextArea);

        /* JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));*/
        JButton OKButton = new JButton("Login");
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                login();
            }
        });
        /*
         buttonPanel.add(Box.createHorizontalGlue());
         buttonPanel.add(OKButton);
         buttonPanel.add(Box.createHorizontalGlue());*/
        springPanel.add(OKButton);
        springPanel.add(getEmptyPanel());
        //springPanel.add(new JPanel());
        SpringUtilities.makeCompactGrid(springPanel,
                3, 2,
                INIT_X, INIT_Y,
                XPAD, YPAD);
        innerPanel.add(springPanel);
        //innerPanel.add(buttonPanel);
        outerPanel.add(innerPanel);
        return outerPanel;
    }

    @Override
    public void viewDidLoad() {

    }

    @Override
    public void viewDidUnload() {

    }

    @Override
    public String getTitle() {

        return APP_TITLE;
    }

}
