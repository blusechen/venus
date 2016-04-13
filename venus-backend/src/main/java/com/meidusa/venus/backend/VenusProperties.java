package com.meidusa.venus.backend;

import java.io.IOException;
import java.util.Properties;

public class VenusProperties extends Properties {
    private static final long serialVersionUID = 1L;
    static VenusProperties venus = new VenusProperties();
    static {
        try {
            venus.load(VenusProperties.class.getResourceAsStream("Venus.properties"));
        } catch (IOException e) {
        }
    }

    private VenusProperties() {
    };

    public static String getVersion() {
        return venus.getProperty("version");
    }
}
