package medsavant.variantannotation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.dialog.PageList;
import com.jidesoft.wizard.*;
import jannovar.Jannovar;
import jannovar.annotation.AnnotationList;
import jannovar.exception.AnnotationException;
import jannovar.exception.FileDownloadException;
import jannovar.exception.JannovarException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.SerializationManager;
import jannovar.io.TranscriptDataDownloader;
import jannovar.io.UCSCKGParser;
import jannovar.io.VCFLine;
import jannovar.io.VCFReader;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.shared.model.VariantTag;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.serverapi.VariantManagerAdapter;
import org.ut.biolab.medsavant.client.util.ClientNetworkUtils;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.util.ProjectWorker;
import org.ut.biolab.medsavant.shared.util.ExtensionsFileFilter;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;


/**
 * Wizard for importing VCFs (Variant Call Files).
 * Updated to run Jannovar before uploading to server.
 * 
 * @author Andrew
 * @author rammar
 */
public class VCFAnnotationWizard extends WizardDialog {

    private static final Log LOG = LogFactory.getLog(VCFAnnotationWizard.class);
    private static VariantManagerAdapter manager = MedSavantClient.VariantManager;
    private List<VariantTag> variantTags;
    private File[] variantFiles;
    private boolean includeHomoRef = false;
    private JComboBox locationField;
    private boolean uploadRequired;
    private JPanel chooseContainer;
    private JLabel chooseTitleLabel;
    private JPanel filesOnMyComputerPanel;
    private JPanel filesOnMedSavantServerPanel;
    private JTextField serverPathField;
    private JTextField emailField;
    private JCheckBox autoPublish;
    private static final String NOTIFICATION_TITLE = "Importing Variants";

