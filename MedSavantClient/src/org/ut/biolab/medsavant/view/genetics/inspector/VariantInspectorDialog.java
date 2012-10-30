package org.ut.biolab.medsavant.view.genetics.inspector;

import com.explodingpixels.macwidgets.HudWindow;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.broad.igv.feature.genome.Genome;
import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.view.genetics.variantinfo.BasicVariantSubInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.SimpleVariantSubInspector;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class VariantInspectorDialog {

    private static VariantInspectorDialog instance;
    private final HudWindow hud;
    private SimpleVariant simpleVariant;
    private static List<Listener<SimpleVariant>> listeners = new ArrayList<Listener<SimpleVariant>>();
    private final CollapsibleInspector si;
    //private final JFrame f;

    private VariantInspectorDialog() {
        hud = new HudWindow("Variant Inspector");
        hud.getJDialog().setSize(300, 350);
        //hud.getJDialog().setLocationRelativeTo(null);
        //hud.getJDialog().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //si = null;

        //f = new JFrame("F");

        si = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Simple Variant";
            }
        };

        SimpleVariantSubInspector subinspector = new SimpleVariantSubInspector();
        addVariantSelectionChangedListener(subinspector);

        si.addSubInspector(subinspector);

        //f.getContentPane().add(si);
        JPanel p = new JPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        p.add(new JLabel("I'm a label!"));
        //p.add(si.getContent());
        p.add(subinspector.getInfoPanel());
        p.add(new JButton("Button"));

        hud.getContentPane().add(p);

    }

    public static VariantInspectorDialog getInstance() {
        if (instance == null) {
            instance = new VariantInspectorDialog();
        }
        return instance;
    }

    public void setDialogVisible(boolean b) {
        hud.getJDialog().setVisible(b);
        //f.setVisible(b);
    }

    public void setSimpleVariant(SimpleVariant v) {
        this.simpleVariant = v;
        for (Listener<SimpleVariant> l : listeners) {
            l.handleEvent(v);
        }
    }

    public final void addVariantSelectionChangedListener(Listener<SimpleVariant> l) {
        listeners.add(l);
    }

    public static class SimpleVariant implements Comparable {

        public final String chr;
        public final long pos;
        public final String ref;
        public final String alt;
        public final String type;

        public SimpleVariant(String chr, long pos, String ref, String alt, String type) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
            this.type = type;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.chr != null ? this.chr.hashCode() : 0);
            hash = 59 * hash + (int) (this.pos ^ (this.pos >>> 32));
            hash = 59 * hash + (this.ref != null ? this.ref.hashCode() : 0);
            hash = 59 * hash + (this.alt != null ? this.alt.hashCode() : 0);
            hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleVariant other = (SimpleVariant) obj;
            if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
                return false;
            }
            if (this.pos != other.pos) {
                return false;
            }
            if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
                return false;
            }
            if ((this.alt == null) ? (other.alt != null) : !this.alt.equals(other.alt)) {
                return false;
            }
            if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof SimpleVariant)) {
                return -1;
            }
            SimpleVariant other = (SimpleVariant) o;
            int chromCompare = (new Genome.ChromosomeComparator()).compare(this.chr, other.chr);
            if (chromCompare != 0) {
                return chromCompare;
            }
            return ((Long) this.pos).compareTo(other.pos);
        }

        @Override
        public String toString() {
            return "SimpleVariant{" + "chr=" + chr + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", type=" + type + '}';
        }
    }
}
