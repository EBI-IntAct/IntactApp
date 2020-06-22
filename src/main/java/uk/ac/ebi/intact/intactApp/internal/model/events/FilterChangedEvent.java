package uk.ac.ebi.intact.intactApp.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.intactApp.internal.model.filters.Filter;

public class FilterChangedEvent extends AbstractCyEvent<Filter> {

    public FilterChangedEvent(Filter source) {
        super(source, FilterChangedListener.class);
    }
}
