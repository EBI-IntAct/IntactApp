package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.components.diagrams.NodeDiagram;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NodeSchematic extends AbstractNodeAttribute {

    private NodeDiagram nodeDiagram;
    private final List<Feature> features;

    public NodeSchematic(Node node, List<Feature> features, OpenBrowser openBrowser) {
        super(null, node, openBrowser);
        this.features = features;
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new GridBagLayout());
        nodeDiagram = new NodeDiagram(node, features);
        EasyGBC c = new EasyGBC();
        content.add(nodeDiagram, c.anchor("west").noExpand());
        content.add(Box.createHorizontalGlue(), c.right().anchor("west").expandHoriz());
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (nodeDiagram != null) {
            nodeDiagram.setBackground(bg);
        }
    }
}
