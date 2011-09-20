/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FilterPanelFactory {

    
    public static FilterView getCategoricalFilterView(final String name, List<String> options, final DbColumn column) {
        HashMap<String,String> map = new HashMap<String,String>();
        for(String s : options) {
            map.put(s, s);
        }
        return getCategoricalFilterView(name,options,map,column);
    }
    
    public static FilterView getCategoricalFilterView(final String name, List<String> options, Map<String,String> optionsToDbValueMap, final DbColumn column) {


        long start = System.currentTimeMillis();

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);
        final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                final List<String> acceptableValues = new ArrayList<String>();
                for (JCheckBox b : boxes) {
                    if (b.isSelected()) {
                        acceptableValues.add(b.getText());
                    }
                }

                if (acceptableValues.size() == boxes.size()) {
                    FilterController.removeFilter(name);
                } else {
                    Filter f = new QueryFilter() {

                        @Override
                        public Condition[] getConditions() {
                            Condition[] results = new Condition[acceptableValues.size()];
                            int i = 0;
                            for (String s : acceptableValues) {
                                results[i++] = BinaryCondition.equalTo(column, s);
                            }
                            return results;
                        }

                        @Override
                        public String getName() {
                            return name;
                        }
                    };
                    //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                    System.out.println("Adding filter: " + f.getName());
                    FilterController.addFilter(f);
                }

                //TODO: why does this not work? Freezes GUI
                //apply.setEnabled(false);
            }
        });

        for (String s : options) {
            JCheckBox b = new JCheckBox(s);
            b.setSelected(true);
            b.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    AbstractButton abstractButton =
                            (AbstractButton) e.getSource();
                    ButtonModel buttonModel = abstractButton.getModel();
                    boolean pressed = buttonModel.isPressed();
                    if (pressed) {
                        applyButton.setEnabled(true);
                    }
                    //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
                }
            });
            b.setAlignmentX(0F);
            container.add(b);
            boxes.add(b);
        }

        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(true);
                    applyButton.setEnabled(true);
                }
            }
        });
        bottomContainer.add(selectAll);

        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                for (JCheckBox c : boxes) {
                    c.setSelected(false);
                    applyButton.setEnabled(true);
                }
            }
        });
        bottomContainer.add(selectNone);

        bottomContainer.add(Box.createGlue());

        bottomContainer.add(applyButton);

        bottomContainer.setAlignmentX(0F);
        container.add(bottomContainer);

        long elapsedTimeMillis = System.currentTimeMillis() - start;

        // Get elapsed time in seconds
        float elapsedTimeSec = elapsedTimeMillis / 1000F;

        //System.out.println("Took " + elapsedTimeSec + " seconds to make " + name + " filter");

        return new FilterView(name, container);
    }
}
/*
TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
DbColumn col = table.getDBColumn(columnAlias);
boolean isNumeric = TableSchema.isNumeric(table.getColumnType(col));
boolean isBoolean = TableSchema.isBoolean(table.getColumnType(col));

//special cases (TODO: messy...)
if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
isNumeric = false;
isBoolean = false;
}

if (isNumeric) {

Range extremeValues = null;

if (columnAlias.equals(VariantTableSchema.ALIAS_POSITION)) {
extremeValues = new Range(1,250000000);
} else if (columnAlias.equals(VariantTableSchema.ALIAS_SB)) {
extremeValues = new Range(-100,100);
} else {
extremeValues = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, col);
}

if (columnAlias.equals(VariantTableSchema.ALIAS_DP)) {
extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
}

JPanel container = new JPanel();
container.setBorder(ViewUtil.getMediumBorder());
container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

final int min = (int) Math.floor(extremeValues.getMin());
final int max = (int) Math.ceil(extremeValues.getMax());

rs.setMinimum(min);
rs.setMaximum(max);

rs.setMajorTickSpacing(5);
rs.setMinorTickSpacing(1);

rs.setLowValue(min);
rs.setHighValue(max);

JPanel rangeContainer = new JPanel();
rangeContainer.setLayout(new BoxLayout(rangeContainer, BoxLayout.X_AXIS));

final JTextField frombox = new JTextField(ViewUtil.numToString(min));
final JTextField tobox = new JTextField(ViewUtil.numToString(max));

final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
final JLabel toLabel = new JLabel(ViewUtil.numToString(max));

rangeContainer.add(fromLabel);
rangeContainer.add(rs);
rangeContainer.add(toLabel);

container.add(frombox);
container.add(tobox);
container.add(rangeContainer);
container.add(Box.createVerticalBox());

rs.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
frombox.setText(ViewUtil.numToString(rs.getLowValue()));
tobox.setText(ViewUtil.numToString(rs.getHighValue()));
}
});

tobox.addKeyListener(new KeyListener() {

public void keyTyped(KeyEvent e) {

}

public void keyPressed(KeyEvent e) {
}

public void keyReleased(KeyEvent e) {
int key = e.getKeyCode();
if (key == KeyEvent.VK_ENTER) {
try {
int num = (int) Math.ceil(getNumber(tobox.getText()));
rs.setHighValue(num);
tobox.setText(ViewUtil.numToString(num));
} catch (Exception e2) {
e2.printStackTrace();
tobox.requestFocus();
}
}
}

});

frombox.addKeyListener(new KeyListener() {

public void keyTyped(KeyEvent e) {

}

public void keyPressed(KeyEvent e) {
}

public void keyReleased(KeyEvent e) {
int key = e.getKeyCode();
if (key == KeyEvent.VK_ENTER) {
try {
int num = (int) Math.floor(getNumber(frombox.getText()));
rs.setLowValue(num);
frombox.setText(ViewUtil.numToString(num));
} catch (Exception e2) {
e2.printStackTrace();
frombox.requestFocus();
}
}
}

});


final JButton applyButton = new JButton("Apply");
applyButton.setEnabled(false);

applyButton.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {

applyButton.setEnabled(false);

Range acceptableRange = new Range(rs.getLowValue(), rs.getHighValue());

if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
} else {
Filter f = new QueryFilter() {

@Override
public Condition[] getConditions() {
Condition[] results = new Condition[2];
results[0] = BinaryCondition.greaterThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getLowValue(), true);
results[1] = BinaryCondition.lessThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getHighValue(), true);

Condition[] resultsCombined = new Condition[1];
resultsCombined[0] = ComboCondition.and(results);

return resultsCombined;
}

@Override
public String getName() {
return columnAlias;
}
};
//Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
System.out.println("Adding filter: " + f.getName());
FilterController.addFilter(f);
}

//TODO: why does this not work? Freezes GUI
//apply.setEnabled(false);
}
});

rs.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
applyButton.setEnabled(true);
}
});

JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
selectAll.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
rs.setLowValue(min);
rs.setHighValue(max);
}
});

JPanel bottomContainer = new JPanel();
bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

bottomContainer.add(selectAll);
bottomContainer.add(Box.createHorizontalGlue());
bottomContainer.add(applyButton);

container.add(bottomContainer);

final FilterView fv = new FilterView(columnAlias, container);
l.add(fv);

} else if (isBoolean) {

List<String> uniq = new ArrayList<String>();
uniq.add("True");
uniq.add("False");

JPanel container = new JPanel();
container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

JPanel bottomContainer = new JPanel();
bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

final JButton applyButton = new JButton("Apply");
applyButton.setEnabled(false);
final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

applyButton.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {

applyButton.setEnabled(false);

final List<String> acceptableValues = new ArrayList<String>();

if (boxes.get(0).isSelected()) {
acceptableValues.add("1");
}
if (boxes.get(1).isSelected()) {
acceptableValues.add("0");
}

if (acceptableValues.size() == boxes.size()) {
FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
} else {
Filter f = new QueryFilter() {

@Override
public Condition[] getConditions() {
Condition[] results = new Condition[acceptableValues.size()];
int i = 0;
for (String s : acceptableValues) {
results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
}
return results;
}

@Override
public String getName() {
return columnAlias;
}
};
//Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
System.out.println("Adding filter: " + f.getName());
FilterController.addFilter(f);
}

//TODO: why does this not work? Freezes GUI
//apply.setEnabled(false);
}
});

for (String s : uniq) {
JCheckBox b = new JCheckBox(s);
b.setSelected(true);
b.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
AbstractButton abstractButton =
(AbstractButton) e.getSource();
ButtonModel buttonModel = abstractButton.getModel();
boolean pressed = buttonModel.isPressed();
if (pressed) {
applyButton.setEnabled(true);
}
//System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
}
});
b.setAlignmentX(0F);
container.add(b);
boxes.add(b);
}

JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
selectAll.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(true);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectAll);

JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

selectNone.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(false);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectNone);

bottomContainer.add(Box.createGlue());

bottomContainer.add(applyButton);

bottomContainer.setAlignmentX(0F);
container.add(bottomContainer);

FilterView fv = new FilterView(columnAlias, container);
l.add(fv);


} else {

Connection conn = ConnectionController.connect();

final List<String> uniq;

if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
uniq = new ArrayList<String>();
uniq.addAll(Arrays.asList(VariantRecord.ALIAS_ZYGOSITY));
} else if (columnAlias.equals(VariantTableSchema.ALIAS_CHROM)) {
uniq = new ArrayList<String>();
uniq.addAll(Arrays.asList(
new String[]{
"chr1","chr2","chr3","chr4","chr5","chr6","chr7","chr8",
"chr9","chr10","chr11","chr12","chr13","chr14","chr15","chr16",
"chr17","chr18","chr19","chr20","chr21","chr22","chrX","chrY"
}));
} else if (columnAlias.equals(VariantTableSchema.ALIAS_REFERENCE)
|| columnAlias.equals(VariantTableSchema.ALIAS_ALTERNATE)) {
uniq = new ArrayList<String>();
uniq.addAll(Arrays.asList(
new String[]{
"A","C","G","T"
}));
} 
else {
uniq = QueryUtil.getDistinctValuesForColumn(conn, table, col);
}

if (columnAlias.equals(VariantTableSchema.ALIAS_CHROM)) {
Collections.sort(uniq,new ChromosomeComparator());
}

JPanel container = new JPanel();
container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

JPanel bottomContainer = new JPanel();
bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

final JButton applyButton = new JButton("Apply");
applyButton.setEnabled(false);
final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

applyButton.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {

applyButton.setEnabled(false);

final List<String> acceptableValues = new ArrayList<String>();
for (JCheckBox b : boxes) {
if (b.isSelected()) {
acceptableValues.add(b.getText());
}
}

if (acceptableValues.size() == boxes.size()) {
FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
} else {
Filter f = new QueryFilter() {

@Override
public Condition[] getConditions() {
Condition[] results = new Condition[acceptableValues.size()];
int i = 0;
for (String s : acceptableValues) {
if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), uniq.indexOf(s));
} else {
results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);                                           
}
}
return results;
}

@Override
public String getName() {
return columnAlias;
}
};
//Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
System.out.println("Adding filter: " + f.getName());
FilterController.addFilter(f);
}

//TODO: why does this not work? Freezes GUI
//apply.setEnabled(false);
}
});

for (String s : uniq) {
JCheckBox b = new JCheckBox(s);
b.setSelected(true);
b.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
AbstractButton abstractButton =
(AbstractButton) e.getSource();
ButtonModel buttonModel = abstractButton.getModel();
boolean pressed = buttonModel.isPressed();
if (pressed) {
applyButton.setEnabled(true);
}
//System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
}
});
b.setAlignmentX(0F);
container.add(b);
boxes.add(b);
}

JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
selectAll.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(true);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectAll);

JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

selectNone.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(false);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectNone);

bottomContainer.add(Box.createGlue());

bottomContainer.add(applyButton);

bottomContainer.setAlignmentX(0F);
container.add(bottomContainer);

FilterView fv = new FilterView(columnAlias, container);
l.add(fv);
}

// Get elapsed time in milliseconds
long elapsedTimeMillis = System.currentTimeMillis()-start;

// Get elapsed time in seconds
float elapsedTimeSec = elapsedTimeMillis/1000F;

System.out.println("Took " + elapsedTimeSec + " seconds to make " + columnAlias + " filter");
}

return l;
 */
