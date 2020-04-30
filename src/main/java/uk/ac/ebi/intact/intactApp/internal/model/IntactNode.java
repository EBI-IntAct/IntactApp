package uk.ac.ebi.intact.intactApp.internal.model;

import org.apache.commons.codec.binary.Base64;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class IntactNode {
    final IntactNetwork intactNetwork;
    final CyNode intactNode;

    public IntactNode(final IntactNetwork iNet, final CyNode sNode) {
        intactNetwork = iNet;
        intactNode = sNode;
    }

    public String getName() {
        return ModelUtils.getName(intactNetwork.getNetwork(), intactNode);
    }

    public String getDisplayName() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, CyNetwork.NAME);
    }

    public String getSpecies() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.SPECIES);
    }

    public String getStringID() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.STRINGID);
    }

    public boolean haveUniprot() {
        return (getUniprot() != null && !getUniprot().equals(""));
    }

    public String getUniprot() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.CANONICAL);
    }

    public String getUniprotURL() {
        String uniprot = getUniprot();
        if (uniprot == null) return null;
        return "http://www.uniprot.org/uniprot/" + uniprot;
    }

    public boolean haveGeneCard() {
        return (haveUniprot() && getSpecies().equals("Homo sapiens"));
    }

    public String getGeneCardURL() {
        String uniprot = getUniprot();
        if (uniprot == null) return null;
        // GeneCards only supports human proteins
        if (getSpecies().equals("Homo sapiens"))
            return "http://www.genecards.org/cgi-bin/carddisp.pl?gene=" + uniprot;
        return null;
    }

    public boolean haveCompartments() {
        return haveData("compartment", null);
    }

    public String getCompartments() {
        return getStringID();
    }

    public String getCompartmentsURL() {
        String id = getCompartments();
        if (id == null) return null;
        return "http://compartments.jensenlab.org/" + id;
    }

    public boolean haveTissues() {
        return haveData("tissue", null);
    }

    public String getTissues() {
        return getStringID();
    }

    public String getTissuesURL() {
        String id = getTissues();
        if (id == null) return null;
        return "http://tissues.jensenlab.org/" + id;
    }

    public boolean havePharos() {
        // return haveData("pharos ", 4);
        // pharos* columns were renamed to target*
        // every human protein is in pharos as of now
        return (getSpecies() != null && getSpecies().equals("Homo sapiens") && getNodeType().equals("protein"));
    }

    public String getPharos() {
        return getUniprot();
    }

    public String getPharosURL() {
        String id = getPharos();
        if (id == null) return null;
        return "http://pharos.nih.gov/idg/targets/" + id;
    }

    public boolean haveDisease() {
        return haveData("stringdb", "disease score");
    }

    public String getDisease() {
        return getStringID();
    }

    public String getDiseaseURL() {
        String id = getDisease();
        if (id == null) return null;
        return "http://diseases.jensenlab.org/" + id;
    }

    public String getNodeType() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.TYPE);
    }

    public boolean havePubChem() {
        return getNodeType().equals("compound");
    }

    public String getPubChem() {
        String dbID = getStringID();
        Matcher m = ModelUtils.cidmPattern.matcher(dbID);
        if (m.lookingAt())
            return m.replaceAll("");
        return null;
    }

    public String getPubChemURL() {
        String id = getPubChem();
        if (id == null || id.equals("")) return null;
        return "https://pubchem.ncbi.nlm.nih.gov/compound/" + id;
    }


    public BufferedImage getStructureImage() {
        BufferedImage bi = null;

        String input = ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.STYLE);
        if (input != null && input.startsWith("string:data:")) {
            input = input.substring(input.indexOf(","));
        }
        byte[] byteStream = Base64.decodeBase64(input);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteStream);
            bi = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            bi = null;
            e.printStackTrace();
        }
        return bi;
    }

    public String getDescription() {
        return ModelUtils.getString(intactNetwork.getNetwork(), intactNode, ModelUtils.DESCRIPTION);
    }

    public boolean haveData(String namespace, String columnMatch) {
        CyNetwork net = intactNetwork.getNetwork();
        List<String> matchingColumns = new ArrayList<>();
        for (CyColumn column : net.getDefaultNodeTable().getColumns()) {
            if (namespace != null && column.getNamespace() != null && column.getNamespace().equals(namespace)) {
                if (columnMatch != null && column.getNameOnly().equals(columnMatch)) {
                    matchingColumns.add(column.getName());
                } else if (columnMatch == null) {
                    matchingColumns.add(column.getName());
                }
            } else if (namespace == null && column.getNameOnly().equals(columnMatch)) {
                matchingColumns.add(column.getName());
            }
        }

        if (matchingColumns.size() == 0)
            return false;

        for (String column : matchingColumns) {
            if (net.getRow(intactNode).getRaw(column) != null)
                return true;
        }
        return false;
    }
}
