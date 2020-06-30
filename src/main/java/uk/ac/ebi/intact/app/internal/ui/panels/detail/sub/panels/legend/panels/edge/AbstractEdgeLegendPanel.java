package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge;

import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;

import java.awt.*;

public abstract class AbstractEdgeLegendPanel extends JPanel {
    protected EasyGBC layoutHelper = new EasyGBC();

    public AbstractEdgeLegendPanel() {
        super(new GridBagLayout());
        setBackground(AbstractDetailPanel.backgroundColor);
    }
}