/** NUMERIC **/

/*
if (true) {

Range extremeValues = null;

if (columnAlias.equals(VariantTableSchema.ALIAS_POSITION)) {
extremeValues = new Range(1,250000000);
} else if (columnAlias.equals(VariantTableSchema.ALIAS_SB)) {
extremeValues = new Range(-100,100);
} else {
extremeValues = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, col);
}

if (columnAlias.equals(VariantTableSchema.ALIAS_DP)) {
extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
}

JPanel container = new JPanel();
container.setBorder(ViewUtil.getMediumBorder());
container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

final int min = (int) Math.floor(extremeValues.getMin());
final int max = (int) Math.ceil(extremeValues.getMax());

rs.setMinimum(min);
rs.setMaximum(max);

rs.setMajorTickSpacing(5);
rs.setMinorTickSpacing(1);

rs.setLowValue(min);
rs.setHighValue(max);

JPanel rangeContainer = new JPanel();
rangeContainer.setLayout(new BoxLayout(rangeContainer, BoxLayout.X_AXIS));

final JTextField frombox = new JTextField(ViewUtil.numToString(min));
final JTextField tobox = new JTextField(ViewUtil.numToString(max));

final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
final JLabel toLabel = new JLabel(ViewUtil.numToString(max));

rangeContainer.add(fromLabel);
rangeContainer.add(rs);
rangeContainer.add(toLabel);

container.add(frombox);
container.add(tobox);
container.add(rangeContainer);
container.add(Box.createVerticalBox());

rs.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
frombox.setText(ViewUtil.numToString(rs.getLowValue()));
tobox.setText(ViewUtil.numToString(rs.getHighValue()));
}
});

tobox.addKeyListener(new KeyListener() {

public void keyTyped(KeyEvent e) {

}

public void keyPressed(KeyEvent e) {
}

public void keyReleased(KeyEvent e) {
int key = e.getKeyCode();
if (key == KeyEvent.VK_ENTER) {
try {
int num = (int) Math.ceil(getNumber(tobox.getText()));
rs.setHighValue(num);
tobox.setText(ViewUtil.numToString(num));
} catch (Exception e2) {
e2.printStackTrace();
tobox.requestFocus();
}
}
}

});

frombox.addKeyListener(new KeyListener() {

public void keyTyped(KeyEvent e) {

}

public void keyPressed(KeyEvent e) {
}

public void keyReleased(KeyEvent e) {
int key = e.getKeyCode();
if (key == KeyEvent.VK_ENTER) {
try {
int num = (int) Math.floor(getNumber(frombox.getText()));
rs.setLowValue(num);
frombox.setText(ViewUtil.numToString(num));
} catch (Exception e2) {
e2.printStackTrace();
frombox.requestFocus();
}
}
}

});


final JButton applyButton = new JButton("Apply");
applyButton.setEnabled(false);

applyButton.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {

applyButton.setEnabled(false);

Range acceptableRange = new Range(rs.getLowValue(), rs.getHighValue());

if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
} else {
Filter f = new QueryFilter() {

@Override
public Condition[] getConditions() {
Condition[] results = new Condition[2];
results[0] = BinaryCondition.greaterThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getLowValue(), true);
results[1] = BinaryCondition.lessThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), rs.getHighValue(), true);

Condition[] resultsCombined = new Condition[1];
resultsCombined[0] = ComboCondition.and(results);

return resultsCombined;
}

@Override
public String getName() {
return columnAlias;
}
};
//Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
System.out.println("Adding filter: " + f.getName());
FilterController.addFilter(f);
}

//TODO: why does this not work? Freezes GUI
//apply.setEnabled(false);
}
});

rs.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
applyButton.setEnabled(true);
}
});

JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
selectAll.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
rs.setLowValue(min);
rs.setHighValue(max);
}
});

JPanel bottomContainer = new JPanel();
bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

bottomContainer.add(selectAll);
bottomContainer.add(Box.createHorizontalGlue());
bottomContainer.add(applyButton);

container.add(bottomContainer);

final FilterView fv = new FilterView(columnAlias, container);
l.add(fv);

}
 */
