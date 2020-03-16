package uk.ac.ebi.intact.intactApp.internal.utils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.*;
import org.cytoscape.view.model.*;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.*;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import uk.ac.ebi.intact.intactApp.internal.model.ChartType;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.Species;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewUtils {
    public static String STYLE_NAME = "STRING style";
    public static String STYLE_NAME_NAMESPACES = "STRING style v1.5";
    public static String STYLE_NAME_ORG = "Organism STRING style";
    public static String STYLE_NAME_ORG_NAMESPACES = "Organism STRING style v1.5";
    public static String STYLE_ORG = "Organism ";

    // Our chart strings
    static String PIE_CHART = "piechart: attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";
    static String CIRCOS_CHART = "circoschart: firstarc=1.0 arcwidth=0.4 attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";
    static String CIRCOS_CHART2 = "circoschart: borderwidth=0 firstarc=1.0 arcwidth=0.4 attributelist=\"enrichmentTermsIntegers\" showlabels=\"false\" colorlist=\"";

    public static CyNetworkView styleNetwork(IntactManager manager, CyNetwork network,
                                             CyNetworkView netView) {
        boolean useStitch = false;
        if (network.getDefaultNodeTable().getColumn(ModelUtils.TYPE) != null)
            useStitch = true;
//        VisualStyle stringStyle = createStyle(manager, network, useStitch);
//
//        updateColorMap(manager, stringStyle, network);
//        updateEnhancedLabels(manager, stringStyle, network, manager.showEnhancedLabels());
//        updateGlassBallEffect(manager, stringStyle, network, manager.showGlassBallEffect());

//        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
//        vmm.setCurrentVisualStyle(stringStyle);

        if (netView != null) {
//            vmm.setVisualStyle(stringStyle, netView);
            manager.getService(CyNetworkViewManager.class).addNetworkView(netView);
            manager.getService(CyApplicationManager.class).setCurrentNetworkView(netView);

        }

        return netView;
    }

    public static void reapplyStyle(IntactManager manager, CyNetworkView view) {
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        style.apply(view);
    }

    public static void updateNodeStyle(IntactManager manager,
                                       CyNetworkView view, List<CyNode> nodes) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyNode node : nodes) {
            if (view.getNodeView(node) != null)
                style.apply(view.getModel().getRow(node), view.getNodeView(node));
        }
        // style.apply(view);
    }

    public static void updateEdgeStyle(IntactManager manager, CyNetworkView view, List<CyEdge> edges) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = vmm.getVisualStyle(view);
        for (CyEdge edge : edges) {
            if (view.getEdgeView(edge) != null)
                style.apply(view.getModel().getRow(edge), view.getEdgeView(edge));
        }
        // style.apply(view);
    }

    public static VisualStyle createStyle(IntactManager manager, CyNetwork network, boolean useStitch) {
        String styleName = getStyleName(manager, network);

        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        for (VisualStyle style : vmm.getAllVisualStyles()) {
            if (style.getTitle().equals(styleName)) {
                return style;
            }
        }

        VisualStyleFactory vsf = manager.getService(VisualStyleFactory.class);

        VisualStyle stringStyle = vsf.createVisualStyle(vmm.getCurrentVisualStyle());
        stringStyle.setTitle(styleName);

        // Lock node width and height
        for (VisualPropertyDependency<?> vpd : stringStyle.getAllVisualPropertyDependencies()) {
            if (vpd.getIdString().equals("nodeSizeLocked"))
                vpd.setDependency(false);
        }

        // Get all of the factories we'll need
        VisualMappingFunctionFactory continuousFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        VisualMappingFunctionFactory discreteFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");

        {
            DiscreteMapping<String, NodeShape> dMapping =
                    (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class,
                            BasicVisualLexicon.NODE_SHAPE);
            dMapping.putMapValue("small molecule", NodeShapeVisualProperty.TRIANGLE);
            dMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
            dMapping.putMapValue("gene", NodeShapeVisualProperty.ROUND_RECTANGLE);
            dMapping.putMapValue("dna", BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE"));
            dMapping.putMapValue("rna", NodeShapeVisualProperty.DIAMOND);
            dMapping.putMapValue("complex", NodeShapeVisualProperty.HEXAGON);

            stringStyle.addVisualMappingFunction(dMapping);
        }


        {
            DiscreteMapping<String, Color> dMapping =
                    (DiscreteMapping) discreteFactory.createVisualMappingFunction(CyEdge.INTERACTION,
                            String.class,
                            BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
            dMapping.putMapValue("physical association", Color.decode("99CC00"));
            dMapping.putMapValue("association", Color.decode("9999FF"));
            dMapping.putMapValue("direct interaction", Color.decode("FFA500"));
            dMapping.putMapValue("colocalization", Color.decode("FFDE3E"));
            dMapping.putMapValue("phosphorylation", Color.decode("990000"));
            dMapping.putMapValue("dephosphorylation", Color.decode("999900"));
            stringStyle.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.decode("999999"));
            stringStyle.addVisualMappingFunction(dMapping);
        }

        vmm.addVisualStyle(stringStyle);
        return stringStyle;
    }

    public static void updateChemVizPassthrough(IntactManager manager, CyNetworkView view, boolean show) {
        VisualStyle stringStyle = getStyle(manager, view);

        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();

        if (show && manager.haveChemViz()) {
            VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
            PassthroughMapping pMapping =
                    (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.CV_STYLE,
                            String.class, customGraphics);
            stringStyle.addVisualMappingFunction(pMapping);
        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2"));
        }
    }

    public static void updateEnhancedLabels(IntactManager manager, VisualStyle stringStyle,
                                            CyNetwork net, boolean show) {

        boolean useStitch = false;
        if (net.getDefaultNodeTable().getColumn(ModelUtils.TYPE) != null)
            useStitch = true;

        VisualMappingFunctionFactory discreteFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        VisualMappingFunctionFactory passthroughFactory =
                manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        // Set up the passthrough mapping for the label
        if (show && manager.haveEnhancedGraphics()) {
            {
                VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");
                PassthroughMapping pMapping =
                        (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.ELABEL_STYLE,
                                String.class, customGraphics);
                stringStyle.addVisualMappingFunction(pMapping);
            }

            // Set up our labels to be in the upper right quadrant
            {
                VisualProperty customGraphicsP = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3");
                Object upperRight = customGraphicsP.parseSerializableString("NE,C,c,0.00,0.00");
                stringStyle.setDefaultValue(customGraphicsP, upperRight);
                if (useStitch) {
                    Object top = customGraphicsP.parseSerializableString("N,C,c,0.00,-5.00");
                    DiscreteMapping<String, Object> dMapping =
                            (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE, String.class,
                                    customGraphicsP);
                    dMapping.putMapValue("compound", top);
                    dMapping.putMapValue("protein", upperRight);
                    stringStyle.addVisualMappingFunction(dMapping);
                }
            }

            // Finally, disable the "standard" label passthrough and position
            {
                stringStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
                // stringStyle.removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_LABEL_POSITION"));
            }
        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3"));
            stringStyle.removeVisualMappingFunction(
                    lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_3"));

            {
                PassthroughMapping pMapping = (PassthroughMapping) passthroughFactory
                        .createVisualMappingFunction(ModelUtils.DISPLAY, String.class,
                                BasicVisualLexicon.NODE_LABEL);
                stringStyle.addVisualMappingFunction(pMapping);
            }

            // {
            // VisualProperty labelPosition = lex.lookup(CyNode.class, "NODE_LABEL_POSITION");
            // DiscreteMapping<String,Object> dMapping =
            // (DiscreteMapping) discreteFactory.createVisualMappingFunction(ModelUtils.TYPE,
            // String.class,
            // labelPosition);
            // Object top = labelPosition.parseSerializableString("N,S,c,0.00,0.00");
            // Object upperRight = labelPosition.parseSerializableString("NE,S,c,0.00,0.00");
            // dMapping.putMapValue("compound", top);
            // dMapping.putMapValue("protein", upperRight);
            // stringStyle.addVisualMappingFunction(dMapping);
            // }
        }
    }

    public static void updateGlassBallEffect(IntactManager manager, VisualStyle stringStyle,
                                             CyNetwork net, boolean show) {

        VisualMappingFunctionFactory passthroughFactory = manager
                .getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class)
                .getDefaultVisualLexicon();

        // Set up the passthrough mapping for the glass ball effect
        if (show) {
            {
                VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
                PassthroughMapping pMapping =
                        (PassthroughMapping) passthroughFactory.createVisualMappingFunction(ModelUtils.STYLE,
                                String.class, customGraphics);
                stringStyle.addVisualMappingFunction(pMapping);
            }

        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1"));
            stringStyle.removeVisualMappingFunction(
                    lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_POSITION_1"));
        }
    }

    private static void updateColorMap(IntactManager manager, VisualStyle style, CyNetwork network) {
        // Build the color list
        DiscreteMapping<String, Color> dMapping = getIntactNodeColorMapping(manager, network);
        style.addVisualMappingFunction(dMapping);
    }

    private static DiscreteMapping<String, Color> getIntactNodeColorMapping(IntactManager manager,
                                                                            CyNetwork network) {
        VisualMappingFunctionFactory discreteFactory = manager
                .getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
        if (discreteFactory == null) {
            return null;
        }

        DiscreteMapping<String, Color> dMapping = (DiscreteMapping) discreteFactory
                .createVisualMappingFunction(CyNetwork.NAME, String.class,
                        BasicVisualLexicon.NODE_FILL_COLOR);

        // Set the node colors around the color wheel
        float h = 0.0f;
        float s = 1.0f;
        float stepSize = 1.0f / (float) network.getNodeCount();
        for (CyNode node : network.getNodeList()) {
            Color c = Color.getHSBColor(h, s, 1.0f);
            h += stepSize;
            if (s == 1.0f)
                s = 0.5f;
            else
                s = 1.0f;
            String name = network.getRow(node).get(CyNetwork.NAME, String.class);
            dMapping.putMapValue(name, c);
        }

        return dMapping;
    }

    private static <K, V> boolean sameVisualMappingFunction(CyNetwork network,
                                                            VisualMappingFunction<K, V> vmf, DiscreteMapping<String, Color> stringMapping) {
        if (!(vmf instanceof DiscreteMapping<?, ?>)) {
            return false;
        }

        if (!vmf.getMappingColumnName().equals(stringMapping.getMappingColumnName())) {
            return false;
        }

        if (!vmf.getMappingColumnType().equals(stringMapping.getMappingColumnType())) {
            return false;
        }

        for (CyNode node : network.getNodeList()) {
            V vmfMappedValue = vmf.getMappedValue(network.getRow(node));
            Color stringMappedValue = stringMapping.getMappedValue(network.getRow(node));

            if (vmfMappedValue == null && stringMappedValue != null) {
                return false;
            } else if (vmfMappedValue != null && !vmfMappedValue.equals(stringMappedValue)) {
                return false;
            }
        }

        return true;
    }

    private static void updateColorMapHost(IntactManager manager, VisualStyle style, CyNetwork net) {
        VisualMappingFunctionFactory discreteFactory = manager
                .getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");

        // get previous mapping
        DiscreteMapping<String, Color> dMapping = (DiscreteMapping) style
                .getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        List<String> species = ModelUtils.getAllNetSpecies(net);
        // get network species
        Map<String, Color> mapValues = new HashMap<>();

        // save previous color mapping
        if (dMapping != null) {
            Map<String, Color> mappedValues = dMapping.getAll();
            for (String spKey : mappedValues.keySet()) {
                if (species.contains(spKey)) {
                    mapValues.put(spKey, mappedValues.get(spKey));
                }
            }
        }
        // make the new mapping after removing the old one
        style.removeVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        dMapping = (DiscreteMapping) discreteFactory.createVisualMappingFunction(
                ModelUtils.SPECIES, String.class, BasicVisualLexicon.NODE_FILL_COLOR);
        // Set the species colors
        for (String sp : species) {
            if (!mapValues.containsKey(sp)) {
                dMapping.putMapValue(sp, Color.decode(Species.getSpeciesColor(sp)));
            } else {
                dMapping.putMapValue(sp, mapValues.get(sp));
            }
        }

        // DiscreteMapping<String,Color> dMapping =
        // (DiscreteMapping) discreteFactory.createVisualMappingFunction("Name", String.class,
        // BasicVisualLexicon.NODE_FILL_COLOR);
        //
        // // Set the node colors around the color wheel
        // for (View<CyNode> nv: view.getNodeViews()) {
        // Color c =
        // Color.decode(view.getModel().getRow(nv.getModel()).get(ModelUtils.SPECIES_COLOR,
        // String.class));
        // String name = view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME,
        // String.class);
        // dMapping.putMapValue(name, c);
        // }
        style.addVisualMappingFunction(dMapping);
    }

    public static void updateNodeColors(IntactManager manager,
                                        CyNetwork net, CyNetworkView view, boolean host) {
        // manager.flushEvents();
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualMappingFunctionFactory discreteFactory = manager
                .getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");

        VisualStyle style = null;
        if (view != null)
            style = vmm.getVisualStyle(view);
        else {
            String styleName = getStyleName(manager, net);
            for (VisualStyle s : vmm.getAllVisualStyles()) {
                if (s.getTitle().equals(styleName)) {
                    style = s;
                    break;
                }
            }
        }

        // Worst case -- can't find a style, so er just bail
        if (style == null) return;

        if (!style.getTitle().startsWith(STYLE_NAME_ORG_NAMESPACES)) {
            VisualStyleFactory vsf = manager.getService(VisualStyleFactory.class);

            VisualStyle stringStyle = vsf.createVisualStyle(vmm.getCurrentVisualStyle());
            stringStyle.setTitle(STYLE_ORG + style.getTitle());
            vmm.addVisualStyle(stringStyle);
            style = stringStyle;
        }

        if (host) {
            updateColorMapHost(manager, style, net);
        } else {
            updateColorMap(manager, style, net);
        }
        if (view != null)
            vmm.setVisualStyle(style, view);
        vmm.setCurrentVisualStyle(style);
    }

    public static void updatePieCharts(IntactManager manager, VisualStyle stringStyle,
                                       CyNetwork net, boolean show) {

        VisualMappingFunctionFactory passthroughFactory = manager
                .getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
        VisualLexicon lex = manager.getService(RenderingEngineManager.class)
                .getDefaultVisualLexicon();
        // Set up the passthrough mapping for the label
        if (show) {
            {
                VisualProperty customGraphics = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_4");
                PassthroughMapping pMapping = (PassthroughMapping) passthroughFactory
                        .createVisualMappingFunction(EnrichmentTerm.colEnrichmentPassthrough, String.class,
                                customGraphics);
                stringStyle.addVisualMappingFunction(pMapping);
            }
        } else {
            stringStyle
                    .removeVisualMappingFunction(lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_4"));

            // Restore the glass ball, if appropriate
            if (manager.showGlassBallEffect()) {
                CyNetworkView netView = manager.getCurrentNetworkView();
                VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
                ViewUtils.updateGlassBallEffect(manager, vmm.getVisualStyle(netView), net, true);
            }
        }
    }

    public static void drawCharts(IntactManager manager,
                                  Map<EnrichmentTerm, String> selectedTerms,
                                  ChartType type) {
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null || selectedTerms.size() == 0)
            return;

        CyTable nodeTable = network.getDefaultNodeTable();
        createColumns(nodeTable);

        List<String> colorList = getColorList(selectedTerms);
        List<String> shownTermNames = getTermNames(network, nodeTable, selectedTerms);

        for (CyNode node : network.getNodeList()) {
            List<Integer> nodeTermsIntegers =
                    nodeTable.getRow(node.getSUID()).getList(EnrichmentTerm.colEnrichmentTermsIntegers, Integer.class);
            String nodeColor = nodeColors(colorList, nodeTermsIntegers, type);
            nodeTable.getRow(node.getSUID()).set(EnrichmentTerm.colEnrichmentPassthrough, nodeColor);
            nodeTable.getRow(node.getSUID()).set(EnrichmentTerm.colEnrichmentTermsIntegers, nodeTermsIntegers);
        }

        // System.out.println(selectedTerms);
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        CyNetworkView netView = manager.getCurrentNetworkView();
        if (netView != null) {
            ViewUtils.updatePieCharts(manager, vmm.getVisualStyle(netView), network, true);

            // Don't override the user if they have specifically disabled the glass ball effect
            if (manager.showGlassBallEffect()) {
                if (ChartType.PIE.equals(type) || ChartType.SPLIT_PIE.equals(type)) {
                    ViewUtils.updateGlassBallEffect(manager, vmm.getVisualStyle(netView), network, false);
                    // manager.setShowGlassBallEffect(false);
                } else {
                    ViewUtils.updateGlassBallEffect(manager, vmm.getVisualStyle(netView), network, true);
                    // manager.setShowGlassBallEffect(true);
                }
                // manager.getShowGlassBallEffectTaskFactory().reregister();
            }
            netView.updateView();
        }
        // save in network table
        CyTable netTable = network.getDefaultNetworkTable();
        ModelUtils.createListColumnIfNeeded(netTable, String.class, ModelUtils.NET_ENRICHMENT_VISTEMRS);
        netTable.getRow(network.getSUID()).set(ModelUtils.NET_ENRICHMENT_VISTEMRS, shownTermNames);

        ModelUtils.createListColumnIfNeeded(netTable, String.class, ModelUtils.NET_ENRICHMENT_VISCOLORS);
        netTable.getRow(network.getSUID()).set(ModelUtils.NET_ENRICHMENT_VISCOLORS, colorList);
    }

    private static void createColumns(CyTable nodeTable) {
        // replace columns
        ModelUtils.replaceListColumnIfNeeded(nodeTable, String.class,
                EnrichmentTerm.colEnrichmentTermsNames);
        ModelUtils.replaceListColumnIfNeeded(nodeTable, Integer.class,
                EnrichmentTerm.colEnrichmentTermsIntegers);
        ModelUtils.replaceColumnIfNeeded(nodeTable, String.class,
                EnrichmentTerm.colEnrichmentPassthrough);
    }

    public static void highlight(IntactManager manager, CyNetworkView view, List<CyNode> nodes) {
        CyNetwork net = view.getModel();

        List<CyEdge> edgeList = new ArrayList<>();
        List<CyNode> nodeList = new ArrayList<>();
        for (CyNode node : nodes) {
            edgeList.addAll(net.getAdjacentEdgeList(node, CyEdge.Type.ANY));
            nodeList.addAll(net.getNeighborList(node, CyEdge.Type.ANY));
        }


        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        VisualProperty customGraphics1 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
        VisualProperty customGraphics2 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
        VisualProperty customGraphics3 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");

        CyCustomGraphics cg = new EmptyCustomGraphics();

        // Override our current style through overrides
        for (View<CyNode> nv : view.getNodeViews()) {
            if (nodeList.contains(nv.getModel()) || nodes.contains(nv.getModel())) {
                nv.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 255);
            } else {
                nv.setLockedValue(customGraphics1, cg);
                nv.setLockedValue(customGraphics2, cg);
                nv.setLockedValue(customGraphics3, cg);
                nv.setLockedValue(BasicVisualLexicon.NODE_TRANSPARENCY, 20);
            }
        }
        for (View<CyEdge> ev : view.getEdgeViews()) {
            if (edgeList.contains(ev.getModel())) {
                ev.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 255);
            } else {
                ev.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 20);
            }
        }
    }

    public static void clearHighlight(IntactManager manager, CyNetworkView view) {
        // if (node == null) return;
        // View<CyNode> nodeView = view.getNodeView(node);
        if (view == null) return;

        VisualLexicon lex = manager.getService(RenderingEngineManager.class).getDefaultVisualLexicon();
        VisualProperty customGraphics1 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
        VisualProperty customGraphics2 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_2");
        VisualProperty customGraphics3 = lex.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_3");

        for (View<CyNode> nv : view.getNodeViews()) {
            nv.clearValueLock(customGraphics1);
            nv.clearValueLock(customGraphics2);
            nv.clearValueLock(customGraphics3);
            nv.clearValueLock(BasicVisualLexicon.NODE_TRANSPARENCY);
        }

        for (View<CyEdge> ev : view.getEdgeViews()) {
            ev.clearValueLock(BasicVisualLexicon.EDGE_TRANSPARENCY);
        }
    }

    public static void hideStringColors(IntactManager manager, CyNetworkView view, boolean show) {
        VisualStyle style = getStyle(manager, view);
        if (style == null || !style.getTitle().contains(STYLE_NAME)) return;

        // Don't overwrite a mapping the user added
        VisualMappingFunction<?, Paint> function = style.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);

        if (function != null && function.getMappingColumnName() != CyNetwork.NAME)
            return;

        if (show) {
            updateColorMap(manager, style, view.getModel());
        } else {
            style.removeVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        }

        // alternative fix by Marc ... to check
        // VisualMappingFunction<?,?> vmf =
        // style.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        // // get the root network
        // CyNetwork rootNetwork =
        // manager.getService(CyRootNetworkManager.class).getRootNetwork(view.getModel()).getBaseNetwork();
        //
        // if (show) {
        // // We update the colorMap only if there is no VisualMapping already applied
        // if (vmf == null) {
        // updateColorMap(manager, style, rootNetwork);
        // } else {
        // // We make sure that this is not a custom VisualMapping
        // if (vmf != null && sameVisualMappingFunction(rootNetwork, vmf,
        // getIntactNodeColorMapping(manager, rootNetwork))) {
        // style.removeVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
        // }
        // }
        // }
    }

    public static void hideSingletons(CyNetworkView view, boolean show) {
        CyNetwork net = view.getModel();
        for (View<CyNode> nv : view.getNodeViews()) {
            CyNode node = nv.getModel();
            List<CyEdge> edges = net.getAdjacentEdgeList(node, CyEdge.Type.ANY);
            if (edges != null && edges.size() > 0) continue;
            if (!show)
                nv.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
            else
                nv.clearValueLock(BasicVisualLexicon.NODE_VISIBLE);
        }
    }

    private static List<String> getColorList(Map<EnrichmentTerm, String> selectedTerms) {
        List<String> colorList = new ArrayList<>();
        for (EnrichmentTerm term : selectedTerms.keySet()) {
            // Color color = selectedTerms.get(term);
            String color = selectedTerms.get(term);
            if (color != null) {
                //String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
                //		color.getBlue());
                //colorList += hex + ",";
                colorList.add(color);
            } else {
                colorList.add("");
            }
        }
        return colorList;
    }

    private static List<String> getTermNames(CyNetwork network, CyTable nodeTable,
                                             Map<EnrichmentTerm, String> selectedTerms) {
        List<String> shownTermNames = new ArrayList<>();
        boolean firstTerm = true;
        for (EnrichmentTerm term : selectedTerms.keySet()) {
            String selTerm = term.getName();
            shownTermNames.add(selTerm);
            List<Long> enrichedNodeSUIDs = term.getNodesSUID();
            for (CyNode node : network.getNodeList()) {
                List<Integer> nodeTermsIntegers = nodeTable.getRow(node.getSUID())
                        .getList(EnrichmentTerm.colEnrichmentTermsIntegers, Integer.class);
                List<String> nodeTermsNames = nodeTable.getRow(node.getSUID())
                        .getList(EnrichmentTerm.colEnrichmentTermsNames, String.class);
                if (firstTerm || nodeTermsIntegers == null)
                    nodeTermsIntegers = new ArrayList<>();
                if (firstTerm || nodeTermsNames == null) {
                    nodeTermsNames = new ArrayList<>();
                }
                if (enrichedNodeSUIDs.contains(node.getSUID())) {
                    nodeTermsNames.add(selTerm);
                    nodeTermsIntegers.add(1);
                } else {
                    nodeTermsNames.add("");
                    nodeTermsIntegers.add(0);
                }
                nodeTable.getRow(node.getSUID()).set(EnrichmentTerm.colEnrichmentTermsIntegers, nodeTermsIntegers);
                nodeTable.getRow(node.getSUID()).set(EnrichmentTerm.colEnrichmentTermsNames, nodeTermsNames);
            }
            if (firstTerm) firstTerm = false;
        }
        return shownTermNames;
    }

    private static String nodeColors(List<String> colors, List<Integer> nodeTermFlags, ChartType type) {
        boolean foundTerm = false;
        for (Integer term : nodeTermFlags) {
            if (term > 0) {
                foundTerm = true;
                break;
            }
        }
        if (!foundTerm) return null;

        StringBuilder colorString = new StringBuilder();
        if (type.equals(ChartType.FULL) || type.equals(ChartType.PIE)) {
            for (String color : colors) {
                colorString.append(color).append(",");
            }
        } else {
            for (int i = 0; i < colors.size(); i++) {
                if (nodeTermFlags.get(i) > 0) {
                    if (type.equals(ChartType.TEETH))
                        colorString.append(colors.get(i)).append("ff,");
                    else
                        colorString.append(colors.get(i)).append(",");
                } else {
                    if (type.equals(ChartType.TEETH))
                        colorString.append("#ffffff00,");
                    else
                        colorString.append("#ffffff,");
                    nodeTermFlags.set(i, 1);
                }
            }
            if (!foundTerm) return null;
        }
        if (type.equals(ChartType.PIE) || type.equals(ChartType.SPLIT_PIE))
            return PIE_CHART + colorString.substring(0, colorString.length() - 1) + "\"";
        if (type.equals(ChartType.TEETH))
            return CIRCOS_CHART2 + colorString.substring(0, colorString.length() - 1) + "\"";
        return CIRCOS_CHART + colorString.substring(0, colorString.length() - 1) + "\"";
    }

    public static VisualStyle getStyle(IntactManager manager, CyNetworkView view) {
        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        VisualStyle style = null;
        if (view != null)
            style = vmm.getVisualStyle(view);
        else {
            String styleName = getStyleName(manager, view.getModel());
            for (VisualStyle s : vmm.getAllVisualStyles()) {
                if (s.getTitle().equals(styleName)) {
                    style = s;
                    break;
                }
            }
        }

        return style;
    }

    private static String getStyleName(IntactManager manager, CyNetwork network) {
        String networkName = manager.getNetworkName(network);
        String styleName = STYLE_NAME_NAMESPACES;
        if (networkName.startsWith("String Network")) {
            String[] parts = networkName.split("_");
            if (parts.length == 1) {
                String[] parts2 = networkName.split(" - ");
                if (parts2.length == 2)
                    styleName = styleName + " - " + parts2[1];
            } else if (parts.length == 2)
                styleName = styleName + "_" + parts[1];
        }
        return styleName;
    }
}