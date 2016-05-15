package com.meidusa.venus.extension.monitor;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * Created by huawei on 5/13/16.
 */
public class MonitorExtensionResolver {

    private static Logger logger = LoggerFactory.getLogger(MonitorExtensionResolver.class);

    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public static void resolver(){
        String monitorIni = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/META-INF/venus.monitor.extension.ini";
        try {
            Resource[] resources = resourcePatternResolver.getResources(monitorIni);

            if (resources == null || resources.length == 0) {
                logger.warn("monitor spi file not found.");
                return;
            }

            logger.debug("found " + resources.length + " spi file in the classpath.");

            for(Resource resource : resources) {
                Ini ini = new Ini();
                try{
                    ini.load(resource.getInputStream());
                }catch (Exception e){
                    logger.error("load ini resource failed.", e);
                    continue;

                }
                Profile.Section section = ini.get("monitor.spi");

                if (section == null) {
                    continue;
                }

                String monitorSpi = section.get("com.meidusa.venus.monitor.spi");

                if(monitorSpi == null) {
                    continue;
                }

                try {
                    registry(monitorSpi);
                } catch (ClassNotFoundException e) {
                    logger.warn("monitor client class not found." ,e);
                } catch (Exception e) {
                    logger.error("");
                }


            }
        } catch (IOException e) {
            logger.error("", e);
        }



    }

    private static void registry(String monitorSpi) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class monitorSpiClass = Class.forName(monitorSpi);
        AbstractMonitorClient client = (AbstractMonitorClient)monitorSpiClass.newInstance();
        VenusMonitorDelegate.getInstance().addMonitorClient(client);
    }


    public static void main(String[] args) {
        MonitorExtensionResolver.resolver();
    }
}
