package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractSelectedElementPanel;

abstract class AbstractEdgeElement extends AbstractSelectedElementPanel {
    private final Edge iEdge;

    public AbstractEdgeElement(String title, Edge iEdge, OpenBrowser openBrowser) {
        super(title, openBrowser);
        this.iEdge = iEdge;
    }

    @Override
    protected void fillContent() {
        if (iEdge instanceof SummaryEdge) {
            fillSummaryEdgeContent((SummaryEdge) iEdge);
        } else if (iEdge instanceof EvidenceEdge) {
            fillEvidenceEdgeContent((EvidenceEdge) iEdge);
        }
    }

    protected abstract void fillSummaryEdgeContent(SummaryEdge edge);

    protected abstract void fillEvidenceEdgeContent(EvidenceEdge edge);
}
