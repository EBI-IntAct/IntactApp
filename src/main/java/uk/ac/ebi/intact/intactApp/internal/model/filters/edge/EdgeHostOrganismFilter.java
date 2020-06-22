package uk.ac.ebi.intact.intactApp.internal.model.filters.edge;

import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.filters.DiscreteFilter;

public class EdgeHostOrganismFilter extends DiscreteFilter<IntactEvidenceEdge> {

    public EdgeHostOrganismFilter(IntactNetworkView iView) {
        super(iView, IntactEvidenceEdge.class, "Host organism");
    }

    @Override
    public String getPropertyValue(IntactEvidenceEdge element) {
        return element.hostOrganism;
    }
}
