package uk.ac.ebi.intact.app.internal.ui.utils;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;

public class LinkUtils {
    public static JLink createSpecieLink(OpenBrowser openBrowser, long taxId) {
        return new JLink(
                "- TaxId: " + taxId,
                "https://www.uniprot.org/taxonomy/" + taxId,
                openBrowser);
    }

    public static JLink createEvidenceEdgeLink(OpenBrowser openBrowser, EvidenceEdge evidenceEdge) {
        return new JLink(evidenceEdge.ac, "https://www.ebi.ac.uk/intact/interaction/" + evidenceEdge.ac, openBrowser);
    }

    public static JLink createCVTermLink(OpenBrowser openBrowser, CVTerm term) {
        return new JLink(term.value, term.id.getUserAccessURL(), openBrowser);
    }
}
