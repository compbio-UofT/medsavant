package org.ut.biolab.medsavant.client.view.genetics.family;

import org.ut.biolab.medsavant.client.view.dialog.IndividualSelector;
import org.ut.biolab.medsavant.client.view.Notification;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.filter.FilterController;
import org.ut.biolab.medsavant.client.filter.FilterEvent;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ExportVCF;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.NotificationsPanel;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.dialog.FamilySelector;
import org.ut.biolab.medsavant.client.view.genetics.family.FamilyMattersOptionView.InheritanceStep.InheritanceModel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.shared.util.MiscUtils;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume
 */
public class FamilyMattersOptionView {

    private List<IncludeExcludeStep> steps;
    private InheritanceStep inheritanceStep;
    private File lastTDFFile;
    private boolean filtersChangedSinceLastDump;
    private final ZygosityStep zygosityStep;

    protected class ZygosityStepViewGenerator {

        private RoundedPanel inheritanceModelView;
         public ZygosityStepViewGenerator() {
            setupView();
        }

        public JPanel getView() {
            return inheritanceModelView;
        }

        private void setupView() {

            inheritanceModelView = new RoundedPanel(10);

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

                        zygosityStep.setZygosity((Zygosity)b.getSelectedItem());
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

            inheritanceModelView = new RoundedPanel(10);
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

    protected static class IncludeExcludeStep {

        private JPanel view;
        private JLabel datasetLabel;
        private List<IncludeExcludeCriteria> criteria;

        public IncludeExcludeStep() {
            setupView();
        }

        public JPanel getView() {
            return view;
        }

        public void setDatasetName(String name) {
            datasetLabel.setText(name);
        }

        public List<IncludeExcludeCriteria> getCriteria() {
            return criteria;
        }

        private void setupView() {

            view = new RoundedPanel(10);

            ViewUtil.applyVerticalBoxLayout(view);
            view.setBorder(ViewUtil.getMediumBorder());

            JPanel required = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(required);

            required.add(new JLabel("Select variants from "));

            datasetLabel = new JLabel("filtered variants");
            datasetLabel.setFont(ViewUtil.getMediumTitleFont());

            required.add(datasetLabel);

            required.add(new JLabel(" where"));

            IncludeExcludeCriteria c = new IncludeExcludeCriteria();

            view.add(required);
            view.add(c.getView());

            criteria = new ArrayList<IncludeExcludeCriteria>();
            criteria.add(c);

            JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
            addButton.setToolTipText("Add criteria to this step");

            view.add(ViewUtil.alignRight(addButton));

            addButton.addMouseListener(new MouseListener() {
                public void actionPerformed(ActionEvent ae) {
                    final IncludeExcludeCriteria c = new IncludeExcludeCriteria();
                    criteria.add(c);

                    JLabel removeButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                    removeButton.setToolTipText("Remove criteria from this step");

                    final JPanel criteriaPanel = new JPanel();
                    ViewUtil.applyHorizontalBoxLayout(criteriaPanel);

                    criteriaPanel.add(new JLabel(" and "));
                    criteriaPanel.add(c.getView());
                    criteriaPanel.add(removeButton);

                    view.add(criteriaPanel, view.getComponentCount() - 1);

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

        Zygosity getZygosity() {
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

    public FamilyMattersOptionView() {

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

    protected static class IncludeExcludeCriteria {

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

            /*cb = new JComboBox();
             cb.setName(COHORT_COMBO);
             updateCohorts(cb);
             view.add(cb);

             view.add(new JLabel("cohort"));*/


            individualsGroupName = "";

            chooseIndividualsButton = new JButton(ORIGINAL_CHOOSE_TEXT);
            chooseIndividualsButton.setFocusable(false);
            customSelector = new IndividualSelector();

            chooseIndividualsButton.addActionListener(new ActionListener() {
                private Set<String> selectedIndividuals;

                @Override
                public void actionPerformed(ActionEvent ae) {

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

                        ActionListener removeFromGroup = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }
                        };

                        JMenuItem saveToCohorts = new JMenuItem("Save to cohorts");
                        saveToCohorts.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                DialogUtils.displayMessage("TODO");
                            }
                        });
                        edit.add(saveToCohorts);

                        m.add(edit);
                        editAdded = true;
                    } else {
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

        private void updateCohortComboBoxes() {
            updateCohorts(cb);
        }
    }
    private static String COHORT_COMBO = "COHORT_COMBO";

    void viewDidLoad() {
        for (IncludeExcludeStep s : steps) {
            for (IncludeExcludeCriteria c : s.criteria) {
                c.updateCohortComboBoxes();
            }
        }
    }

    private static void updateCohorts(JComboBox b) {

        Object o = b.getSelectedItem();
        Cohort selected = null;
        if (o instanceof Cohort) {
            selected = (Cohort) o;
        }

        b.removeAllItems();
        Cohort[] cohorts = null;

        try {
            cohorts = MedSavantClient.CohortManager.getCohorts(LoginController.getInstance().getSessionID(), ProjectController.getInstance().getCurrentProjectID());
        } catch (Exception ex) {
            DialogUtils.displayErrorMessage("Error loading cohorts", ex);
        }

        if (cohorts == null || cohorts.length == 0) {
            b.addItem("no cohorts");
            b.setEnabled(false);

        } else {
            b.setEnabled(true);
            for (Cohort c : cohorts) {
                b.addItem(c);
                if (selected != null && c.toString().equals(selected.toString())) {
                    b.setSelectedItem(c);
                }
            }
            //if (selected != null) {
            //    b.setSelectedItem(selected);
            //}
        }

    }
    int jobNumber = 0;

    private void setupView() {
        view = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(view);

        IncludeExcludeStep s = new IncludeExcludeStep();
        view.add(s.getView());

        steps = new ArrayList<IncludeExcludeStep>();
        steps.add(s);

        JLabel addButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
        addButton.setToolTipText("Add step");

        view.add(Box.createVerticalStrut(10));
        view.add(ViewUtil.alignLeft(addButton));

        addButton.addMouseListener(new MouseListener() {
            public void actionPerformed(ActionEvent ae) {
                final IncludeExcludeStep c = new IncludeExcludeStep();
                c.setDatasetName("previous step");

                steps.add(c);

                JLabel removeButton = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                removeButton.setToolTipText("Remove step");

                final JPanel criteriaPanel = ViewUtil.getClearPanel();
                ViewUtil.applyHorizontalBoxLayout(criteriaPanel);

                criteriaPanel.add(removeButton);
                criteriaPanel.add(Box.createHorizontalStrut(10));
                criteriaPanel.add(c.getView());

                final Component strut = Box.createVerticalStrut(10);

                int indexFromBack = 7;

                view.add(strut, view.getComponentCount() - indexFromBack);
                view.add(criteriaPanel, view.getComponentCount() - indexFromBack);

                removeButton.addMouseListener(new MouseListener() {
                    public void actionPerformed(ActionEvent ae) {
                        steps.remove(c);
                        view.remove(strut);
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

        view.add(Box.createVerticalStrut(10));

        ZygosityStepViewGenerator zygosity = new ZygosityStepViewGenerator();

        view.add(zygosity.getView());
        view.add(Box.createVerticalStrut(10));

        InheritanceStepViewGenerator inheritance = new InheritanceStepViewGenerator();

        view.add(inheritance.getView());

        view.add(Box.createVerticalStrut(10));


        JButton runButton = new JButton("Run");

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                new MedSavantWorker<Object>(FamilyMattersOptionView.class.getCanonicalName()) {
                    MedSavantWorker currentWorker;
                    private int notificationID;
                    @Override
                    protected void showProgress(double fract) {
                    }

                    @Override
                    protected void showSuccess(Object result) {
                    }

                    @Override
                    protected Object doInBackground() throws Exception {

                        jobNumber++;

                        final Locks.FileResultLock fileLock = new Locks.FileResultLock();
                        final Locks.DialogLock dialogLock = new Locks.DialogLock();

                        Notification j = new Notification("Cohort Analysis #" + jobNumber) {
                            @Override
                            public void showResults() {
                                dialogLock.getResultsDialog().setTitle(this.getTitle());
                                dialogLock.getResultsDialog().setVisible(true);
                            }

                            @Override
                            public void cancelJob() {
                                if (currentWorker != null) {
                                    currentWorker.cancel(true);
                                }
                            }

                            @Override
                            public void closeJob(){
                                NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).markNotificationAsComplete(notificationID);
                            }
                        };

                        notificationID = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).addNotification(j.getView());
                        //AnalyticsJobsPanel.getInstance().addJob(j);

                        j.setStatus(Notification.JobStatus.RUNNING);

                        // File retriever
                        currentWorker = getRetrieverWorker(j, fileLock);
                        currentWorker.execute();
                        try {
                            synchronized (fileLock) {
                                fileLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        }

                        System.out.println("Running algorithm on file " + fileLock.getFile().getAbsolutePath());

                        // Algorithm Runner
                        currentWorker = getAlgorithmWorker(fileLock.getFile(), steps, zygosityStep, inheritanceStep, j, dialogLock);
                        currentWorker.execute();

                        try {
                            synchronized (dialogLock) {
                                dialogLock.wait();
                            }
                        } catch (InterruptedException ex) {
                        } finally {
                            NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).markNotificationAsComplete(notificationID);
                        }

                        j.setStatus(Notification.JobStatus.FINISHED);
                        j.setStatusMessage("Complete");

                        int result = DialogUtils.askYesNo("Cohort Analysis Complete", "<html>Would you like to view the<br/>results now?</html>");
                        if (result == DialogUtils.YES) {
                            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                            dialogLock.getResultsDialog().setSize(new Dimension((int)(screenSize.width*0.9), (int)(screenSize.height * 0.9)));
                            dialogLock.getResultsDialog().setVisible(true);
                        }

                        return null;
                    }
                }.execute();

                DialogUtils.displayMessage("Cohort Analysis Submitted");
            }

            private MedSavantWorker getAlgorithmWorker(File file, List<IncludeExcludeStep> steps, ZygosityStep zygosityStep, InheritanceStep model, Notification j, Locks.DialogLock genericLock) {
                FamilyMattersWorker w = new FamilyMattersWorker(steps, zygosityStep, model, file);
                w.setUIComponents(j);
                w.setCompletionLock(genericLock);
                return w;
            }

            private MedSavantWorker getRetrieverWorker(final Notification m, final Locks.FileResultLock fileLock) {
                return new MedSavantWorker<File>(FamilyMattersOptionView.class.getCanonicalName()) {
                    @Override
                    protected void showProgress(double fract) {
                        m.setProgress(fract);
                    }

                    @Override
                    protected void showSuccess(File result) {
                        m.setStatusMessage("Complete");
                        synchronized (fileLock) {
                            fileLock.setFile(result);
                            fileLock.notify();
                        }
                    }

                    @Override
                    protected File doInBackground() throws Exception {

                        m.setStatusMessage("Retrieving Variants");

                        String session = LoginController.getInstance().getSessionID();
                        int refID = ReferenceController.getInstance().getCurrentReferenceID();
                        int projectID = ProjectController.getInstance().getCurrentProjectID();
                        int updateID = MedSavantClient.ProjectManager.getNewestUpdateID(
                                session,
                                projectID,
                                refID,
                                true);

                        File outdir = DirectorySettings.generateDateStampDirectory(DirectorySettings.getTmpDirectory());

                        // hard code for testing only
                        //File tdfFile = new File("/private/var/folders/np/94t7v45x3ll1nls20039ynk00000gn/T/msavant/2013_7_8_10_28_14_317/./FORGE162-varexport-1373292779750.tdf");

                        File tdfFile;
                        if (filtersChangedSinceLastDump || (lastTDFFile == null)) {

                            if (lastTDFFile == null) {
                                System.out.println("No previous dump");
                            } else {
                                System.out.println("Filters changed since last dump");
                            }

                            File zipFile = new File(outdir, System.currentTimeMillis() + "-variantdump.tdf.zip");

                            System.out.println("Exporting to " + zipFile.getAbsolutePath());
                            tdfFile = ExportVCF.exportTDF(zipFile, this);
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


                        int[] columnsToKeep = new int[]{3, 4, 5, 7, 8, 11, 12};
                        m.setStatusMessage("Stripping file");

                        File strippedFile = awkColumnsFromFile(tdfFile, columnsToKeep);
                        //File strippedFile = new File(tdfFile.getAbsolutePath() + ".awk");
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

        view.add(ViewUtil.centerHorizontally(runButton));

    }
}
