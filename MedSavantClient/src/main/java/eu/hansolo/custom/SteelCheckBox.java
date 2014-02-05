package eu.hansolo.custom;

/**
 *
 * @author hansolo
 */
public class SteelCheckBox extends javax.swing.JCheckBox
{
    // <editor-fold defaultstate="collapsed" desc="Variable declaration">
    private boolean colored = false;
    private boolean rised = false;
    private eu.hansolo.tools.ColorDef selectedColor = eu.hansolo.tools.ColorDef.JUG_GREEN;
    protected static final String COLORED_PROPERTY = "colored";
    protected static final String COLOR_PROPERTY = "color";
    protected static final String RISED_PROPERTY = "rised";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public SteelCheckBox()
    {
        super();
        setOpaque(false);
        setRised(true);
        setText(" ");
        setPreferredSize(new java.awt.Dimension(100, 26));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getter/Setter">
    public boolean isColored()
    {
        return this.colored;
    }

    public void setColored(final boolean COLORED)
    {
        final boolean OLD_STATE = this.colored;
        this.colored = COLORED;
        firePropertyChange(COLORED_PROPERTY, OLD_STATE, COLORED);
        repaint();
    }

    public boolean isRised()
    {
        return this.rised;
    }

    public void setRised(final boolean RISED)
    {
        final boolean OLD_VALUE = this.rised;
        this.rised = RISED;
        firePropertyChange(RISED_PROPERTY, OLD_VALUE, RISED);
    }

    public eu.hansolo.tools.ColorDef getSelectedColor()
    {
        return this.selectedColor;
    }

    public void setSelectedColor(final eu.hansolo.tools.ColorDef SELECTED_COLOR)
    {
        final eu.hansolo.tools.ColorDef OLD_COLOR = this.selectedColor;
        this.selectedColor = SELECTED_COLOR;
        firePropertyChange(COLOR_PROPERTY, OLD_COLOR, SELECTED_COLOR);
        repaint();
    }

    @Override
    public void setUI(final javax.swing.plaf.ButtonUI BUI)
    {
        super.setUI(new SteelCheckBoxUI(this));
    }

    public void setUi(final javax.swing.plaf.ComponentUI UI)
    {
        this.ui = new SteelCheckBoxUI(this);
    }

    @Override
    protected void setUI(final javax.swing.plaf.ComponentUI UI)
    {
        super.setUI(new SteelCheckBoxUI(this));
    }
    // </editor-fold>

    @Override
    public String toString()
    {
        return "SteelCheckBox";
    }
}
