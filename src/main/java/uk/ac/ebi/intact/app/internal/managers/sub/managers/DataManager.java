package uk.ac.ebi.intact.app.internal.managers.sub.managers;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.*;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.ListField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.models.*;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.*;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class DataManager implements
        SessionLoadedListener,
        NetworkAddedListener,
        NetworkViewAddedListener,
        NetworkAboutToBeDestroyedListener,
        NetworkViewAboutToBeDestroyedListener {
    private final Manager manager;
    final CyRootNetworkManager rootNetworkManager;
    final List<IntactNetworkCreatedListener> intactNetworkCreatedListeners = new ArrayList<>();
    final List<IntactViewUpdatedListener> intactViewUpdatedListeners = new ArrayList<>();
    final Map<CyNetwork, Network> networkMap;
    final Map<CyNetworkView, NetworkView> intactNetworkViewMap;

    public DataManager(Manager manager) {
        this.manager = manager;
        networkMap = new HashMap<>();
        intactNetworkViewMap = new HashMap<>();
        rootNetworkManager = manager.utils.getService(CyRootNetworkManager.class);

        // Load Fields
        System.out.println(NodeFields.SPECIES.toString());
        System.out.println(EdgeFields.SUMMARY_NB_EDGES.toString());
        System.out.println(NetworkFields.FEATURES_TABLE_REF.toString());
        System.out.println(FeatureFields.TYPE.toString());
        System.out.println(FeatureFields.TYPE.toString());
        System.out.println(IdentifierFields.ID.toString());
    }

    public void loadCurrentSession() {
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        for (CyNetwork cyNetwork : manager.utils.getService(CyNetworkManager.class).getNetworkSet()) {
            if (!isIntactNetwork(cyNetwork)) continue;
            Network network = new Network(manager);
            addNetwork(network, cyNetwork);
            fireIntactNetworkCreated(network);
            linkNetworkTablesFromTableData(network);
            network.completeMissingNodeColorsFromTables();
            for (CyNetworkView view : networkViewManager.getNetworkViews(cyNetwork)) {
                addNetworkView(view, true);
            }
        }
    }


    public CyNetwork createNetwork(String name) {
        CyNetwork cyNetwork = manager.utils.getService(CyNetworkFactory.class).createNetwork();
        CyNetworkManager netMgr = manager.utils.getService(CyNetworkManager.class);

        Set<CyNetwork> nets = netMgr.getNetworkSet();
        Set<CyNetwork> allNets = new HashSet<>(nets);
        for (CyNetwork net : nets) {
            allNets.add(((CySubNetwork) net).getRootNetwork());
        }
        // See if this name is already taken by a network or a network collection (root network)
        int index = -1;
        boolean match = false;
        for (CyNetwork net : allNets) {
            String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
            if (netName.equals(name)) {
                match = true;
            } else if (netName.startsWith(name)) {
                String subname = netName.substring(name.length());
                if (subname.startsWith(" - ")) {
                    try {
                        int v = Integer.parseInt(subname.substring(3));
                        if (v >= index)
                            index = v + 1;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (match && index < 0) {
            name = name + " - 1";
        } else if (index > 0) {
            name = name + " - " + index;
        }
        cyNetwork.getRow(cyNetwork).set(CyNetwork.NAME, name);

        return cyNetwork;
    }

    public void addNetwork(Network network, CyNetwork cyNetwork) {
        networkMap.put(cyNetwork, network);
        network.setNetwork(cyNetwork);
    }

    public String getNetworkName(CyNetwork net) {
        return net.getRow(net).get(CyNetwork.NAME, String.class);
    }

    public CyNetworkView createNetworkView(CyNetwork cyNetwork) {
        CyNetworkView view = manager.utils.getService(CyNetworkViewFactory.class)
                .createNetworkView(cyNetwork);
        if (networkMap.containsKey(cyNetwork)) {
            networkMap.get(cyNetwork).hideExpandedEdgesOnViewCreation(view);
            manager.style.intactStyles.get(SummaryStyle.type).applyStyle(view);
        }
        return view;
    }

    public void addNetwork(CyNetwork cyNetwork) {
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        if (!networkManager.networkExists(cyNetwork.getSUID())) {
            networkManager.addNetwork(cyNetwork);
        }
        manager.utils.getService(CyApplicationManager.class).setCurrentNetwork(cyNetwork);
    }

    public NetworkView addNetworkView(CyNetworkView cyView, boolean loadData) {
        NetworkView view = new NetworkView(manager, cyView, loadData);
        intactNetworkViewMap.put(cyView, view);
        return view;
    }

    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
        CyNetwork cyNetwork = e.getNetwork();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        Network network = networkMap.get(cyNetwork);
        if (network != null) {
            CyTable featuresTable = network.getFeaturesTable();
            if (featuresTable != null) tableManager.deleteTable(featuresTable.getSUID());
            CyTable identifiersTable = network.getIdentifiersTable();
            if (identifiersTable != null) tableManager.deleteTable(identifiersTable.getSUID());

            networkMap.remove(cyNetwork);
        }
    }

    @Override
    public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
        intactNetworkViewMap.remove(e.getNetworkView());
    }

    @Override
    public void handleEvent(NetworkAddedEvent e) {
        CyNetwork newNetwork = e.getNetwork();
        CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(newNetwork);
        CySubNetwork baseNetwork = rootNetwork.getBaseNetwork();
        if (baseNetwork.getSUID().equals(newNetwork.getSUID())) return;
        addSubNetwork((CySubNetwork) newNetwork, baseNetwork);
    }

    private void addSubNetwork(CySubNetwork newCyNetwork, CySubNetwork baseNetwork) {
        if (networkMap.containsKey(baseNetwork)) {
            Network parent = networkMap.get(baseNetwork);
            handleSubNetworkEdges(newCyNetwork, parent);
            Network newNetwork = new Network(manager);
            addNetwork(newNetwork, newCyNetwork);
            newNetwork.setFeaturesTable(parent.getFeaturesTable());
            newNetwork.setIdentifiersTable(parent.getIdentifiersTable());
            NetworkFields.UUID.setValue(newCyNetwork.getRow(newCyNetwork), NetworkFields.UUID.getValue(baseNetwork.getRow(baseNetwork)));
        }
    }

    @Override
    public void handleEvent(NetworkViewAddedEvent e) {
        if (networkMap.containsKey(e.getNetworkView().getModel())) {
            addNetworkView(e.getNetworkView(), false);
        }
    }

    @Override
    public void handleEvent(SessionLoadedEvent event) {
        // Create string networks for any networks loaded by string
        networkMap.clear();
        intactNetworkViewMap.clear();
        CySession loadedSession = event.getLoadedSession();
        Set<CyNetwork> cyNetworks = loadedSession.getNetworks();
        for (CyNetwork cyNetwork : cyNetworks) {
            if (ModelUtils.isIntactNetwork(cyNetwork)) {
                if (ModelUtils.ifHaveIntactNS(cyNetwork)) {
                    Network network = new Network(manager);
                    addNetwork(network, cyNetwork);
                    network.completeMissingNodeColorsFromTables();
                }
            }
        }

        linkIntactTablesToNetwork(loadedSession.getTables(), loadedSession);

        for (CyNetworkView view : loadedSession.getNetworkViews()) {
            if (ModelUtils.isIntactNetwork(view.getModel())) {
                addNetworkView(view, true);
            }
        }

        NetworkView currentView = getCurrentIntactNetworkView();
        if (currentView != null) {
            fireIntactViewChangedEvent(new IntactViewUpdatedEvent(manager, currentView));
            manager.utils.showResultsPanel();
        } else {
            manager.utils.hideResultsPanel();
        }
    }

    void linkIntactTablesToNetwork(Collection<CyTableMetadata> tables, CySession loadingSession) {
        for (CyTableMetadata tableM : tables) {
            linkSummaryEdgesIdsToSUIDs(tableM, loadingSession);
            CyTable table = tableM.getTable();

            CyColumn networkUUIDColumn = NetworkFields.UUID.getColumn(table);
            if (networkUUIDColumn == null) continue;
            List<String> uuids = networkUUIDColumn.getValues(String.class);
            if (uuids.isEmpty()) continue;

            if (Table.FEATURE.containsAllFields(table)) {
                updateSUIDList(table, FeatureFields.EDGES_SUID, CyEdge.class, loadingSession);
                for (Network network : networkMap.values()) {
                    CyNetwork cyNetwork = network.getCyNetwork();
                    CyRow netRow = cyNetwork.getRow(cyNetwork);
                    if (NetworkFields.UUID.getValue(netRow).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                        network.setFeaturesTable(table);
                    }
                }
            }

            if (Table.IDENTIFIER.containsAllFields(table)) {
                for (Network network : networkMap.values()) {
                    CyNetwork cyNetwork = network.getCyNetwork();
                    if (NetworkFields.UUID.getValue(cyNetwork.getRow(cyNetwork)).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                        network.setIdentifiersTable(table);
                    }
                }
            }
        }
    }

    void linkSummaryEdgesIdsToSUIDs(CyTableMetadata tableM, CySession loadingSession) {
        CyTable edgeTable = tableM.getTable();
        if (!Table.EDGE.containsAllFields(edgeTable)) return;
        updateSUIDList(edgeTable, EdgeFields.SUMMARY_EDGES_SUID, CyEdge.class, loadingSession);
    }

    private void updateSUIDList(CyTable sourceTable, ListField<Long> linkField, Class<? extends CyIdentifiable> targetType, CySession loadingSession) {
        for (CyRow row : sourceTable.getAllRows()) {
            List<Long> suids = linkField.getValue(row);
            if (suids != null && loadingSession.getObject(suids.get(0), targetType) == null) return;
            linkField.map(row, oldSUID -> loadingSession.getObject(oldSUID, targetType).getSUID());
        }
    }


    //================= Data getters =================//
    public CyNetwork getCurrentCyNetwork() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetwork();
    }

    public Network getNetwork(CyNetwork cyNetwork) {
        if (networkMap.containsKey(cyNetwork))
            return networkMap.get(cyNetwork);
        return null;
    }

    public Network getCurrentNetwork() {
        return networkMap.get(getCurrentCyNetwork());
    }

    public Network[] getIntactNetworks() {
        return networkMap.values().toArray(Network[]::new);
    }

    public CyNetworkView getCurrentCyView() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetworkView();
    }

    public NetworkView getNetworkView(CyNetworkView view) {
        return intactNetworkViewMap.get(view);
    }

    public NetworkView getCurrentIntactNetworkView() {
        return intactNetworkViewMap.get(getCurrentCyView());
    }

    public NetworkView[] getViews() {
        return intactNetworkViewMap.values().toArray(NetworkView[]::new);
    }

    //================= IntactNetworkCreated =================//

    public void fireIntactNetworkCreated(Network network) {
        for (IntactNetworkCreatedListener listener : intactNetworkCreatedListeners) {
            try {
                listener.handleEvent(new IntactNetworkCreatedEvent(manager, network));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.add(listener);
    }

    public void removeIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.remove(listener);
    }


    //================= IntactViewTypeChanged =================//
    public void intactViewChanged(NetworkView.Type newType, NetworkView view) {
        manager.style.intactStyles.get(newType).applyStyle(view.cyView);
        view.setType(newType);
        fireIntactViewChangedEvent(new IntactViewUpdatedEvent(manager, view));
    }

    public void fireIntactViewChangedEvent(IntactViewUpdatedEvent event) {
        for (IntactViewUpdatedListener listener : intactViewUpdatedListeners) {
            try {
                listener.handleEvent(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addIntactViewChangedListener(IntactViewUpdatedListener listener) {
        intactViewUpdatedListeners.add(listener);
    }

    public void removeIntactViewChangedListener(IntactViewUpdatedListener listener) {
        intactViewUpdatedListeners.remove(listener);
    }
}
