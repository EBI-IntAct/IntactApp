package uk.ac.ebi.intact.app.internal.tasks.version;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

import java.util.Arrays;
import java.util.List;

public class VersionTask extends AbstractTask implements ObservableTask {

    final String version;

    public VersionTask(final String version) {
        this.version = version;
    }

    public void run(TaskMonitor monitor) {
    }

    @SuppressWarnings("unchecked")
    public <R> R getResults(Class<? extends R> type) {
        if (type.equals(String.class)) {
            String response = "Version: " + version + "\n";
            return (R) response;
        } else if (type.equals(JSONResult.class)) {
            return (R) (JSONResult) () -> "{\"version\":\"" + version + "\"}";
        }
        return (R) version;
    }

    public List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class);
    }
}