	private boolean useJannovar= true;
	private SerializationManager sManager= new SerializationManager();
	private HashMap<Byte,Chromosome> chromosomeMap;
	private String dirPath;
	private ArrayList<TranscriptModel> transcriptModelList= null;
	private final String UCSCserializationFileName= "ucsc.ser";
	
	
    public VCFAnnotationWizard() {
        setTitle("Import Variants Wizard");
        WizardStyle.setStyle(WizardStyle.MACOSX_STYLE);

        variantTags = new ArrayList<VariantTag>();

        //add pages
        PageList model = new PageList();
        model.append(getWelcomePage());
        model.append(getVCFSourcePage());
        model.append(getChooseFilesPage());
        model.append(getAddTagsPage());
        model.append(getNotificationsPage());
        model.append(getQueuePage());
        //model.append(getSetLivePage());
        model.append(getCompletePage());
        setPageList(model);

        pack();
        setResizable(true);
        setLocationRelativeTo(null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(720, 600);
    }

    private void setUploadRequired(boolean req) {
        uploadRequired = req;

        if (chooseContainer != null) {
            chooseContainer.removeAll();

            if (uploadRequired) {
                chooseTitleLabel.setText("Choose the variant file(s) to be imported:");
                chooseContainer.add(filesOnMyComputerPanel, BorderLayout.CENTER);
            } else {
                chooseTitleLabel.setText("Specify the full directory path containing variant file(s) to be imported:");
                chooseContainer.add(filesOnMedSavantServerPanel, BorderLayout.CENTER);
            }
        }
    }

    private AbstractWizardPage getVCFSourcePage() {
        return new DefaultWizardPage("Location of Files") {
            private JRadioButton onMyComputerButton = new JRadioButton("This computer");
            private JRadioButton onMedSavantServerButton = new JRadioButton("The MedSavant server");

            {
                ButtonGroup g = new ButtonGroup();
                g.add(onMyComputerButton);
                g.add(onMedSavantServerButton);

                onMyComputerButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setUploadRequired(onMyComputerButton.isSelected());
                    }
                });

                onMedSavantServerButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        setUploadRequired(onMyComputerButton.isSelected());
                    }
                });

                addText("The VCFs I want to import are on:");
                addComponent(onMyComputerButton);
                addComponent(onMedSavantServerButton);

                onMyComputerButton.setSelected(true);


            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };
    }

    private AbstractWizardPage getWelcomePage() {

        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Import Variants") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        String projectName = ProjectController.getInstance().getCurrentProjectName();
        String referenceName = ReferenceController.getInstance().getCurrentReferenceName();

        page.addText("This wizard will help you import variants for:");

        JLabel nameLabel = new JLabel(projectName + " (" + referenceName + ")");
        nameLabel.setFont(ViewUtil.getMediumTitleFont());
        page.addComponent(nameLabel);
        page.addText("If the variants are with respect to another reference\ngenome, switch to that reference and try importing again.");

        return page;
    }

    private AbstractWizardPage getChooseFilesPage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Choose Files") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);

                if (uploadRequired) {
                    if (variantFiles != null && variantFiles.length > 0) {
                        fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                    } else {
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                    }
                } else {
                    if (serverPathField.getText().isEmpty()) {
                        fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                    } else {
                        fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                    }
                }
            }
        };

        chooseContainer = new JPanel();
        chooseContainer = new JPanel();
        chooseContainer.setLayout(new BorderLayout());
        chooseTitleLabel = new JLabel();
        filesOnMyComputerPanel = populateOnMyComputerPanel(page);// populateRepositoryPanel();
        filesOnMedSavantServerPanel = populateOnServerPanel(page); //populateLocalPanel();

        page.addComponent(chooseTitleLabel);
        page.addComponent(chooseContainer);

        page.addComponent(new JLabel("Files can be in Variant Call Format (*.vcf) or BGZipped\nVCF (*.vcf.gz).\n\n"));

		/* Add VCF annotation support via Jannovar. */
		final JCheckBox JannovarBox= new JCheckBox("Functionally annotate variants");
		JannovarBox.setSelected(true);
		JannovarBox.setOpaque(false);
		JannovarBox.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					useJannovar= JannovarBox.isSelected();
				}
			}
		);
		
		page.addComponent(JannovarBox);
		
		
        final JCheckBox homoRefBox = new JCheckBox("Include HomoRef variants (strongly discouraged)");
        homoRefBox.setOpaque(false);
        homoRefBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                includeHomoRef = homoRefBox.isSelected();
            }
        });
        page.addComponent(homoRefBox);
		
        setUploadRequired(true);

        return page;

    }
    private static final Dimension LOCATION_SIZE = new Dimension(150, 22);

    private AbstractWizardPage getAddTagsPage() {
        //setup page
        final DefaultWizardPage page = new DefaultWizardPage("Add Tags") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Variants can be filtered by tag value in the Filter section.");
        page.addText("Add tags for this set of variants:");

        final String[] patternExamples = {
            "<Tag Name>",
            "Sequencer",
            "Sequencer Version",
            "Variant Caller",
            "Variant Caller Version",
            "Technician"
        };

        locationField = new JComboBox(patternExamples);
        locationField.setEditable(true);

        final JPanel tagContainer = new JPanel();
        ViewUtil.applyVerticalBoxLayout(tagContainer);

        final JTextField valueField = new JTextField();


        final String startingValue = "<Value>";
        valueField.setText(startingValue);

        final JTextArea ta = new JTextArea();
        ta.setRows(10);
        ta.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        ta.setEditable(false);

        JLabel button = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (locationField.getSelectedItem().toString().isEmpty()) {
                    DialogUtils.displayError("Tag cannot be empty");
                    locationField.requestFocus();
                    return;
                } else if (locationField.getSelectedItem().toString().equals(patternExamples[0])) {
                    DialogUtils.displayError("Enter a valid tag name");
                    locationField.requestFocus();
                    return;
                }

                if (valueField.getText().toString().isEmpty()) {
                    DialogUtils.displayError("Value cannot be empty");
                    valueField.requestFocus();
                    return;
                } else if (valueField.getText().equals(startingValue)) {
                    DialogUtils.displayError("Enter a valid value");
                    valueField.requestFocus();
                    return;
                }

                VariantTag tag = new VariantTag((String) locationField.getSelectedItem(), valueField.getText());


                variantTags.add(tag);
                ta.append(tag.toString() + "\n");
                valueField.setText("");
            }
        });

        JPanel container2 = new JPanel();
        ViewUtil.clear(container2);
        ViewUtil.applyHorizontalBoxLayout(container2);
        container2.add(locationField);
        container2.add(ViewUtil.clear(new JLabel(" = ")));
        container2.add(valueField);
        container2.add(button);

        page.addComponent(container2);
        locationField.setToolTipText("Current display range");

        locationField.setPreferredSize(LOCATION_SIZE);
        locationField.setMinimumSize(LOCATION_SIZE);

        valueField.setPreferredSize(LOCATION_SIZE);
        valueField.setMinimumSize(LOCATION_SIZE);

        page.addComponent(tagContainer);

        page.addComponent(new JScrollPane(ta));

        JButton clear = new JButton("Clear");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                variantTags.clear();
                ta.setText("");
                addDefaultTags(variantTags, ta);
            }
        });

        addDefaultTags(variantTags, ta);

        page.addComponent(ViewUtil.alignRight(clear));

        return page;

    }

    private void addDefaultTags(List<VariantTag> variantTags, JTextArea ta) {

        VariantTag tag1 = new VariantTag(VariantTag.UPLOADER, LoginController.getInstance().getUserName());
        VariantTag tag2 = new VariantTag(VariantTag.UPLOAD_DATE, (new Date()).toString());
        variantTags.add(tag1);
        variantTags.add(tag2);
        ta.append(tag1.toString() + "\n");
        ta.append(tag2.toString() + "\n");
    }

    private AbstractWizardPage getNotificationsPage() {

        final CompletionWizardPage page = new CompletionWizardPage("Notifications") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("Variant import may take some time. Enter your email address to be notified when the process completes.");

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(p);
        JLabel l = new JLabel("Email: ");
        emailField = new JTextField();
        p.add(l);
        p.add(emailField);
        page.addComponent(p);

        autoPublish = new JCheckBox("Automatically publish data upon import completion");
        autoPublish.setSelected(true);
        page.addComponent(autoPublish);
        page.addText("If you choose not to automatically publish, you will be prompted to publish manually upon completion. Variant publication logs all users out.");

        return page;
    }

    private AbstractWizardPage getCompletePage() {

        final CompletionWizardPage page = new CompletionWizardPage("Complete") {
            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.FINISH);
                fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.NEXT);
            }
        };

        page.addText("You have completed the import submission process.");

        return page;
    }

    private AbstractWizardPage getQueuePage() {
        final DefaultWizardPage page = new DefaultWizardPage("Transfer, Annotate, and Publish Variants") {
            private static final double UPLOAD_FILES_PERCENT = 20.0;
            private static final double UPDATE_DATABASE_PERCENT = 80.0;
            private final JLabel progressLabel = new JLabel("You are now ready to import variants.");
            //private final JProgressBar progressBar = new JProgressBar();
            private final JButton workButton = new JButton("Import");
            //private final JButton publishButton = new JButton("Publish Variants");
            //private final JCheckBox autoPublishVariants = new JCheckBox("Automatically publish variants after import");
            //private final JLabel publishProgressLabel = new JLabel("Ready to publish variants.");
            //private final JProgressBar publishProgressBar = new JProgressBar();
            private boolean inUploading = false;

            {
                addComponent(progressLabel);
                //addComponent(progressBar);

                final JComponent j = new JLabel("<html>You may continue. The import process will continue in the<br>background and you will be notified upon completion.</html>");
                addComponent(j);
                j.setVisible(false);

                final DefaultWizardPage page = this;

                //autoPublishVariants.setOpaque(false);

                workButton.addActionListener(new ActionListener() {
                    private int notificationId;
                    private MedSavantWorker<Void> variantWorker;

                    @Override
                    public void actionPerformed(ActionEvent ae) {

                        LOG.info("Starting import worker");
                        workButton.setEnabled(false);
                        j.setVisible(true);
                        page.fireButtonEvent(ButtonEvent.HIDE_BUTTON, ButtonNames.BACK);
                        page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);


                        new ProjectWorker<Void>("Importing variants", autoPublish.isSelected(), LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID()) {
                            private int fileIndex = 0;

                            @Override
                            protected Void runInBackground() throws Exception {
                                String email = emailField.getText();
                                if (email.isEmpty()) {
                                    email = null;
                                }

                                if (uploadRequired) {
                                    setIndeterminate(false);
                                    inUploading = true;
                                    LOG.info("Creating input streams");
                                    int[] transferIDs = new int[variantFiles.length];
                                    for (File file : variantFiles) {
										/* Get annotated files from Jannovar and then delete them. */
										if (useJannovar) {
											
											if (!hasSerializedFile(UCSCserializationFileName)) {
												setStatusMessage("Downloading annotation files");
												downloadSerializedFile(jannovar.common.Constants.UCSC);
											}
											
											setStatusMessage("Functionally annotating " + file.getName());
											file= annotateVCFWithJannovar(file);
										}
										
                                        LOG.info("Created input stream for file");
                                        setStatusMessage("Uploading " + file.getName());
                                        //progressLabel.setText("Uploading " + file.getName() + " to server...");
                                        transferIDs[fileIndex] = ClientNetworkUtils.copyFileToServer(file);
										
										/* After being copied to the server, we can remove the file from the
										 * local computer if it was processed with Jannovar (and is superfluous). */
										if (useJannovar) file.delete();
										
                                        fileIndex++;
                                    }
                                    setStatusMessage("Importing variants");
                                    inUploading = false;
                                    setIndeterminate(true);
                                    manager.uploadVariants(LoginController.getInstance().getSessionID(), transferIDs, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), tagsToStringArray(variantTags), includeHomoRef, email, false);
                                    LOG.info("Import complete");
                                } else {
                                    LOG.info("Importing variants stored on server");
                                    setStatusMessage("Importing variants");
                                    manager.uploadVariants(LoginController.getInstance().getSessionID(), new File(serverPathField.getText()), ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), tagsToStringArray(variantTags), includeHomoRef, email, false);
                                    LOG.info("Done importing");
                                }
                                return null;
                            }

                            @Override
                            protected void showProgress(double fract) {
                                double prog;
                                if (uploadRequired) {
                                    if (inUploading) {
                                        // The fraction will be a percentage of the current file.  We have multiple files making up
                                        // the UPLOAD_FILES_PERCENT.
                                        prog = UPLOAD_FILES_PERCENT * (fileIndex + fract) / variantFiles.length;
                                    } else {
                                        prog = UPLOAD_FILES_PERCENT + fract * UPDATE_DATABASE_PERCENT;
                                    }
                                } else {
                                    prog = fract * 100.0;
                                }
                                setProgress(prog / 100.0);
                            }

                            @Override
                            protected ProgressStatus checkProgress() throws RemoteException {
                                ProgressStatus stat;
                                if (inUploading) {
                                    stat = MedSavantClient.NetworkManager.checkProgress(LoginController.getInstance().getSessionID(), isCancelled());
                                } else {
                                    try {
                                        stat = manager.checkProgress(LoginController.getInstance().getSessionID(), isCancelled());
                                    } catch (SessionExpiredException ex) {
                                        MedSavantExceptionHandler.handleSessionExpiredException(ex);
                                        return null;
                                    }
                                }
                                if (stat != null) {
                                    setStatusMessage(stat.message);
                                }
                                return stat;
                            }
                        }.execute();
                        toFront();

                    }//end actionPerformed
                }); //end new ActionListener(...){

                addComponent(ViewUtil.alignRight(workButton));
            }

            @Override
            public void setupWizardButtons() {
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.BACK);
                fireButtonEvent(ButtonEvent.SHOW_BUTTON, ButtonNames.NEXT);
                fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
            }
        };

        return page;
    }

    private static String[][] tagsToStringArray(List<VariantTag> variantTags) {

        String[][] result = new String[variantTags.size()][2];

        int row = 0;
        for (VariantTag t : variantTags) {
            result[row][0] = t.key;
            result[row][1] = t.value;
            row++;
        }

        return result;
    }

    private JPanel populateOnServerPanel(final DefaultWizardPage page) {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        serverPathField = new JTextField();
        ViewUtil.clear(serverPathField);
        serverPathField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent ce) {
                if (serverPathField.getText().isEmpty()) {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }
        });
        JPanel container = ViewUtil.getClearPanel();
        ViewUtil.clear(container);
        ViewUtil.applyHorizontalBoxLayout(container);

        container.add(serverPathField);

        p.add(ViewUtil.alignLeft(container));

        return p;
    }

    private JPanel populateOnMyComputerPanel(final DefaultWizardPage page) {

        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        final JTextField outputFileField = new JTextField();
        ViewUtil.clear(outputFileField);
        outputFileField.setEnabled(false);
        JButton chooseFileButton = new JButton("...");
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                variantFiles = DialogUtils.chooseFilesForOpen("Import Variants", new ExtensionsFileFilter(new String[]{"vcf", "vcf.gz"}), null);
                if (variantFiles == null || variantFiles.length == 0) {
                    page.fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.NEXT);
                } else {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
                String path = getPathString(variantFiles);
                outputFileField.setText(path);
                if (variantFiles.length > 0) {
                    page.fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);
                }
            }

            private String getPathString(File[] files) {
                if (files.length > 1) {
                    return files.length + " files";
                } else if (files.length == 1) {
                    return files[0].getAbsolutePath();
                } else {
                    return "";
                }
            }
        });
        JPanel container = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(container);

        container.add(outputFileField);
        container.add(chooseFileButton);

        p.add(ViewUtil.clear(ViewUtil.alignLeft(container)));


        return p;
    }
	
	
	/**
	 * Check if the Jannovar serialized annotation file has been downloaded.
	 */
	private boolean hasSerializedFile(String filename) {
		File jannovarDirectory= new File(DirectorySettings.getMedSavantDirectory().getPath(), "jannovar");
		if (jannovarDirectory.exists()) {
			dirPath= jannovarDirectory.getPath();
			return (new File(jannovarDirectory.getPath(), filename)).exists();
		} else {
			return jannovarDirectory.exists();
		}
	}
	
	
	/**
	 * Download the Jannovar serialized annotation file.
	 */
	private void downloadSerializedFile(int sourceDB) throws JannovarException {
		if (sourceDB == jannovar.common.Constants.UCSC) {
			File jannovarDirectory= new File(DirectorySettings.getMedSavantDirectory().getPath(), "jannovar");
			jannovarDirectory.mkdir();
			dirPath= jannovarDirectory.getPath();
			
			downloadTranscriptFiles(jannovar.common.Constants.UCSC);
			inputTranscriptModelDataFromUCSCFiles();
			serializeUCSCdata();
		} else {
			throw new JannovarException("VCFAnnotationWizard: Currently unsupported DB specified");
		} 
	}
	
	
	/**
     * This function creates a
     * {@link TranscriptDataDownloader} object in order to
     * download the required transcript data files. If the user has set the proxy and
     * proxy port via the command line, we use these to download the files.
     */
    public void downloadTranscriptFiles(int source) {
		TranscriptDataDownloader downloader = null;
		try {
			downloader = new TranscriptDataDownloader(dirPath);
			downloader.downloadTranscriptFiles(source);
		} catch (FileDownloadException e) {
			System.err.println(e);
			System.exit(1);
		}
    }

	
	 /**
     * Input the four UCSC files for the KnownGene data.
     */
    private void inputTranscriptModelDataFromUCSCFiles() {
		UCSCKGParser parser = new UCSCKGParser(dirPath);
		try{
			parser.parseUCSCFiles();
		} catch (Exception e) {
			System.out.println("[Jannovar] Unable to input data from the UCSC files");
			e.printStackTrace();
			System.exit(1);
		}
		transcriptModelList = parser.getKnownGeneList();
    }
	
	
	/**
     * Inputs the KnownGenes data from UCSC files, convert the
     * resulting {@link jannovar.reference.TranscriptModel TranscriptModel}
     * objects to {@link jannovar.interval.Interval Interval} objects, and
     * store these in a serialized file.
     */
    public void serializeUCSCdata() throws JannovarException {
		SerializationManager manager = new SerializationManager();
		System.out.println("[Jannovar] Serializing known gene data as " + UCSCserializationFileName);
		manager.serializeKnownGeneList(dirPath + File.separator + UCSCserializationFileName, transcriptModelList);
    }
	
	
	/**
	 * Uses Jannovar to create a new VCF file and sends that file to server.
	 * The Jannovar VCF file is subsequently removed (treated as temporary data)
	 * 
	 * Code modified from Jannovar class.
	 */
	private File annotateVCFWithJannovar(File sourceVCF) throws JannovarException {		
		chromosomeMap= Chromosome.constructChromosomeMapWithIntervalTree(
				sManager.deserializeKnownGeneList(dirPath + File.separator + UCSCserializationFileName));
		
		/* Annotated VCF name as determined by Jannovar. */
		String outname= sourceVCF.getAbsolutePath(); 
		int i = outname.lastIndexOf("vcf");
		if (i<0) {
			i = outname.lastIndexOf("VCF");
		}
		if (i<0) {
			outname = outname + ".jv.vcf";
		} else {
			outname = outname.substring(0,i) + "jv.vcf";
		}
		
		VCFReader parser= new VCFReader();
		VCFLine.setStoreVCFLines();
		try{
			parser.parseFile(sourceVCF.getAbsolutePath());
		} catch (VCFParseException e) {
			System.err.println("[Jannovar] Unable to parse VCF file");
			System.err.println(e.toString());
			System.exit(1);
		}
		
		ArrayList<VCFLine> lineList = parser.getVCFLineList();
		
		try {
			FileWriter fstream = new FileWriter(outname);
			BufferedWriter out = new BufferedWriter(fstream);
			
			/** Write the header of the new VCF file. */
			ArrayList<String> lst = parser.getAnnotatedVCFHeader();
			for (String s: lst) {
				out.write(s + "\n");
			}
			
			/** Now write each of the variants. */
			for (VCFLine  line : lineList) {
				Variant v = parser.VCFline2Variant(line);
				try{
					annotateVCFLine(line,v,out);
				} catch (AnnotationException e) {
					System.out.println("[Jannovar] Warning: Annotation error: " + e.toString());
				} catch (JannovarException e) {
					System.out.println("[Jannovar] Error processing VCF file");
					e.printStackTrace();
				}
			}
			
			out.close();
			
		} catch (IOException e){
			System.out.println("[Jannovar] Error writing annotated VCF file");
			System.out.println("[Jannovar] " + e.toString());
			System.exit(1);
		}
		
		System.out.println("[Jannovar] Wrote annotated VCF file to \"" + outname + "\"");
		
		return new File(outname);
	}
	
	/**
     * Annotate a single line of a VCF file, and output the line together with the new
     * INFO fields representing the annotations.
	 * 
	 * Code modified from Jannovar class.
	 * 
     * @param line an object representing the original VCF line 
     * @param v the Variant object that was parsed from the line
     * @param out A file handle to write to.
     */
    private void annotateVCFLine(VCFLine line, Variant v, Writer out) throws IOException, AnnotationException, JannovarException {
		byte chr =  v.getChromosomeAsByte();
		int pos = v.get_position();
		String ref = v.get_ref();
		String alt = v.get_alt();
		Chromosome c = chromosomeMap.get(chr);
		if (c==null) {
			String e = String.format("[Jannovar] Could not identify chromosome \"%d\"", chr );
			throw new AnnotationException(e);	
		} 
		AnnotationList anno = c.getAnnotationList(pos,ref,alt);
		if (anno==null) {
			String e = String.format("[Jannovar] No annotations found for variant %s", v.toString());
			throw new AnnotationException(e);	
		}
		String annotation = anno.getSingleTranscriptAnnotation();
		String effect = anno.getVariantType().toString();
		String A[] = line.getOriginalVCFLine().split("\t");
		for (int i=0;i<7;++i)
			out.write(A[i] + "\t");
		/* Now add the stuff to the INFO line */
		String INFO = String.format("EFFECT=%s;HGVS=%s;%s",effect,annotation,A[7]);
		out.write(INFO + "\t");
		for (int i=8;i<A.length;++i)
			 out.write(A[i] + "\t");
		out.write("\n");
    }
}