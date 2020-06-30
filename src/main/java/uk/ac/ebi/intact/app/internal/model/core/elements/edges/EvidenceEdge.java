package uk.ac.ebi.intact.app.internal.model.core.elements.edges;

import org.cytoscape.model.CyEdge;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;

import static uk.ac.ebi.intact.app.internal.model.tables.fields.models.EdgeFields.*;

public class EvidenceEdge extends Edge {
    public final String ac;
    public final String type;
    public final OntologyIdentifier typeMIId;
    public final String interactionDetectionMethod;
    public final OntologyIdentifier interactionDetectionMethodMIId;
    public final String participantDetectionMethod;
    public final OntologyIdentifier participantDetectionMethodMIId;
    public final String hostOrganism;
    public final String expansionType;
    public final String pubMedId;
    public final String sourceBiologicalRole;
    public final String targetBiologicalRole;
    public final long hostOrganismTaxId;
    public final long id;

    EvidenceEdge(Network network, CyEdge edge) {
        super(network, edge);
        ac = AC.getValue(edgeRow);
        type = TYPE.getValue(edgeRow);
        typeMIId = new OntologyIdentifier(TYPE_MI_ID.getValue(edgeRow), SourceOntology.MI);
        id = ID.getValue(edgeRow);
        interactionDetectionMethod = INTERACTION_DETECTION_METHOD.getValue(edgeRow);
        interactionDetectionMethodMIId = new OntologyIdentifier(INTERACTION_DETECTION_METHOD_MI_ID.getValue(edgeRow), SourceOntology.MI);
        participantDetectionMethod = PARTICIPANT_DETECTION_METHOD.getValue(edgeRow);
        participantDetectionMethodMIId = new OntologyIdentifier(PARTICIPANT_DETECTION_METHOD_MI_ID.getValue(edgeRow), SourceOntology.MI);
        hostOrganism = HOST_ORGANISM.getValue(edgeRow);
        hostOrganismTaxId = HOST_ORGANISM_ID.getValue(edgeRow);
        expansionType = EXPANSION_TYPE.getValue(edgeRow);
        sourceBiologicalRole = SOURCE_BIOLOGICAL_ROLE.getValue(edgeRow);
        targetBiologicalRole = TARGET_BIOLOGICAL_ROLE.getValue(edgeRow);
        pubMedId = PUBMED_ID.getValue(edgeRow);
        collapsed = false;
    }


}