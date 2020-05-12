package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.SelectEdgesTask;

import java.util.Collection;

public class SelectEdgesTaskFactory extends AbstractTaskFactory {

    private final CyNetwork network;
    private final Collection<CyEdge> edgesToSelect;

    public SelectEdgesTaskFactory(CyNetwork network, Collection<CyEdge> edgesToSelect) {
        this.network = network;
        this.edgesToSelect = edgesToSelect;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SelectEdgesTask(network, edgesToSelect));
    }
}
