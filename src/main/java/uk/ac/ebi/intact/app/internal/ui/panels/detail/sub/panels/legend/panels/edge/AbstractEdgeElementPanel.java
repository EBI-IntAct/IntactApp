package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.edge;

import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;

import java.awt.*;

public abstract class AbstractEdgeElementPanel extends JPanel {
    protected EasyGBC layoutHelper = new EasyGBC();

    public AbstractEdgeElementPanel() {
        super(new GridBagLayout());
        setBackground(UIColors.lightBackground);
    }
}
