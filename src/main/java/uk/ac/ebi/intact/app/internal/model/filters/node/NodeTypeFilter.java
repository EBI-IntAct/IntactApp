package uk.ac.ebi.intact.app.internal.model.filters.node;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;

public class NodeTypeFilter extends DiscreteFilter<Node> {
    public NodeTypeFilter(NetworkView view) {
        super(view, Node.class, "Type", "Interactor molecule type (protein, nucleic acid, small molecule, complex...) involved in the interaction");
    }

    @Override
    public String getPropertyValue(Node node) {
        return node.typeName;
    }
}
