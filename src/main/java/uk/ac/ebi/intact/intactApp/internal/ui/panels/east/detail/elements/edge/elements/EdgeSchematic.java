package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.model.styles.utils.StyleMapper;
import uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams.NodeDiagram;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class EdgeSchematic extends AbstractEdgeElement {
    private final JPanel nodesPanel = new JPanel(new GridBagLayout());
    private final JPanel edgePanel = new JPanel(new GridBagLayout());
    public final static Color transparentColor = new Color(0, 0, 0, 0);

    public EdgeSchematic(IntactEdge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        setBorder(new EmptyBorder(0,4,0,4));
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
        drawNodes(edge);
        int thickness = edge.subEdgeSUIDs.size() + 2;
        thickness = Integer.min(thickness, 25);
        EdgeDiagram edgeDiagram = new EdgeDiagram(CollapsedIntactStyle.getColor(edge.miScore), thickness, false);
        drawEdgePanel(edgeDiagram);
    }

    @Override
    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        drawNodes(edge);
        EdgeDiagram edgeDiagram = new EdgeDiagram(StyleMapper.edgeTypeToPaint.get(edge.type), 4, edge.expansionType != null);
        drawEdgePanel(edgeDiagram);
    }

    private void drawNodes(IntactEdge edge) {
        content.setLayout(new OverlayLayout(content));
        nodesPanel.setOpaque(false);

        Map<IntactNode, List<Feature>> features = edge.getFeatures();
        EasyGBC c = new EasyGBC();

        NodeDiagram sourceDiagram = new NodeDiagram(edge.source, features.get(edge.source));
        sourceDiagram.setOpaque(false);
        sourceDiagram.setBackground(transparentColor);
        nodesPanel.add(sourceDiagram, c.expandBoth());

        NodeDiagram targetDiagram = new NodeDiagram(edge.target, features.get(edge.target));
        targetDiagram.setOpaque(false);
        targetDiagram.setBackground(transparentColor);
        nodesPanel.add(targetDiagram, c.down().expandBoth());

        content.add(nodesPanel);
    }

    private static class EdgeDiagram extends JComponent {
        Paint color;
        boolean dashed;
        int thickness;

        public EdgeDiagram(Paint paint, int thickness, boolean dashed) {
            this.color = paint;
            this.dashed = dashed;
            this.thickness = thickness;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(color);
            if (dashed) {
                g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{6.0f, 5.0f}, 0));
            } else {
                g2.setStroke(new BasicStroke(thickness));
            }

//            int halfHeight = getHeight() / 2;
//            int quarterWidth = getWidth() / 4;

            int halfWidth = getWidth() / 2;
            int quarterHeight = getHeight() / 4;
            g2.drawLine(halfWidth, quarterHeight + 10, halfWidth, getHeight() - quarterHeight - 10);
        }
    }


    private void drawEdgePanel(EdgeDiagram edgeDiagram) {
        edgePanel.setOpaque(false);
        edgeDiagram.setOpaque(true);
        edgePanel.add(edgeDiagram, new EasyGBC().expandBoth());
        content.add(edgePanel);
    }


}