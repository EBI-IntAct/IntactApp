package uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions;

import java.awt.*;

public enum InteractionType {
    COLOCOALIZATION("colocalization", "MI_0403", new Color(165, 165, 165), false),
    ASSOCIATION("association", "MI_0914", new Color(95, 77, 174), false),
    PHYSICAL_ASSOCIATION("physical association", "MI_0915", new Color(89, 121, 195), false),
    DIRECT_INTERACTION("direct interaction", "MI_0407", new Color(55, 133, 123), true),
    ENZYMATIC_REACTION("enzymatic reaction", "MI_0414", new Color(88, 134, 52), true),
    PHOSPHORYLATION_R("phosphorylation reaction", "MI_0217", new Color(255, 189, 67), true),
    PHOSPHORYLATION("phosphorylation", "", new Color(255, 189, 67), false),
    DEPHOSPHORYLATION_R("dephosphorylation reaction", "MI_0203",new Color(147, 86, 203), true),
    DEPHOSPHORYLATION("dephosphorylation", "", new Color(147, 86, 203), false);

    public final String name;
    public final String MI_ID;
    public final Color defaultColor;
    public final boolean queryChildren;

    InteractionType(String name, String MI_ID, Color defaultColor, boolean queryChildren) {
        this.name = name;
        this.MI_ID = MI_ID;
        this.defaultColor = defaultColor;
        this.queryChildren = queryChildren;
    }
}
