package uk.ac.ebi.intact.app.internal.tasks.view;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SelectEdgesTaskFactory;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.managers.Manager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHiderTask extends AbstractTask {
    protected Manager manager;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public final boolean currentView;
    @Tunable(description = "Network view", longDescription = "Network view to manipulate. If not set, the current one will be used if possible.", dependsOn = "currentView=false")
    public ListSingleSelection<NetworkView> view;
    protected CyNetworkView cyView;
    protected Network chosenNetwork;
    protected NetworkView chosenView;

    public AbstractHiderTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        this.manager = manager;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
        this.currentView = currentView;
        if (!currentView) {
            view = new ListSingleSelection<>(ArrayUtils.insert(0, manager.data.getViews(), new CurrentView(manager)));
        }
    }

    public AbstractHiderTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, NetworkView networkView) {
        this.manager = manager;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
        this.currentView = false;
        chosenView = networkView;
    }

    protected void collapseEdgesIfNeeded() {
        chooseData();
        if (chosenView != null && chosenView.getType() != NetworkView.Type.SUMMARY) {
            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<CyEdge> edgesToSelect = chosenNetwork.getEvidenceEdges().stream()
                    .filter(Edge::isSelected)
                    .map(edge -> chosenNetwork.getSummaryEdge(edge.cyEdge).cyEdge)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getEvidenceCyEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getSummaryCyEdges()));
            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
        }
    }

    protected void expandEdgesIfNeeded() {
        chooseData();
        if (chosenView != null && chosenView.getType() == NetworkView.Type.SUMMARY) {
            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<CyEdge> edgesToSelect = chosenNetwork.getSummaryEdges().stream()
                    .filter(Edge::isSelected)
                    .map((SummaryEdge edge) -> chosenNetwork.getSimilarEvidenceCyEdges(edge.cyEdge))
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getSummaryCyEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getEvidenceCyEdges()));
            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
        }
    }

    private void chooseData() {
        if (!currentView) {
            if (chosenView == null) chosenView = view.getSelectedValue();
            if (chosenView instanceof CurrentView) chosenView = manager.data.getCurrentIntactNetworkView();
        } else {
            chosenView = manager.data.getCurrentIntactNetworkView();
        }
        if (chosenView != null) {
            cyView = chosenView.cyView;
            chosenNetwork = chosenView.network;
        }
    }

    private static class CurrentView extends NetworkView {

        public CurrentView(Manager manager) {
            super(manager, null, false);
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