/** BOOLEAN **/
/**
 *  else if (isBoolean) {

List<String> uniq = new ArrayList<String>();
uniq.add("True");
uniq.add("False");

JPanel container = new JPanel();
container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

JPanel bottomContainer = new JPanel();
bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

final JButton applyButton = new JButton("Apply");
applyButton.setEnabled(false);
final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

applyButton.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {

applyButton.setEnabled(false);

final List<String> acceptableValues = new ArrayList<String>();

if (boxes.get(0).isSelected()) {
acceptableValues.add("1");
}
if (boxes.get(1).isSelected()) {
acceptableValues.add("0");
}

if (acceptableValues.size() == boxes.size()) {
FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
} else {
Filter f = new QueryFilter() {

@Override
public Condition[] getConditions() {
Condition[] results = new Condition[acceptableValues.size()];
int i = 0;
for (String s : acceptableValues) {
results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
}
return results;
}

@Override
public String getName() {
return columnAlias;
}
};
//Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
System.out.println("Adding filter: " + f.getName());
FilterController.addFilter(f);
}

//TODO: why does this not work? Freezes GUI
//apply.setEnabled(false);
}
});

for (String s : uniq) {
JCheckBox b = new JCheckBox(s);
b.setSelected(true);
b.addChangeListener(new ChangeListener() {

public void stateChanged(ChangeEvent e) {
AbstractButton abstractButton =
(AbstractButton) e.getSource();
ButtonModel buttonModel = abstractButton.getModel();
boolean pressed = buttonModel.isPressed();
if (pressed) {
applyButton.setEnabled(true);
}
//System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
}
});
b.setAlignmentX(0F);
container.add(b);
boxes.add(b);
}

JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
selectAll.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(true);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectAll);

JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

selectNone.addActionListener(new ActionListener() {

public void actionPerformed(ActionEvent e) {
for (JCheckBox c : boxes) {
c.setSelected(false);
applyButton.setEnabled(true);
}
}
});
bottomContainer.add(selectNone);

bottomContainer.add(Box.createGlue());

bottomContainer.add(applyButton);

bottomContainer.setAlignmentX(0F);
container.add(bottomContainer);

FilterView fv = new FilterView(columnAlias, container);
l.add(fv);


} else {
 */
