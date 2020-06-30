package uk.ac.ebi.intact.app.internal.model.filters;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.CollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public abstract class ContinuousFilter<T extends Element> extends Filter<T> {
    protected double min;
    protected double max;
    protected double currentMin;
    protected double currentMax;

    public ContinuousFilter(NetworkView view, Class<T> elementType, String name, double min, double max) {
        super(view, name, elementType);
        this.min = min;
        this.max = max;
        currentMin = min;
        currentMax = max;
    }

    public ContinuousFilter(NetworkView view, Class<T> elementType, String name) {
        super(view, name, elementType);
        List<? extends Element> elements;
        if (Node.class.isAssignableFrom(elementType)) {
            elements = network.getINodes();
        } else if (elementType == CollapsedEdge.class) {
            elements = network.getCollapsedIEdges();
        } else if (elementType == EvidenceEdge.class) {
            elements = network.getEvidenceIEdges();
        } else throw new IllegalArgumentException();

        DoubleSummaryStatistics stats = elements.stream().mapToDouble(value -> getProperty(elementType.cast(value))).summaryStatistics();

        this.min = stats.getMin();
        this.max = stats.getMax();
        currentMin = min;
        currentMax = max;
    }

    @Override
    public boolean load(JsonNode json) {
        if (!super.load(json)) return false;
        min = json.get("min").doubleValue();
        max = json.get("max").doubleValue();
        currentMin = json.get("currentMin").doubleValue();
        currentMax = json.get("currentMax").doubleValue();
        return true;
    }

    public abstract double getProperty(T element);

    @Override
    public void filterView() {
        if (currentMin == min && currentMax == max) return;
        Collection<? extends Element> elementsToFilter;

        if (Node.class.isAssignableFrom(elementType)) {
            elementsToFilter = view.visibleNodes;
        } else if (Edge.class.isAssignableFrom(elementType)) {
            if (elementType == CollapsedEdge.class && view.getType() != NetworkView.Type.COLLAPSED) return;
            if (elementType == EvidenceEdge.class && view.getType() == NetworkView.Type.COLLAPSED) return;
            elementsToFilter = view.visibleEdges;
        } else {
            return;
        }

        elementsToFilter.removeIf(element -> {
            double property = getProperty(elementType.cast(element));
            return property < currentMin || property > currentMax;
        });
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        if (min < max) {
            if (min > currentMin) currentMin = min;
            this.min = min;
        }
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        if (max > min) {
            if (max > currentMax) currentMax = max;
            this.max = max;
        }
    }

    public double getCurrentMin() {
        return currentMin;
    }

    public void setCurrentMin(double currentMin) {
        this.currentMin = currentMin;
        view.filter();
    }

    public double getCurrentMax() {
        return currentMax;
    }

    public void setCurrentMax(double currentMax) {
        this.currentMax = currentMax;
        view.filter();
    }

    public void setCurrentPositions(double min, double max) {
        this.currentMin = min;
        this.currentMax = max;
        view.filter();
    }
}