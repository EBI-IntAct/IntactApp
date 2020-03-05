package uk.ac.ebi.intact.intactApp.internal.utils.styles.from.data;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.IntactStyle;

import java.awt.*;

public class MutationIntactStyle extends IntactStyle {
    public MutationIntactStyle(IntactManager manager) {
        super(manager, "Intact - Mutation");
    }

    @Override
    protected void setNodeBorderPaintStyle() {
        DiscreteMapping<Boolean, Paint> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.MUTATION, Boolean.class, BasicVisualLexicon.NODE_BORDER_PAINT);
        dMapping.putMapValue(true, new Color(255, 0, 0));

        style.addVisualMappingFunction(dMapping);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        //TODO Move to data instead of webservice infos
        DiscreteMapping<String, LineType> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction("style::shape", String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        dMapping.putMapValue("solid", LineTypeVisualProperty.SOLID);
        dMapping.putMapValue("dashed", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(dMapping);
    }

    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<Boolean, Paint> dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.DISRUPTED_BY_MUTATION, Boolean.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
        dMapping.putMapValue(true, new Color(255, 0, 0));
        dMapping.putMapValue(false, new Color(126, 131, 137));

        style.addVisualMappingFunction(dMapping);
    }
}
