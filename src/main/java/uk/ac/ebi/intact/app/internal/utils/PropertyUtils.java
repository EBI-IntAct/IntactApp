package uk.ac.ebi.intact.app.internal.utils;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.Properties;

public class PropertyUtils {

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
        return properties.getProperties().getProperty(propertyKey);
    }

    public static Double getDoubleProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Double.valueOf(value);
    }

    public static Integer getIntegerProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Integer.valueOf(value);
    }

    public static Boolean getBooleanProperty(CyProperty<Properties> properties, String propertyKey) {
        String value = getStringProperty(properties, propertyKey);
        if (value == null) return null;
        return Boolean.valueOf(value);
    }

    public static class ConfigPropsReader extends AbstractConfigDirPropsReader {
        ConfigPropsReader(SavePolicy policy, String name) {
            super(name, name + ".props", policy);
        }
    }

    public static CyProperty<Properties> getPropertyService(Manager manager, CyProperty.SavePolicy policy) {
        return getPropertyService(manager, policy, "intactApp");
    }


    public static CyProperty<Properties> getPropertyService(Manager manager, CyProperty.SavePolicy policy, String name) {
        if (policy == CyProperty.SavePolicy.SESSION_FILE) {
            CyProperty<Properties> service;
            try {
                service = manager.utils.getService(CyProperty.class, "(cyPropertyName=" + name + ")");
                // Do we already have a session with our properties
                if (service.getSavePolicy().equals(CyProperty.SavePolicy.SESSION_FILE))
                    return service;
            } catch (RuntimeException e) {
                // Either we have a null session or our properties aren't in this session
                service = new SimpleCyProperty<>(name, new Properties(), Properties.class, CyProperty.SavePolicy.SESSION_FILE);
                Properties serviceProps = new Properties();
                serviceProps.setProperty("cyPropertyName", name);
                manager.utils.registerAllServices(service, serviceProps);
                return service;
            }


        } else if (policy == CyProperty.SavePolicy.CONFIG_DIR || policy == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
            CyProperty<Properties> service = new ConfigPropsReader(policy, name);
            Properties serviceProps = new Properties();
            serviceProps.setProperty("cyPropertyName", service.getName());
            manager.utils.registerAllServices(service, serviceProps);
            return service;
        }
        return null;
    }
}
