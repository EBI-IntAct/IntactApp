package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class MutationViewTask extends AbstractHiderTask {
    public MutationViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    public MutationViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, NetworkView networkView) {
        super(manager, hideTaskFactory, unHideTaskFactory, networkView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        expandEdgesIfNeeded();
        if (chosenView != null && chosenView.getType() != NetworkView.Type.MUTATION) {
            manager.data.viewChanged(NetworkView.Type.MUTATION, chosenView);
        }
    }
}
