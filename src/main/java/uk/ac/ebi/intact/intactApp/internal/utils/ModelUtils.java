package uk.ac.ebi.intact.intactApp.internal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.*;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm.TermCategory;

import java.awt.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelUtils {

    // Namespaces
    public static String INTACTDB_NAMESPACE = "intactdb";
    public static String STYLE_NAMESPACE = "style";
    public static String COLLAPSED_NAMESPACE = "collapsed";
    public static String NAMESPACE_SEPARATOR = "::";

    // Node information
    public static String CANONICAL = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "canonical name";
    public static String DISPLAY = "display name";
    public static String CV_STYLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "chemViz Passthrough";
    public static String ELABEL_STYLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "enhancedLabel Passthrough";
    public static String ID = "@id";


    public static String INTACT_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "id";
    public static String PREFERRED_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "preferred id";
    public static String PREFERRED_ID_DB = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "preferred id database";
    public static String TAX_ID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "tax id";
    public static String MUTATION = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "mutation";

    public static String DETECTION_METHOD = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "detection method";
    public static String DISRUPTED_BY_MUTATION = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "disrupted by mutation";
    public static String MI_SCORE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "mi score";
    public static String SHAPE = STYLE_NAMESPACE + NAMESPACE_SEPARATOR + "shape";
    public static String COLOR = STYLE_NAMESPACE + NAMESPACE_SEPARATOR + "color";
    public static String SOURCE_SHAPE = STYLE_NAMESPACE + NAMESPACE_SEPARATOR + "source shape";
    public static String TARGET_SHAPE = STYLE_NAMESPACE + NAMESPACE_SEPARATOR + "target shape";
    public static String C_COLOR = STYLE_NAMESPACE + NAMESPACE_SEPARATOR + "collapsed color";
    public static String C_IS_COLLAPSED = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "is collapsed";
    public static String C_INTACT_IDS = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "intact ids";
    public static String C_MI_SCORE = COLLAPSED_NAMESPACE + NAMESPACE_SEPARATOR + "mi score";


    public static String DESCRIPTION = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "description";
    public static String QUERYTERM = "query term";
    public static String SPECIES = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "species";
    public static String STRINGID = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "database identifier";
    public static String STYLE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "STRING style";
    public static String TYPE = INTACTDB_NAMESPACE + NAMESPACE_SEPARATOR + "node type";

    public static String TISSUE_NAMESPACE = "tissue";
    public static String COMPARTMENT_NAMESPACE = "compartment";
    // public static String TM_LINKOUT = "TextMining Linkout";
    public static List<String> ignoreKeys = new ArrayList<>(Arrays.asList("image", "canonical", "@id", "description",
            "id", "preferred_id", "preferred_id_db", "interactor_type", "species", "interactor_name", "label", "tax_id", "mutation",
            "source", "target", "interaction_ac", "interaction_detection_method", "interaction_type", "mi_score", "disrupted_by_mutation",
            "shape", "color", "collapsed_color"));
    public static List<String> namespacedNodeAttributes = new ArrayList<>(Arrays.asList("canonical name", "full name", "chemViz Passthrough",
            "enhancedLabel Passthrough", "description", "disease score", "namespace", "sequence", "smiles", "species", "database identifier",
            "STRING style", "node type", "textmining foreground", "textmining background", "textmining score"));

    //public static Pattern cidmPattern = Pattern.compile("\\(CIDm\\)0*");
    public static Pattern cidmPattern = Pattern.compile("CIDm0*");
    // public static String DISEASEINFO =
    // "http://diseases.jensenlab.org/Entity?type1=9606&type2=-26";

    public static List<String> namespacedEdgeAttributes = new ArrayList<>(Arrays.asList("score", "interspecies", "experiments", "cooccurrence",
            "coexpression", "textmining", "databases", "neighborhood"));

    // Network information
    public static String CONFIDENCE = "confidence score";
    public static String DATABASE = "database";
    public static String NET_SPECIES = "species";
    public static String NET_DATAVERSION = "data version";
    public static String NET_URI = "uri";
    public static String NET_ENRICHMENT_SETTINGS = "enrichmentSettings";

    public static String showEnhancedLabelsFlag = "showEnhancedLabels";

    // Create network view size threshold
    // See https://github.com/cytoscape/cytoscape-impl/blob/develop/core-task-impl/
    // src/main/java/org/cytoscape/task/internal/loadnetwork/AbstractLoadNetworkTask.java
    public static int DEF_VIEW_THRESHOLD = 3000;
    public static String VIEW_THRESHOLD = "viewThreshold";

    // Other stuff
    public static String COMPOUND = "STITCH compounds";
    public static String EMPTYLINE = "--------";

    public static String REQUERY_MSG_USER =
            "<html>This action cannot be performed on the current network as it <br />"
                    + "appears to be an old STRING network. Would you like to get <br />"
                    + "the latest STRING network for the nodes in your network?</html>";
    public static String REQUERY_TITLE = "Re-query network?";

    public static boolean haveQueryTerms(CyNetwork network) {
        if (network == null) return false;
        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(QUERYTERM, String.class) != null)
                return true;
        }
        return false;
    }

    public static void selectQueryTerms(CyNetwork network) {
        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(QUERYTERM, String.class) != null)
                network.getRow(node).set(CyNetwork.SELECTED, true);
            else
                network.getRow(node).set(CyNetwork.SELECTED, false);
        }
    }

    public static List<String> getCompartmentList(CyNetwork network) {
        List<String> compartments = new ArrayList<>();
        if (network == null) {
            return compartments;
        }
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(COMPARTMENT_NAMESPACE);
        if (columns == null || columns.size() == 0) return compartments;
        for (CyColumn col : columns) {
            compartments.add(col.getNameOnly());
        }
        return compartments;
    }

    public static List<CyNode> augmentNetworkFromJSON(IntactManager manager, CyNetwork net,
                                                      List<CyEdge> newEdges, JsonNode object, Map<String, String> queryTermMap,
                                                      String useDATABASE) {
        //TODO Make augmentNetworkFromJson
//        JsonNode results = getResultsFromJSON(object);
//        if (results == null)
//            return null;
//
//        Map<String, CyNode> nodeMap = new HashMap<>();
//        Map<String, String> nodeNameMap = new HashMap<>();
//        String species = ModelUtils.getNetSpecies(net);
//        // TODO: Check if we really don't have to infer the database!
//
//        for (CyNode node : net.getNodeList()) {
//            if (species == null)
//                species = net.getRow(node).get(SPECIES, String.class);
//            String stringId = net.getRow(node).get(STRINGID, String.class);
//            if (stringId == null)
//                continue; // Could be merged from another network
//            String name = net.getRow(node).get(CyNetwork.NAME, String.class);
//            nodeMap.put(stringId, node);
//            nodeNameMap.put(stringId, name);
//            // TODO: Change network from string to stitch once we add compounds?
//            if (isCompound(net, node))
//                useDATABASE = Databases.STITCH.getAPIName();
//        }
//        setDatabase(net, useDATABASE);
//
//        List<CyNode> nodes = getJSON(manager, species, net, nodeMap, nodeNameMap, queryTermMap,
//                newEdges, results, useDATABASE);
//        return nodes;
        return null;
    }

    public static void setConfidence(CyNetwork network, double confidence) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), Double.class, CONFIDENCE);
        network.getRow(network).set(CONFIDENCE, confidence);
    }

    public static Double getConfidence(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(CONFIDENCE) == null)
            return null;
        return network.getRow(network).get(CONFIDENCE, Double.class);
    }

    public static void setDatabase(CyNetwork network, String database) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, DATABASE);
        network.getRow(network).set(DATABASE, database);
    }

    public static String getDatabase(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(DATABASE) == null)
            return null;
        return network.getRow(network).get(DATABASE, String.class);
    }

    public static void setDataVersion(CyNetwork network, String dataVersion) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, NET_DATAVERSION);
        network.getRow(network).set(NET_DATAVERSION, dataVersion);
    }

    public static String getDataVersion(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(NET_DATAVERSION) == null)
            return null;
        return network.getRow(network).get(NET_DATAVERSION, String.class);
    }

    public static void setNetURI(CyNetwork network, String netURI) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, NET_URI);
        network.getRow(network).set(NET_URI, netURI);
    }

    public static void setNetSpecies(CyNetwork network, String species) {
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, NET_SPECIES);
        network.getRow(network).set(NET_SPECIES, species);
    }

    public static String getNetSpecies(CyNetwork network) {
        if (network.getDefaultNetworkTable().getColumn(NET_SPECIES) == null)
            return null;
        return network.getRow(network).get(NET_SPECIES, String.class);
    }

    public static String getMostCommonNetSpecies(CyNetwork net) {
        Map<String, Integer> species = new HashMap<>();
        for (CyNode node : net.getNodeList()) {
            String nSpecies = net.getRow(node).get(SPECIES, String.class);
            if (nSpecies == null || nSpecies.equals(""))
                continue;
            if (!species.containsKey(nSpecies)) {
                species.put(nSpecies, 1);
            } else {
                int count = species.get(nSpecies) + 1;
                species.put(nSpecies, count);
            }
        }
        String netSpecies = "";
        int maxCount = 0;
        for (Map.Entry<String, Integer> tempSp : species.entrySet()) {
            if (netSpecies.equals("") || tempSp.getValue() > maxCount) {
                netSpecies = tempSp.getKey();
                maxCount = tempSp.getValue();
            }
        }
        return netSpecies;
    }

    public static List<String> getAllNetSpecies(CyNetwork net) {
        List<String> species = new ArrayList<>();
        for (CyNode node : net.getNodeList()) {
            String nSpecies = net.getRow(node).get(SPECIES, String.class);
            if (nSpecies != null && !nSpecies.equals("") && !species.contains(nSpecies))
                species.add(nSpecies);
        }
        return species;
    }

    public static List<String> getEnrichmentNetSpecies(CyNetwork net) {
        List<String> species = new ArrayList<>();
        for (CyNode node : net.getNodeList()) {
            String nSpecies = net.getRow(node).get(SPECIES, String.class);
            if (nSpecies != null && !nSpecies.equals("") && !species.contains(nSpecies)) {
                Species theSpecies = Species.getSpecies(nSpecies);
                // TODO: This is kind of a hack for now and will be updated once we get the kingdom data from the server
                if (theSpecies != null && (theSpecies.getType().equals("core") || theSpecies.getType().equals("periphery")))
                    species.add(nSpecies);
            }
        }
        return species;
    }

    public static String formatForColumnNamespace(String columnName) {
        String formattedColumnName = columnName;
        if (columnName.contains("::")) {
            if (columnName.startsWith(INTACTDB_NAMESPACE))
                formattedColumnName = columnName.substring(INTACTDB_NAMESPACE.length() + 2);
            else
                formattedColumnName = columnName.replaceFirst("::", " ");
        }
        return formattedColumnName;
    }

    public static boolean isMergedIntactNetwork(CyNetwork network) {
        CyTable nodeTable = network.getDefaultNodeTable();
        if (nodeTable.getColumn(INTACT_ID) == null)
            return false;
        // Enough to check for id in the node columns and score in the edge columns
        //if (nodeTable.getColumn(SPECIES) == null)
        //	return false;
        //if (nodeTable.getColumn(CANONICAL) == null)
        //	return false;
        CyTable edgeTable = network.getDefaultEdgeTable();
        return edgeTable.getColumn(MI_SCORE) != null;
    }

    public static boolean isIntactNetwork(CyNetwork network) {
        return isMergedIntactNetwork(network);
    }

    // This method will tell us if we have the new side panel functionality (i.e. namespaces)
    public static boolean ifHaveIntactNS(CyNetwork network) {
        if (network == null) return false;
        CyRow netRow = network.getRow(network);
        Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns(INTACTDB_NAMESPACE);
        return columns != null && columns.size() > 0;
    }

    public static boolean isCurrentDataVersion(CyNetwork network) {
        return network != null && network.getRow(network).get(NET_DATAVERSION, String.class) != null
                && network.getRow(network).get(NET_DATAVERSION, String.class)
                .equals(IntactManager.DATAVERSION);
    }

    public static boolean isStitchNetwork(CyNetwork network) {
        return false;
    }

    public static String getExisting(CyNetwork network) {
        StringBuilder str = new StringBuilder();
        for (CyNode node : network.getNodeList()) {
            String stringID = network.getRow(node).get(STRINGID, String.class);
            if (stringID != null && stringID.length() > 0)
                str.append(stringID).append("\n");
        }
        return str.toString();
    }

    public static String getSelected(CyNetwork network, View<CyNode> nodeView) {
        StringBuilder selectedStr = new StringBuilder();
        if (nodeView != null) {
            String stringID = network.getRow(nodeView.getModel()).get(STRINGID, String.class);
            selectedStr.append(stringID).append("\n");
        }

        for (CyNode node : network.getNodeList()) {
            if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                String stringID = network.getRow(node).get(STRINGID, String.class);
                if (stringID != null && stringID.length() > 0)
                    selectedStr.append(stringID).append("\n");
            }
        }
        return selectedStr.toString();
    }

    public static boolean isCompound(CyNetwork net, CyNode node) {
        if (net == null || node == null)
            return false;

        String ns = net.getRow(node).get(ID, String.class);
        return getType(ns).equals("compound");
    }

    private static String getType(String id) {
        // Get the namespace
        String namespace = id.substring(0, id.indexOf(":"));
        if (namespace.equals("stringdb"))
            return "protein";
        if (namespace.equals("stitchdb"))
            return "compound";
        return "unknown";
    }

    public static void createColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;

        table.createColumn(columnName, clazz, false);
    }

    public static void replaceColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createColumn(columnName, clazz, false);
    }

    public static void createListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            return;

        table.createListColumn(columnName, clazz, false);
    }

    public static void replaceListColumnIfNeeded(CyTable table, Class<?> clazz, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);

        table.createListColumn(columnName, clazz, false);
    }

    public static void deleteColumnIfExisting(CyTable table, String columnName) {
        if (table.getColumn(columnName) != null)
            table.deleteColumn(columnName);
    }

    public static String getName(CyNetwork network, CyIdentifiable ident) {
        return getString(network, ident, CyNetwork.NAME);
    }

    public static String getString(CyNetwork network, CyIdentifiable ident, String column) {
        // System.out.println("network = "+network+", ident = "+ident+" column = "+column);
        if (network.getRow(ident, CyNetwork.DEFAULT_ATTRS) != null)
            return network.getRow(ident, CyNetwork.DEFAULT_ATTRS).get(column, String.class);
        return null;
    }

    public static List<String> getAvailableInteractionPartners(CyNetwork network) {
        List<String> availableTypes = new ArrayList<>();
        List<String> species = ModelUtils.getAllNetSpecies(network);
        Collections.sort(species);
        String netSp = getNetSpecies(network);
        if (netSp != null && !species.contains(netSp)) {
            availableTypes.add(netSp);
        }
        availableTypes.addAll(species);
        availableTypes.add(COMPOUND);
        List<String> spPartners = new ArrayList<>();
        for (String sp : species) {
            List<String> partners = Species.getSpeciesPartners(sp);
            for (String spPartner : partners) {
                if (!species.contains(spPartner))
                    spPartners.add(spPartner);
            }
        }
        Collections.sort(spPartners);
        if (spPartners.size() > 0) {
            availableTypes.add(EMPTYLINE);
            availableTypes.addAll(spPartners);
        }
        return availableTypes;
    }


    public static JsonNode getResultsFromJSON(JsonNode json) {
        if (json == null || !json.has(IntactManager.RESULT))
            return null;

        return json.get(IntactManager.RESULT);
    }

    public static Integer getVersionFromJSON(JsonNode json) {
        if (json == null || !json.has(IntactManager.APIVERSION))
            return null;
        return json.get(IntactManager.APIVERSION).intValue();
    }

    public static Set<CyTable> getEnrichmentTables(IntactManager manager, CyNetwork network) {
        CyTableManager tableManager = manager.getService(CyTableManager.class);
        Set<CyTable> netTables = new HashSet<>();
        Set<String> tableNames = new HashSet<>(TermCategory.getTables());
        Set<CyTable> currTables = tableManager.getAllTables(true);
        for (CyTable current : currTables) {
            if (tableNames.contains(current.getTitle())
                    && current.getColumn(EnrichmentTerm.colNetworkSUID) != null
                    && current.getAllRows().size() > 0) {
                CyRow tempRow = current.getAllRows().get(0);
                if (tempRow.get(EnrichmentTerm.colNetworkSUID, Long.class) != null && tempRow
                        .get(EnrichmentTerm.colNetworkSUID, Long.class).equals(network.getSUID())) {
                    netTables.add(current);
                }
            }
        }
        return netTables;
    }

    public static CyTable getEnrichmentTable(IntactManager manager, CyNetwork network, String name) {
        CyTableManager tableManager = manager.getService(CyTableManager.class);
        Set<CyTable> currTables = tableManager.getAllTables(true);
        for (CyTable current : currTables) {
            if (name.equals(current.getTitle())
                    && current.getColumn(EnrichmentTerm.colNetworkSUID) != null
                    && current.getAllRows().size() > 0) {
                CyRow tempRow = current.getAllRows().get(0);
                if (tempRow.get(EnrichmentTerm.colNetworkSUID, Long.class) != null && tempRow
                        .get(EnrichmentTerm.colNetworkSUID, Long.class).equals(network.getSUID())) {
                    return current;
                }
            }
        }
        return null;
    }


    public static void deleteEnrichmentTables(CyNetwork network, IntactManager manager, boolean publOnly) {
        CyTableManager tableManager = manager.getService(CyTableManager.class);
        Set<CyTable> oldTables = ModelUtils.getEnrichmentTables(manager, network);
        for (CyTable table : oldTables) {
            if (publOnly && !table.getTitle().equals(TermCategory.PMID.getTable())) {
                continue;
            }
            tableManager.deleteTable(table.getSUID());
            manager.flushEvents();
        }
    }

    public static void setupEnrichmentTable(CyTable enrichmentTable) {
        if (enrichmentTable.getColumn(EnrichmentTerm.colGenesSUID) == null) {
            enrichmentTable.createListColumn(EnrichmentTerm.colGenesSUID, Long.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colNetworkSUID) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colNetworkSUID, Long.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colName) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colName, String.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colYear) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colYear, Integer.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colIDPubl) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colIDPubl, String.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colDescription) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colDescription, String.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colCategory) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colCategory, String.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colFDR) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colFDR, Double.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colGenesBG) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colGenesBG, Integer.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colGenesCount) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colGenesCount, Integer.class, false);
        }
        if (enrichmentTable.getColumn(EnrichmentTerm.colGenes) == null) {
            enrichmentTable.createListColumn(EnrichmentTerm.colGenes, String.class, false);
        }
        // if (table.getColumn(EnrichmentTerm.colShowChart) == null) {
        //	table.createColumn(EnrichmentTerm.colShowChart, Boolean.class, false);
        // }
        if (enrichmentTable.getColumn(EnrichmentTerm.colChartColor) == null) {
            enrichmentTable.createColumn(EnrichmentTerm.colChartColor, String.class, false);
        }
        // table.createColumn(EnrichmentTerm.colPvalue, Double.class, false);
        // table.createColumn(EnrichmentTerm.colBonferroni, Double.class, false);
    }


    public static void setStringProperty(CyProperty<Properties> properties,
                                         String propertyKey, Object propertyValue) {
        Properties p = properties.getProperties();
        p.setProperty(propertyKey, propertyValue.toString());
    }

    public static boolean hasProperty(CyProperty<Properties> properties, String propertyKey) {
        Properties p = properties.getProperties();
        return p.getProperty(propertyKey) != null;
    }

    public static String getStringProperty(CyProperty<Properties> properties, String propertyKey) {
        Properties p = properties.getProperties();
        if (p.getProperty(propertyKey) != null)
            return p.getProperty(propertyKey);
        return null;
    }

    public static Double getDoubleProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Double.valueOf(value);
    }

    public static Integer getIntegerProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Integer.valueOf(value);
    }

    public static Boolean getBooleanProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = ModelUtils.getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Boolean.valueOf(value);
    }

    public static String listToString(List<?> list) {
        StringBuilder str = new StringBuilder();
        if (list == null || list.size() == 0) return str.toString();
        for (int i = 0; i < list.size() - 1; i++) {
            str.append(list.get(i)).append(",");
        }
        return str + list.get(list.size() - 1).toString();
    }

    public static List<String> stringToList(String string) {
        if (string == null || string.length() == 0) return new ArrayList<>();
        String[] arr = string.split(",");
        return Arrays.asList(arr);
    }

    public static void updateEnrichmentSettings(CyNetwork network, Map<String, String> settings) {
        StringBuilder setting = new StringBuilder();
        int index = 0;
        for (String key : settings.keySet()) {
            if (index > 0) {
                setting.append(";");
            }
            setting.append(key).append("=").append(settings.get(key));
            index++;
        }
        createColumnIfNeeded(network.getDefaultNetworkTable(), String.class, NET_ENRICHMENT_SETTINGS);
        network.getRow(network).set(NET_ENRICHMENT_SETTINGS, setting.toString());
    }

    public static Map<String, String> getEnrichmentSettings(CyNetwork network) {
        Map<String, String> settings = new HashMap<>();
        String setting = network.getRow(network).get(NET_ENRICHMENT_SETTINGS, String.class);
        if (setting == null || setting.length() == 0)
            return settings;

        String[] settingArray = setting.split(";");
        for (String s : settingArray) {
            String[] pair = s.split("=");
            if (pair.length == 2) {
                settings.put(pair[0], pair[1]);
            }
        }
        return settings;
    }

    public static CyProperty<Properties> getPropertyService(IntactManager manager,
                                                            SavePolicy policy) {
        String name = "stringApp";
        if (policy.equals(SavePolicy.SESSION_FILE)) {
            CyProperty<Properties> service = manager.getService(CyProperty.class, "(cyPropertyName=" + name + ")");
            // Do we already have a session with our properties
            if (service.getSavePolicy().equals(SavePolicy.SESSION_FILE))
                return service;

            // Either we have a null session or our properties aren't in this session
            Properties props = new Properties();
            service = new SimpleCyProperty<>(name, props, Properties.class, SavePolicy.SESSION_FILE);
            Properties serviceProps = new Properties();
            serviceProps.setProperty("cyPropertyName", service.getName());
            manager.registerAllServices(service, serviceProps);
            return service;
        } else if (policy.equals(SavePolicy.CONFIG_DIR) || policy.equals(SavePolicy.SESSION_FILE_AND_CONFIG_DIR)) {
            CyProperty<Properties> service = new ConfigPropsReader(policy, name);
            Properties serviceProps = new Properties();
            serviceProps.setProperty("cyPropertyName", service.getName());
            manager.registerAllServices(service, serviceProps);
            return service;
        }
        return null;
    }

    public static int getViewThreshold(IntactManager manager) {
        final Properties props = (Properties) manager
                .getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
        final String vts = props.getProperty(VIEW_THRESHOLD);
        int threshold;

        try {
            threshold = Integer.parseInt(vts);
        } catch (Exception e) {
            threshold = DEF_VIEW_THRESHOLD;
        }

        return threshold;
    }

    // Method to convert terms entered in search text to
    // appropriate newline-separated string to send to server
    public static String convertTerms(String terms, boolean splitComma, boolean splitSpaces) {
        String regexSp = "\\s+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
        String regexComma = "[,]+(?=((\\\\[\\\\\"]|[^\\\\\"])*\"(\\\\[\\\\\"]|[^\\\\\"])*\")*(\\\\[\\\\\"]|[^\\\\\"])*$)";
        if (splitSpaces) {
            // Substitute newlines for space
            terms = terms.replaceAll(regexSp, "\n");
        }

        if (splitComma) {
            // Substitute newlines for commas
            terms = terms.replaceAll(regexComma, "\n");
        }

        // Strip off any blank lines
        terms = terms.replaceAll("(?m)^\\s*", "");
        return terms;
    }

    public static void copyRow(CyTable fromTable, CyTable toTable, CyIdentifiable from, CyIdentifiable to, List<String> columnsCreated) {
        for (CyColumn col : fromTable.getColumns()) {
            if (!columnsCreated.contains(col.getName()))
                continue;
            if (col.getName().equals(CyNetwork.SUID))
                continue;
            if (col.getName().equals(CyNetwork.NAME))
                continue;
            if (col.getName().equals(CyNetwork.SELECTED))
                continue;
            if (col.getName().equals(CyRootNetwork.SHARED_NAME))
                continue;
            if (from.getClass().equals(CyEdge.class) && col.getName().equals(CyRootNetwork.SHARED_INTERACTION))
                continue;
            if (from.getClass().equals(CyEdge.class) && col.getName().equals(CyEdge.INTERACTION))
                continue;
            Object v = fromTable.getRow(from.getSUID()).getRaw(col.getName());
            toTable.getRow(to.getSUID()).set(col.getName(), v);
        }
    }

    public static void createNodeMap(CyNetwork network, Map<String, CyNode> nodeMap, String column) {
        // Get all of the nodes in the network
        for (CyNode node : network.getNodeList()) {
            String key = network.getRow(node).get(column, String.class);
            nodeMap.put(key, node);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> copyColumns(CyTable fromTable, CyTable toTable) {
        List<String> columns = new ArrayList<>();
        for (CyColumn col : fromTable.getColumns()) {
            String fqn = col.getName();
            // Does that column already exist in our target?
            if (toTable.getColumn(fqn) == null) {
                // No, create it.
                if (col.getType().equals(List.class)) {
                    // There is no easy way to handle this, unfortunately...
                    // toTable.createListColumn(fqn, col.getListElementType(), col.isImmutable(), (List<?>)col.getDefaultValue());
                    if (col.getListElementType().equals(String.class))
                        toTable.createListColumn(fqn, String.class, col.isImmutable(),
                                (List<String>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Long.class))
                        toTable.createListColumn(fqn, Long.class, col.isImmutable(),
                                (List<Long>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Double.class))
                        toTable.createListColumn(fqn, Double.class, col.isImmutable(),
                                (List<Double>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Integer.class))
                        toTable.createListColumn(fqn, Integer.class, col.isImmutable(),
                                (List<Integer>) col.getDefaultValue());
                    else if (col.getListElementType().equals(Boolean.class))
                        toTable.createListColumn(fqn, Boolean.class, col.isImmutable(),
                                (List<Boolean>) col.getDefaultValue());
                } else {
                    toTable.createColumn(fqn, col.getType(), col.isImmutable(), col.getDefaultValue());
                    columns.add(fqn);
                }
            }
        }
        return columns;
    }

    public static void copyNodePositions(IntactManager manager, CyNetwork from, CyNetwork to,
                                         Map<String, CyNode> nodeMap, String column) {
        CyNetworkView fromView = getNetworkView(manager, from);
        CyNetworkView toView = getNetworkView(manager, to);
        for (View<CyNode> nodeView : fromView.getNodeViews()) {
            // Get the to node
            String nodeKey = from.getRow(nodeView.getModel()).get(column, String.class);
            if (!nodeMap.containsKey(nodeKey))
                continue;
            View<CyNode> toNodeView = toView.getNodeView(nodeMap.get(nodeKey));
            // Copy over the positions
            Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            Double z = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION);
            toNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
            toNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
            if (z != null && z != 0.0)
                toNodeView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, z);
        }
    }

    public static void copyEdges(CyNetwork fromNetwork, CyNetwork toNetwork,
                                 Map<String, CyNode> nodeMap, String column) {
        List<String> columnsCreated = copyColumns(fromNetwork.getDefaultEdgeTable(), toNetwork.getDefaultEdgeTable());
        List<CyEdge> edgeList = fromNetwork.getEdgeList();
        for (CyEdge edge : edgeList) {
            CyNode sourceNode = edge.getSource();
            CyNode targetNode = edge.getTarget();
            boolean isDirected = edge.isDirected();

            String source = fromNetwork.getRow(sourceNode).get(column, String.class);
            String target = fromNetwork.getRow(targetNode).get(column, String.class);

            if (!nodeMap.containsKey(source) || !nodeMap.containsKey(target))
                continue;

            CyNode newSource = nodeMap.get(source);
            CyNode newTarget = nodeMap.get(target);

            CyEdge newEdge = toNetwork.addEdge(newSource, newTarget, isDirected);
            copyRow(fromNetwork.getDefaultEdgeTable(), toNetwork.getDefaultEdgeTable(), edge, newEdge, columnsCreated);
        }
    }

    public static CyNetworkView getNetworkView(IntactManager manager, CyNetwork network) {
        Collection<CyNetworkView> views =
                manager.getService(CyNetworkViewManager.class).getNetworkViews(network);

        // At some point, figure out a better way to do this
        for (CyNetworkView view : views) {
            return view;
        }
        return null;
    }

    public static void copyNodeAttributes(CyNetwork from, CyNetwork to,
                                          Map<String, CyNode> nodeMap, String column) {
        // System.out.println("copyNodeAttributes");
        List<String> columnsCreated = copyColumns(from.getDefaultNodeTable(), to.getDefaultNodeTable());
        for (CyNode node : from.getNodeList()) {
            String nodeKey = from.getRow(node).get(column, String.class);
            if (!nodeMap.containsKey(nodeKey))
                continue;
            CyNode newNode = nodeMap.get(nodeKey);
            copyRow(from.getDefaultNodeTable(), to.getDefaultNodeTable(), node, newNode, columnsCreated);
        }
    }

    public static class ConfigPropsReader extends AbstractConfigDirPropsReader {
        ConfigPropsReader(SavePolicy policy, String name) {
            super(name, "stringApp.props", policy);
        }
    }


    /////////////////////////////////////////////////////////////////////


    public static Map<String, List<JsonNode>> groupByJSON(JsonNode toSplit, String keyGroup) {
        Map<String, List<JsonNode>> groups = new HashMap<>();
        if (keyGroup == null || keyGroup.equals("")) {
            keyGroup = "group";
        }

        for (JsonNode element : toSplit) {
            String group = element.get(keyGroup).textValue();
            if (!groups.containsKey(group)) {
                List<JsonNode> elementsOfGroup = new ArrayList<>();
                elementsOfGroup.add(element.get("data"));
                groups.put(group, elementsOfGroup);
            } else {
                groups.get(group).add(element.get("data"));
            }
        }

        return groups;
    }

    private static Pattern rgbPattern = Pattern.compile("rgb\\((\\d+), ?(\\d+), ?(\\d+) ?\\)");

    public static Color parseColorRGB(String rgbColor) {
        Matcher m = rgbPattern.matcher(rgbColor);
        if (m.find()) {
            return new Color(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    public static CyNetwork createIntactNetworkFromJSON(IntactNetwork intactNetwork, String species,
                                                        JsonNode object, Map<String, String> queryTermMap, String netName,
                                                        String useDATABASE) {
        intactNetwork.getManager().ignoreAdd();
        CyNetwork network = createIntactNetworkFromJSON(intactNetwork.getManager(), species, object, queryTermMap, netName, useDATABASE);
        intactNetwork.getManager().listenToAdd();
        intactNetwork.getManager().showResultsPanel();
        return network;
    }

    private static CyNetwork createIntactNetworkFromJSON(IntactManager manager, String species,
                                                         JsonNode object, Map<String, String> queryTermMap, String netName,
                                                         String useDATABASE) {
        JsonNode results = getResultsFromJSON(object);
        if (results == null)
            return null;

        // Get a network name
        String defaultName = "IntAct Network";
        if (netName != null && !netName.equals("")) {
            netName = defaultName + " - " + netName;
        } else if (queryTermMap != null && queryTermMap.size() == 1) {
            netName = defaultName + " - " + queryTermMap.values().iterator().next();
        } else {
            netName = defaultName;
        }

        // Create the network
        CyNetwork newNetwork = manager.createNetwork(netName);
        setDatabase(newNetwork, useDATABASE);
        setNetSpecies(newNetwork, species);

        // Create a map to save the nodes
        Map<String, CyNode> nodeMap = new HashMap<>();

        // Create a map to save the node names
        Map<String, String> nodeNameMap = new HashMap<>();

        loadJSON(newNetwork, nodeMap, nodeNameMap, null, null);

        manager.addNetwork(newNetwork);
        return newNetwork;
    }

    public static List<CyNode> loadJSON(CyNetwork network, Map<String, CyNode> nodeMap, Map<String, String> nodeNameMap, List<CyEdge> newEdges, JsonNode json) {
        try {
            List<CyNode> newNodes = new ArrayList<>();

            List<String> intactNodeColumns = new ArrayList<>(Arrays.asList(INTACT_ID, PREFERRED_ID, PREFERRED_ID_DB, TYPE, SPECIES, SHAPE, COLOR));
            for (String intactNodeColumn : intactNodeColumns) {
                createColumnIfNeeded(network.getDefaultNodeTable(), String.class, intactNodeColumn);
            }
            createColumnIfNeeded(network.getDefaultNodeTable(), Long.class, TAX_ID);
            createColumnIfNeeded(network.getDefaultNodeTable(), Boolean.class, MUTATION);


            List<String> intactEdgeColumns = new ArrayList<>(Arrays.asList(INTACT_ID, DETECTION_METHOD));
            for (String intactEdgeColumn : intactEdgeColumns) {
                createColumnIfNeeded(network.getDefaultEdgeTable(), String.class, intactEdgeColumn);
            }
            createColumnIfNeeded(network.getDefaultEdgeTable(), Double.class, MI_SCORE);
            createColumnIfNeeded(network.getDefaultEdgeTable(), Boolean.class, DISRUPTED_BY_MUTATION);
            createColumnIfNeeded(network.getDefaultEdgeTable(), Boolean.class, C_IS_COLLAPSED);
            createColumnIfNeeded(network.getDefaultEdgeTable(), Double.class, C_MI_SCORE);
            createListColumnIfNeeded(network.getDefaultEdgeTable(), String.class, C_INTACT_IDS);


            intactEdgeColumns = new ArrayList<>(Arrays.asList(C_COLOR, COLOR, SHAPE, SOURCE_SHAPE, TARGET_SHAPE));
            for (String intactEdgeColumn : intactEdgeColumns) {
                createColumnIfNeeded(network.getDefaultEdgeTable(), String.class, intactEdgeColumn);
            }


            URL resource = Species.class.getResource("/getInteractions.json");
            InputStream stream = resource.openConnection().getInputStream();
            JsonNode fixedJSON = new ObjectMapper().readTree(new InputStreamReader(stream));

            Map<String, List<JsonNode>> groups = ModelUtils.groupByJSON(fixedJSON, "group");
            List<JsonNode> nodesJSON = groups.get("nodes");
            List<JsonNode> edgesJSON = groups.get("edges");

            if (nodesJSON.size() > 0) {
                createColumnsFromIntactJSON(nodesJSON, network.getDefaultNodeTable());
                for (JsonNode node : nodesJSON) {
                    CyNode newNode = createIntactNode(network, node, nodeMap, nodeNameMap);
                    if (newNode != null)
                        newNodes.add(newNode);
                }
            }
            if (edgesJSON.size() > 0) {
                createColumnsFromIntactJSON(edgesJSON, network.getDefaultEdgeTable());
                for (JsonNode edge : edgesJSON) {
                    createIntactEdge(network, edge, nodeMap, nodeNameMap, newEdges);
                }
            }

            return newNodes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createColumnsFromIntactJSON(List<JsonNode> nodes, CyTable table) {
        Map<String, Class<?>> jsonKeysClass = new HashMap<>();
        Set<String> listKeys = new HashSet<>();
        for (JsonNode nodeJSON : nodes) {
            nodeJSON.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                if (jsonKeysClass.containsKey(key)) {
                    return;
                }
                JsonNode value = entry.getValue();
                if (value.isArray()) {
                    jsonKeysClass.put(key, getJsonNodeValueClass(value.get(0)));
                    listKeys.add(key);
                } else {
                    jsonKeysClass.put(key, getJsonNodeValueClass(value));
                }
            });
        }
        List<String> jsonKeysSorted = new ArrayList<>(jsonKeysClass.keySet());
        Collections.sort(jsonKeysSorted);
        for (String jsonKey : jsonKeysSorted) {
            if (ignoreKeys.contains(jsonKey))
                continue;
            if (listKeys.contains(jsonKey)) {
                createListColumnIfNeeded(table, jsonKeysClass.get(jsonKey), jsonKey);
            } else {
                createColumnIfNeeded(table, jsonKeysClass.get(jsonKey), jsonKey);
            }
        }
    }

    private static Class<?> getJsonNodeValueClass(JsonNode valueNode) {
        if (valueNode.isBoolean())
            return Boolean.class;
        else if (valueNode.isDouble())
            return Double.class;
        else if (valueNode.isLong())
            return Long.class;
        else if (valueNode.isInt())
            return Integer.class;
        else if (valueNode.isTextual())
            return String.class;
        return String.class;
    }

    private static Object getJsonNodeValue(JsonNode valueNode) {
        if (valueNode.isBoolean())
            return valueNode.booleanValue();
        else if (valueNode.isDouble())
            return valueNode.booleanValue();
        else if (valueNode.isLong())
            return valueNode.longValue();
        else if (valueNode.isInt())
            return valueNode.intValue();
        else if (valueNode.isTextual())
            return valueNode.textValue();
        return valueNode.asText();
    }


    private static CyNode createIntactNode(CyNetwork network, JsonNode nodeJSON,
                                           Map<String, CyNode> nodeMap, Map<String, String> nodeNameMap) {

        String intactId = nodeJSON.get("id").textValue();

        if (nodeMap.containsKey(intactId))
            return null;

        CyNode newNode = network.addNode();
        CyRow row = network.getRow(newNode);

        nodeJSON.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            switch (entry.getKey()) {
                case "id":
                    row.set(INTACT_ID, intactId);
                    break;
                case "interactor_name":
                    row.set(CyNetwork.NAME, value.textValue());
                    break;
                case "interactor_type":
                    row.set(TYPE, value.textValue());
                    break;
                case "species":
                    row.set(SPECIES, value.textValue());
                    break;
                case "preferred_id":
                    row.set(PREFERRED_ID, value.textValue());
                    break;
                case "preferred_id_db":
                    row.set(PREFERRED_ID_DB, value.textValue().split("[()]")[1]);
                    break;
                case "tax_id":
                    row.set(TAX_ID, value.longValue());
                    break;
                case "mutation":
                    row.set(MUTATION, value.booleanValue());
                    break;
                case "shape":
                    row.set(SHAPE, value.textValue().replaceAll("-", "_"));
                    break;
                case "color":
                    row.set(COLOR, cleanJSONColorData(value.textValue()));
                    break;
                case "label":
                case "parent":
                    return;
                default:
                    row.set(entry.getKey(), getJsonNodeValue(value));

            }
        });

        nodeMap.put(intactId, newNode);
        nodeNameMap.put(intactId, intactId);
        return newNode;
    }

    private static void createIntactEdge(CyNetwork network, JsonNode edgeJSON,
                                         Map<String, CyNode> nodeMap, Map<String, String> nodeNameMap, List<CyEdge> newEdges) {
        String source = edgeJSON.get("source").textValue();
        String target = edgeJSON.get("target").textValue();
        CyNode sourceNode = nodeMap.get(source);
        CyNode targetNode = nodeMap.get(target);


        CyEdge edge;
        String type = edgeJSON.get("interaction_type").textValue();

        edge = network.addEdge(sourceNode, targetNode, false);

        CyRow row = network.getRow(edge);

        row.set(CyNetwork.NAME, nodeNameMap.get(source) + " (" + type + ") " + nodeNameMap.get(target));
        row.set(CyEdge.INTERACTION, type);
        row.set(C_IS_COLLAPSED, false);

        boolean isDisruptedByMutation = edgeJSON.get("disrupted_by_mutation").booleanValue();
        if (isDisruptedByMutation) {
            if (network.getRow(sourceNode).get(MUTATION, Boolean.class)) {
                row.set(SOURCE_SHAPE, "Circle");
            }
            if (network.getRow(targetNode).get(MUTATION, Boolean.class)) {
                row.set(TARGET_SHAPE, "Circle");
            }
        }

        if (newEdges != null)
            newEdges.add(edge);

        edgeJSON.fields().forEachRemaining(entry -> {
            JsonNode value = entry.getValue();
            switch (entry.getKey()) {
                case "id":
                case "source":
                case "target":
                case "interaction_type":
                    return;
                case "interaction_detection_method":
                    row.set(DETECTION_METHOD, value.textValue());
                    break;
                case "interaction_ac":
                    row.set(INTACT_ID, value.textValue());
                    break;
                case "mi_score":
                    row.set(MI_SCORE, value.doubleValue());
                    break;
                case "disrupted_by_mutation":
                    row.set(DISRUPTED_BY_MUTATION, value.booleanValue());
                    break;
                case "shape":
                    row.set(SHAPE, value.textValue());
                    break;
                case "color":
                    row.set(COLOR, cleanJSONColorData(value.textValue()));
                    break;
                case "collapsed_color":
                    row.set(C_COLOR, cleanJSONColorData(value.textValue()));
                    break;
                default:
                    row.set(entry.getKey(), getJsonNodeValue(value));
            }
        });
    }

    private static String cleanJSONColorData(Object colorObject) {
        String tmp = ((String) colorObject).replaceFirst("rgb\\(", "");
        return tmp.substring(0, tmp.length() - 1);
    }

    //////////////////////////////////////////////////////////////////////

}
