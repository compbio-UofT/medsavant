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
package org.ut.biolab.medsavant.app.mendelclinic.view;

import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.app.mendelclinic.MendelClinicApp;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ExportVCF;
import org.ut.biolab.medsavant.client.view.dialog.FamilySelector;
import org.ut.biolab.medsavant.app.mendelclinic.controller.MendelWorker;
import org.ut.biolab.medsavant.app.mendelclinic.model.Locks;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.app.AppDirectory;
import org.ut.biolab.medsavant.client.view.app.builtin.task.BackgroundTaskWorker;
import org.ut.biolab.medsavant.client.view.app.builtin.task.TaskWorker;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.notify.Notification;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.db.TableSchema;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.model.GeneralLog;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume
 */
public class OptionView {

    private List<IncludeExcludeStep> steps;
    private InheritanceStep inheritanceStep;
    private File lastTDFFile;
    private boolean filtersChangedSinceLastDump;
    private final ZygosityStep zygosityStep;

    protected class ZygosityStepViewGenerator {

        private JPanel inheritanceModelView;

        public ZygosityStepViewGenerator() {
            setupView();
        }

        public JPanel getView() {
            return inheritanceModelView;
        }

        private void setupView() {

            inheritanceModelView = ViewUtil.getWhiteLineBorderedPanel();

            inheritanceModelView.add(new JLabel("and has zygosity"));
            final JComboBox b = new JComboBox();

            b.addItem("Any");
            for (Zygosity z : Zygosity.values()) {
                b.addItem(z);
            }

            inheritanceModelView.add(b);

            b.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent ie) {
                    int index = b.getSelectedIndex();
                    if (index == 0) {
                        zygosityStep.setZygosity(null);
                    } else {

                        zygosityStep.setZygosity((Zygosity) b.getSelectedItem());
                    }
                }

            });
        }
    }

    protected class InheritanceStepViewGenerator {

        private JPanel inheritanceModelView;
        private FamilySelector familyDialog;
        private final static String ORIGINAL_CHOOSE_TEXT = "choose families";

        public InheritanceStepViewGenerator() {
            setupView();
        }

        public JPanel getView() {
            return inheritanceModelView;
        }

        private void setupView() {

            inheritanceModelView = ViewUtil.getWhiteLineBorderedPanel();
            inheritanceModelView.add(new JLabel("and follows"));

            familyDialog = new FamilySelector();

            JComboBox b = new JComboBox();

            for (InheritanceModel m : InheritanceModel.values()) {
                b.addItem(m);
            }

            inheritanceModelView.add(b);

            final JButton configure = new JButton(ORIGINAL_CHOOSE_TEXT);
            configure.setVisible(false);
            configure.setFocusable(false);
            final JLabel in = new JLabel("in");
            in.setVisible(false);

            b.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    boolean b = e.getItem() != InheritanceModel.ANY;
                    in.setVisible(b);
                    configure.setVisible(b);

                    InheritanceModel s = (InheritanceModel) e.getItem();
                    inheritanceStep.setInheritanceModel(s);

                }
            });

            inheritanceModelView.add(new JLabel("inheritance model"));
            inheritanceModelView.add(in);
            inheritanceModelView.add(configure);

            configure.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    familyDialog.setVisible(true);
                    Set<String> selectedFamilies = familyDialog.getSelectedFamilies();
                    int n = selectedFamilies.size();
                    if (n > 0) {
                        configure.setText(n + " " + MiscUtils.pluralize(n, "family", "families"));
                    } else {
                        configure.setText(ORIGINAL_CHOOSE_TEXT);
                    }
                    inheritanceStep.setFamilies(selectedFamilies);
                }
            });

        }
    }

    public static class IncludeExcludeStep {

        private JPanel view;
        private JLabel datasetLabel;
        private List<IncludeExcludeCriteria> criteria;
        private JButton variantNavigatorButton;

        public IncludeExcludeStep() {
            setupView();
        }

        public JPanel getView() {
            return view;
        }

        public void setDatasetName(String name) {
            datasetLabel.setText(name);
            variantNavigatorButton.setVisible(false);
        }

        public List<IncludeExcludeCriteria> getCriteria() {
            return criteria;
        }

        private void setupView() {

            view = ViewUtil.getWhiteLineBorderedPanel();
            view.setLayout(new MigLayout("wrap, center"));

            JPanel required = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(required);

            required.add(new JLabel("Select variants from "));
            
            variantNavigatorButton = ViewUtil.getSoftButton("Variant Navigator");
            required.add(variantNavigatorButton);
            variantNavigatorButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MedSavantFrame.getInstance().getDashboard().launchApp(AppDirectory.getVariantNavigator());
                }
                
            });
            
            datasetLabel = new JLabel("");
            datasetLabel.setFont(ViewUtil.getMediumTitleFont());

            required.add(datasetLabel);

            required.add(new JLabel(" results, where"));

            IncludeExcludeCriteria c = new IncludeExcludeCriteria();

            view.add(required, "width 100%");
            view.add(c.getView());

            criteria = new ArrayList<IncludeExcludeCriteria>();
            criteria.add(c);

            JButton addButton = ViewUtil.getSoftButton("Add criteria to this step");

            view.add(addButton,"right");

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    final IncludeExcludeCriteria c = new IncludeExcludeCriteria();
                    criteria.add(c);

                    JButton removeButton = ViewUtil.getSoftButton("Remove criteria from this step");

                    final JPanel criteriaPanel = ViewUtil.getClearPanel();
                    criteriaPanel.setLayout(new MigLayout("fillx, insets 0"));
                    
                    criteriaPanel.add(new JLabel(" and "));
                    criteriaPanel.add(c.getView());
                    criteriaPanel.add(removeButton,"pushx 1.0, gpx 100, right");

                    view.add(criteriaPanel, "width 100%", view.getComponentCount() - 1);

                    removeButton.addMouseListener(new MouseListener() {
                        public void actionPerformed(ActionEvent ae) {
                            criteria.remove(c);
                            view.remove(criteriaPanel);
                            view.updateUI();
                            view.invalidate();
                        }

                        @Override
                        public void mouseClicked(MouseEvent me) {
                        }

                        @Override
                        public void mousePressed(MouseEvent me) {
                        }

                        @Override
                        public void mouseReleased(MouseEvent me) {
                            actionPerformed(null);
                        }

                        @Override
                        public void mouseEntered(MouseEvent me) {
                        }

                        @Override
                        public void mouseExited(MouseEvent me) {
                        }
                    });

                    view.updateUI();
                    view.invalidate();
                }
            });
        }
    }

    public static class ZygosityStep {

        private Zygosity zygosity;

        public ZygosityStep() {
        }

        public ZygosityStep(Zygosity z) {
            this.zygosity = z;
        }

        public void setZygosity(Zygosity z) {
            this.zygosity = z;
        }

        public boolean isSet() {
            return zygosity != null;
        }

        public Zygosity getZygosity() {
            return this.zygosity;
        }

    }

    public static class InheritanceStep {

        public enum InheritanceModel {

            ANY {
                        @Override
                        public String toString() {
                            return "Any";
                        }
                    }, AUTOSOMAL_DOMINANT {
                        @Override
                        public String toString() {
                            return "Autosomal Dominant";
                        }
                    }, AUTOSOMAL_RECESSIVE {
                        @Override
                        public String toString() {
                            return "Autosomal Recessive";
                        }
                    }, X_LINKED_RECESSIVE {
                        @Override
                        public String toString() {
                            return "X-Linked Recessive";
                        }
                    }, X_LINKED_DOMINANT {
                        @Override
                        public String toString() {
                            return "X-Linked Dominant";
                        }
                    }, COMPOUND_HETEROZYGOTE {
                        @Override
                        public String toString() {
                            return "Compound Heterozygote";
                        }
                    }, DE_NOVO {
                        @Override
                        public String toString() {
                            return "De Novo";
                        }
                    }
        };
        private Set<String> families;
        private InheritanceModel inheritanceModel;

        public InheritanceStep() {
            families = new HashSet<String>();
            inheritanceModel = InheritanceModel.ANY;
        }

        public Set<String> getFamilies() {
            return families;
        }

        public void setFamilies(Set<String> families) {
            this.families = families;
        }

        public InheritanceModel getInheritanceModel() {
            return inheritanceModel;
        }

        public void setInheritanceModel(InheritanceModel inheritanceModel) {
            this.inheritanceModel = inheritanceModel;
        }
    }

    private enum AggregateBy {

        Position, Variant, Gene
    }
    private AggregateBy aggregateBy = AggregateBy.Variant;
    private JPanel view;

    public OptionView() {

        inheritanceStep = new InheritanceStep();
        zygosityStep = new ZygosityStep();

        setupView();

        FilterController.getInstance().addListener(new Listener<FilterEvent>() {
            @Override
            public void handleEvent(FilterEvent event) {
                filtersChangedSinceLastDump = true;
            }
        });
    }

    public JPanel getView() {
        return view;
    }

    public static class IncludeExcludeCriteria {

        private static Map<String, Set<String>> groupNameToIndividualsMap = new HashMap<String, Set<String>>();
        private JButton chooseIndividualsButton;
        private String individualsGroupName;
        private final static String ORIGINAL_CHOOSE_TEXT = "choose individuals";
        private IndividualSelector customSelector;

        @Override
        public String toString() {
            return "IncludeExcludeCriteria{" + "freqAmount=" + freqAmount + ", aggregationType=" + aggregationType + ", frequencyType=" + frequencyType + ", frequencyCount=" + frequencyCount + '}';
        }
        private int freqAmount;
        private AggregationType aggregationType;
        private FrequencyType frequencyType;
        private FrequencyCount frequencyCount;
        private boolean isNot = false;

        public Set<String> getDNAIDs() {
            if (isNot) {
                return customSelector.getInverseOfHospitalIDsOfSelectedIndividuals();
            } else {
                return customSelector.getHospitalIDsOfSelectedIndividuals();
            }
        }

        public enum AggregationType {

            Variant, Gene
        };

        public AggregationType getAggregationType() {
            return aggregationType;
        }

        public enum FrequencyType {

            ALL, NO, AT_LEAST, AT_MOST
        };

        public FrequencyType getFrequencyType() {
            return frequencyType;
        }

        public enum FrequencyCount {

            Count, Percent
        };

        public FrequencyCount getFequencyCount() {
            return frequencyCount;
        }

        public int getFreqAmount() {
            return freqAmount;
        }

        /*public Cohort getCohort() {
         Object o = cb.getSelectedItem();
         if (o instanceof Cohort) {
         return (Cohort) o;
         }
         return null;
         }*/
        private JPanel view;
        private JComboBox freqTypeChoice;
        private JTextField freqAmountField;
        private JComboBox freqCountChoice;
        private JComboBox typeBox;
        private static Cohort[] cohorts;
        private JComboBox cb;

        public IncludeExcludeCriteria() {
            setupView();
        }

        public JPanel getView() {
            return view;
        }

        private void setupView() {
            view = ViewUtil.getClearPanel();

            typeBox = new JComboBox();
            typeBox.addItem("variant exists");
            typeBox.addItem("gene has variant");
            //typeBox.addItem("position has variant");

            typeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (typeBox.getSelectedIndex()) {
                        case 0:
                            aggregationType = AggregationType.Variant;
                            break;
                        case 1:
                            aggregationType = AggregationType.Gene;
                            break;
                    }
                }
            });

            typeBox.setSelectedIndex(0);

            view.add(typeBox);
            view.add(new JLabel("in"));

            freqTypeChoice = new JComboBox();
            freqTypeChoice.addItem("all");
            freqTypeChoice.addItem("none");
            freqTypeChoice.addItem("at most");
            freqTypeChoice.addItem("at least");

            freqTypeChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (freqTypeChoice.getSelectedIndex()) {
                        case 0:
                            frequencyType = FrequencyType.ALL;
                            break;
                        case 1:
                            frequencyType = FrequencyType.NO;
                            break;
                        case 2:
                            frequencyType = FrequencyType.AT_MOST;
                            break;
                        case 3:
                            frequencyType = FrequencyType.AT_LEAST;
                            break;
                    }
                }
            });

            freqTypeChoice.setSelectedIndex(0);

            view.add(freqTypeChoice);

            final JPanel freqPanel = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(freqPanel);

            freqAmountField = new JTextField();
            freqAmountField.setColumns(3);
            freqPanel.add(freqAmountField);

            freqAmountField.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent ce) {
                    String freq = freqAmountField.getText();
                    int freqNum = 0;
                    try {
                        freqNum = Integer.parseInt(freq);
                    } catch (Exception e) {
                    }
                    freqAmount = freqNum;
                }
            });

            freqCountChoice = new JComboBox();
            freqCountChoice.addItem("");
            freqCountChoice.addItem("%");
            freqPanel.add(freqCountChoice);

            freqCountChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    switch (freqCountChoice.getSelectedIndex()) {
                        case 0:
                            frequencyCount = FrequencyCount.Count;
                            break;
                        case 1:
                            frequencyCount = FrequencyCount.Percent;
                            break;
                    }
                }
            });

            freqCountChoice.setSelectedIndex(0);

            view.add(freqPanel);
            freqPanel.setVisible(false);

            freqTypeChoice.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String selected = (String) freqTypeChoice.getSelectedItem();
                    freqPanel.setVisible(selected.contains("at"));
                }
            });

            view.add(new JLabel("of individuals"));

            final JComboBox notIndividuals = new JComboBox();
            notIndividuals.setFocusable(false);
            notIndividuals.addItem("in");
            notIndividuals.addItem("not in");
            notIndividuals.setSelectedIndex(0);
            view.add(notIndividuals);

            notIndividuals.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent ie) {
                    isNot = notIndividuals.getSelectedIndex() == 1;
                }

            });

            individualsGroupName = "";

            chooseIndividualsButton = new JButton(ORIGINAL_CHOOSE_TEXT);
            chooseIndividualsButton.setFocusable(false);
            customSelector = new IndividualSelector();

            chooseIndividualsButton.addActionListener(new ActionListener() {
                private Set<String> selectedIndividuals;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    customSelector.refresh();
                    JPopupMenu m = new JPopupMenu();

                    // edit existing selections
                    boolean editAdded = false;
                    if (customSelector.hasMadeSelection()) {
                        JMenu edit = new JMenu(selectedIndividuals.size() + " individual(s)");

                        int counter = 0;

                        // name existing selections
                        if (customSelector.getHospitalIDsOfSelectedIndividuals().size() > 0 && (individualsGroupName == null || individualsGroupName.isEmpty())) {

                            JMenuItem name = new JMenuItem("Name this group");
                            name.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    renameGroup();
                                }
                            });
                            edit.add(name);
                            edit.add(new JSeparator());
                            counter++;
                        } else {
                        }

                        for (String o : selectedIndividuals) {
                            JMenuItem j = new JMenuItem(o);
                            j.setEnabled(false);
                            edit.add(j);
                            if (counter++ == 10) {
                                JMenuItem k = new JMenuItem("...");
                                k.setEnabled(false);
                                edit.add(k);
                                break;
                            }
                        }

                        edit.add(new JSeparator());
                        JMenuItem doEdit = new JMenuItem("Edit selections");
                        doEdit.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                showIndividualSelector();
                            }
                        });
                        edit.add(doEdit);

                        JMenuItem resetSelf = new JMenuItem("Reset selections");
                        resetSelf.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {

                                if (!individualsGroupName.isEmpty()) {
                                    int result = DialogUtils.askYesNo("Reset selections", "<html>Resetting will remove this item from the group.<br>Choose 'Edit' to make changes to the whole group.<br><br>Do you want to continue?</html>");
                                    if (result != DialogUtils.YES) {
                                        return;
                                    }
                                }

                                chooseIndividualsButton.setText(ORIGINAL_CHOOSE_TEXT);
                                individualsGroupName = "";
                                selectedIndividuals = new HashSet<String>();
                                customSelector.resetSelections();
                            }
                        });
                        edit.add(resetSelf);

                        /*JMenuItem saveToCohorts = new JMenuItem("Save to cohorts");
                         saveToCohorts.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent ae) {
                         DialogUtils.displayMessage("TODO");
                         }
                         });
                         edit.add(saveToCohorts);*/
                        m.add(edit);
                        editAdded = true;
                    }

                    // set group
                    if (groupNameToIndividualsMap.keySet().size() > 0) {

                        String s = "Set group to";

                        JMenu group = new JMenu(s);
                        int counter = 0;
                        for (final String key : groupNameToIndividualsMap.keySet()) {
                            if (key.equals(individualsGroupName)) {
                                continue;
                            }
                            counter++;
                            JMenuItem i = new JMenuItem(key);
                            i.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    applyGroupName(key);
                                }
                            });
                            group.add(i);
                        }
                        if (counter > 0) {
                            m.add(group);
                        } else {
                        }
                    } else {
                    }

                    if (m.getSubElements().length > 0) {

                        if (!editAdded) {
                            JMenuItem select = new JMenuItem("Make selections");
                            select.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    showIndividualSelector();
                                }
                            });
                            m.add(select, 0);

                        } else {
                        }

                        m.show(chooseIndividualsButton, 0, 0);
                    } else {
                        showIndividualSelector();
                    }

                }

                private void renameGroup() {
                    renameGroup("");
                }

                private void renameGroup(String def) {
                    String name = DialogUtils.displayInputMessage("", "Name for this group", def);
                    if (name == null) {
                        return;
                    }
                    if (groupNameToIndividualsMap.containsKey(name)) {
                        int result = DialogUtils.askYesNo("Confirm rename", "A group with this name already exists. Overwrite?");
                        if (result != DialogUtils.YES) {
                            renameGroup(name);
                            return;
                        }
                    }
                    setNameForGroup(name, selectedIndividuals);
                }

                private void setNameForGroup(String groupName, Set<String> individuals) {
                    changeButtonToName(groupName);
                    groupNameToIndividualsMap.put(groupName, selectedIndividuals);
                }

                private void applyGroupName(String groupName) {
                    if (groupNameToIndividualsMap.containsKey(groupName)) {
                        changeButtonToName(groupName);
                        selectedIndividuals = groupNameToIndividualsMap.get(groupName);
                        customSelector.setSelectedIndividuals(selectedIndividuals);
                    } else {
                        DialogUtils.displayMessage("No such group as " + groupName);
                    }
                }

                private void showIndividualSelector() {

                    if (individualsGroupName != null && groupNameToIndividualsMap.containsKey(individualsGroupName)) {
                        customSelector.setSelectedIndividuals(groupNameToIndividualsMap.get(individualsGroupName));
                    }

                    customSelector.setVisible(true);

                    selectedIndividuals = customSelector.getHospitalIDsOfSelectedIndividuals();

                    if (individualsGroupName != null && groupNameToIndividualsMap.containsKey(individualsGroupName)) {
                        groupNameToIndividualsMap.put(individualsGroupName, selectedIndividuals);
                    }

                    if (selectedIndividuals != null) {
                        if (selectedIndividuals.size() > 0) {
                            if (individualsGroupName.isEmpty()) {
                                chooseIndividualsButton.setText(selectedIndividuals.size() + " individuals ▾");
                            }
                            String s = "<html>";
                            for (String o : selectedIndividuals) {
                                s += o + "<br>";
                            }
                            s += "</html>";
                            chooseIndividualsButton.setToolTipText(s);
                        }
                    }
                }

                private void changeButtonToName(String groupName) {
                    individualsGroupName = groupName;
                    chooseIndividualsButton.setText(individualsGroupName + " ▾");
                }
            });

            view.add(chooseIndividualsButton);
        }
    }

    void viewDidLoad() {
    }

    int jobNumber = 0;

    private void setupView() {
        view = ViewUtil.getClearPanel();
        view.setLayout(new MigLayout("wrap 2, fillx"));

        IncludeExcludeStep s = new IncludeExcludeStep();
        view.add(s.getView(),"skip 1, width 100%");

        steps = new ArrayList<IncludeExcludeStep>();
        steps.add(s);

        JButton addButton = ViewUtil.getSoftButton("Add step");

        view.add(addButton,"skip 1, center, wrap");

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                final IncludeExcludeStep c = new IncludeExcludeStep();
                c.setDatasetName("previous step");

                steps.add(c);

                final JButton removeButton = ViewUtil.getSoftButton("Remove step");

                int indexFromBack = 4;
                view.add(removeButton, view.getComponentCount() - indexFromBack);
                view.add(c.getView(), "width 100%", view.getComponentCount() - indexFromBack);

                //int indexFromBack = 4;
                //view.add(criteriaPanel, view.getComponentCount() - indexFromBack);

                removeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        steps.remove(c);
                        view.remove(removeButton);
                        view.remove(c.getView());
                        view.updateUI();
                        view.invalidate();
                    }
                });

                view.updateUI();
                view.invalidate();
            }
        });


        ZygosityStepViewGenerator zygosity = new ZygosityStepViewGenerator();

        view.add(zygosity.getView(),"skip 1, width 100%");

        InheritanceStepViewGenerator inheritance = new InheritanceStepViewGenerator();

        view.add(inheritance.getView(),"skip 1, width 100%");

        JButton runButton = new JButton("Run");

        runButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ae) {
                
                final Notification notification = new Notification();

                new BackgroundTaskWorker<Boolean>("Mendel") {

                    MedSavantWorker currentWorker;
                    Locks.DialogLock dialogLock;
                    private final int WARN_IF_VARIANTS_EXCEED = 1000000;

                    @Override
                    protected Boolean doInBackground() throws Exception {

                        Set<String> dnaIDs = getRelevantDNAIDs();
                        
                        //System.out.println("Relevant DNA IDs");
                        //for (String d : dnaIDs) {
                        //    System.out.println("\t" + d);
                        //}
                        
                        if (dnaIDs.isEmpty()) {
                            DialogUtils.displayMessage("Please select individuals to run the analysis on");
                            return false;
                        }
                        
                        Condition[][] conditions = getRelevantConditions(dnaIDs);

                        int numVariants = MedSavantClient.VariantManager.getFilteredVariantCount(
                                LoginController.getSessionID(),
                                ProjectController.getInstance().getCurrentProjectID(),
                                ReferenceController.getInstance().getCurrentReferenceID(),
                                conditions);

                        if (numVariants > WARN_IF_VARIANTS_EXCEED) {
                            int result = DialogUtils.askYesNo("Confirm", String.format("<html>This analysis will download %s genetic variants<br/>to your computer, which may take some time.<br/><br/>For quicker results, add more filters in the Variant Navigator.<br/><br/>Proceed anyway?</html>", MiscUtils.numToString(numVariants)));
                            if (result == DialogUtils.NO) {
                                return false;
                            }
                        }

                        System.out.println("Starting Mendel");

                        notification.setName("Mendel");
                        notification.setHideDoesClose(false);
                        notification.setIsIndeterminateProgress(true);
                        notification.setIcon(MendelClinicApp.icon);

                        this.addListener(new Listener<TaskWorker>() {

                            @Override
                            public void handleEvent(TaskWorker event) {
                                if (!event.getLog().isEmpty()) {
                                    GeneralLog log = event.getLog().get(event.getLog().size() - 1);
                                    notification.setDescription(log.getDescription());
                                }
                            }

                        });

                        MedSavantFrame.getInstance().showNotification(notification);

                        final Locks.FileResultLock fileLock = new Locks.FileResultLock();
                        dialogLock = new Locks.DialogLock();

                        // File retriever
                        currentWorker = getRetrieverWorker(this, fileLock, notification);
                        currentWorker.execute();
                        try {
                            synchronized (fileLock) {
                                fileLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        }

                        System.out.println("Running algorithm on file " + fileLock.getFile().getAbsolutePath());

                        // Algorithm Runner
                        currentWorker = getAlgorithmWorker(this, fileLock.getFile(), steps, zygosityStep, inheritanceStep, this, dialogLock);
                        currentWorker.execute();

                        try {
                            synchronized (dialogLock) {
                                dialogLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        } finally {
                            //NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).markNotificationAsComplete(notificationID);
                        }

                        return true;
                    }

                    @Override
                    protected void showSuccess(Boolean t) {

                        if (t) {

                            notification.setHideDoesClose(true);
                            notification.unhide();
                            notification.setAction("Results", new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                                    dialogLock.getResultsDialog().setSize(new Dimension((int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9)));
                                    dialogLock.getResultsDialog().setTitle("Mendel Results");
                                    dialogLock.getResultsDialog().setVisible(true);
                                }
                            });

                            this.setStatus(TaskWorker.TaskStatus.FINISHED);

                            int result = DialogUtils.askYesNo("Mendel Complete", "<html>Would you like to view the<br/>results now?</html>");
                            if (result == DialogUtils.YES) {
                                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                                dialogLock.getResultsDialog().setSize(new Dimension((int) (screenSize.width * 0.9), (int) (screenSize.height * 0.9)));
                                dialogLock.getResultsDialog().setTitle("Mendel Results");
                                dialogLock.getResultsDialog().setVisible(true);
                            }
                        }
                    }
                }.start();
            }

            private MendelWorker getAlgorithmWorker(BackgroundTaskWorker tw, File file, List<IncludeExcludeStep> steps, ZygosityStep zygosityStep, InheritanceStep model, BackgroundTaskWorker j, Locks.DialogLock genericLock) {
                MendelWorker w = new MendelWorker(tw, steps, zygosityStep, model, file);
                w.setCompletionLock(genericLock);
                return w;
            }

            private Set<String> getRelevantDNAIDs() {
                Set<String> dnaIds = new HashSet<String>();

                for (IncludeExcludeStep step : steps) {
                    for (IncludeExcludeCriteria criteria : step.getCriteria()) {
                        dnaIds.addAll(criteria.getDNAIDs());
                    }
                }

                for (String family : inheritanceStep.getFamilies()) {
                    try {
                        Map<String, String> patientToDNAID = MedSavantClient.PatientManager.getDNAIDsForFamily(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), family);
                        for (String pID : patientToDNAID.keySet()) {
                            dnaIds.add(patientToDNAID.get(pID));
                        }
                    } catch (SessionExpiredException ex) {
                        MedSavantExceptionHandler.handleSessionExpiredException(ex);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                return dnaIds;
            }

            private Condition[][] getRelevantConditions(Set<String> dnaIds) throws InterruptedException, SQLException, RemoteException {

                Condition[] dnaIDConditions = new Condition[dnaIds.size()];

                // add conditions for dnaIDs ... (dna_id = 'A' OR ...) ...
                TableSchema table = ProjectController.getInstance().getCurrentVariantTableSchema();
                int counter = 0;
                for (String dnaID : dnaIds) {
                    dnaIDConditions[counter++] = BinaryCondition.equalTo(table.getDBColumn(BasicVariantColumns.DNA_ID.getColumnName()), dnaID);
                    //System.out.println(dnaIDConditions[counter-1]);
                }
                
                // and within rows, or between rows
                Condition[][] existingConditions = FilterController.getInstance().getAllFilterConditions();
                Condition[] flatRows = new Condition[existingConditions.length];
                counter = 0;
                for (Condition[] existingConditionRow : existingConditions) {
                    flatRows[counter++] = ComboCondition.and(existingConditionRow);
                }
                Condition flatMatrix = ComboCondition.or(flatRows);

                Condition c = ComboCondition.and(flatMatrix,ComboCondition.or(dnaIDConditions));
                return new Condition[][] { new Condition[] { c } };
            }

            private MedSavantWorker getRetrieverWorker(final BackgroundTaskWorker m, final Locks.FileResultLock fileLock, final Notification n) {
                return new MedSavantWorker<File>("Variant Download") {

                    @Override
                    protected void showSuccess(File result) {
                        n.setDescription("Variant Download Complete");
                        m.addLog("Variant Download Complete");
                        synchronized (fileLock) {
                            fileLock.setFile(result);
                            fileLock.notify();
                        }
                    }

                    @Override
                    protected File doInBackground() throws Exception {

                        n.setDescription("Retrieving Variants...");
                        m.addLog("Retrieving Variants...");

                        String session = LoginController.getSessionID();
                        int refID = ReferenceController.getInstance().getCurrentReferenceID();
                        int projectID = ProjectController.getInstance().getCurrentProjectID();
                        int updateID = MedSavantClient.ProjectManager.getNewestUpdateID(
                                session,
                                projectID,
                                refID,
                                true);

                        File outdir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());

                        File tdfFile;
                        if (filtersChangedSinceLastDump || (lastTDFFile == null)) {

                            if (lastTDFFile == null) {
                                System.out.println("No previous dump");
                            } else {
                                System.out.println("Filters changed since last dump");
                            }

                            File zipFile = new File(outdir, System.currentTimeMillis() + "-variantdump.tdf.zip");

                            System.out.println("Exporting to " + zipFile.getAbsolutePath());

                            Set<String> dnaIDs = getRelevantDNAIDs();
                            Condition[][] conditions = getRelevantConditions(dnaIDs);

                            tdfFile = ExportVCF.exportTDF(zipFile, null, conditions);
                            System.out.println("Finished export");

                            // remove the old tdf file, it's out of date
                            if (lastTDFFile != null) {
                                lastTDFFile.delete();
                            }

                            // replace with the new one
                            lastTDFFile = tdfFile;
                        } else {
                            System.out.println("Reusing previous dump");
                            tdfFile = lastTDFFile;
                        }

                        System.out.println("Imported from server " + tdfFile.getAbsolutePath());

                        filtersChangedSinceLastDump = false;

                        // line in format: "8243_0000","chr17","6115", "6115", "G","C","SNP","Hetero"
                        int[] columnsToKeep = new int[]{3, 4, 5, 6, 8, 9, 13, 14};
                        n.setDescription("Stripping file");
                        m.addLog("Stripping file");

                        File strippedFile = awkColumnsFromFile(tdfFile, columnsToKeep);
                        System.out.println("Stripped file is " + strippedFile.getAbsolutePath());

                        return strippedFile;
                    }

                    private File awkColumnsFromFile(File inFile, int[] columnsToKeep) throws IOException {

                        File outfile = new File(inFile.getAbsolutePath() + ".awk");

                        CSVReader r = new CSVReader(new FileReader(inFile), '\t', '\"');
                        CSVWriter w = new CSVWriter(new FileWriter(outfile), '\t', '\"');

                        String[] line = new String[0];
                        String[] subsetLines = new String[columnsToKeep.length];
                        while ((line = readNext(r)) != null) {
                            clear(subsetLines);

                            for (int i = 0; i < columnsToKeep.length; i++) {
                                subsetLines[i] = line[columnsToKeep[i]];
                            }

                            w.writeNext(subsetLines);
                        }

                        r.close();
                        w.close();

                        return outfile;
                    }

                    private String[] readNext(CSVReader recordReader) throws IOException {
                        String[] line = recordReader.readNext();
                        if (line == null) {
                            return null;
                        } else {
                            line[line.length - 1] = removeNewLinesAndCarriageReturns(line[line.length - 1]);
                        }

                        return line;
                    }

                    private String removeNewLinesAndCarriageReturns(String next) {

                        next = next.replaceAll("\n", "");
                        next = next.replaceAll("\r", "");

                        return next;
                    }

                    private void clear(String[] subsetLines) {
                        for (int i = 0; i < subsetLines.length; i++) {
                            subsetLines[i] = null;
                        }
                    }

                };
            }
        });

        view.add(runButton,"skip 1, right");

    }
}
