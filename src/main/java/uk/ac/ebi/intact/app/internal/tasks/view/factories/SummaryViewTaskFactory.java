package uk.ac.ebi.intact.app.internal.tasks.view.factories;


import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.SummaryViewTask;

public class SummaryViewTaskFactory extends AbstractHiderTaskFactory {

    public SummaryViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SummaryViewTask(manager, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
