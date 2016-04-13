package com.meidusa.venus.backend.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.util.Log4jConfigurer;

import com.meidusa.toolkit.common.bean.config.ConfigUtil;

/**
 * 
 * @author structchen
 * 
 */
public class VenusJettyServer {

    private static final int PORT = 8080;

    private Server server;
    private int port = PORT;
    private String contextPath = "/";
    private String webapp = "./webapp";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String context) {
        this.contextPath = context;
    }

    public String getWebapp() {
        return webapp;
    }

    public void setWebapp(String webapp) {
        this.webapp = webapp;
    }

    public void start() throws Exception {
        server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);

        WebAppContext context = new WebAppContext();
        context.setContextPath(contextPath);
        context.setResourceBase(webapp);
        context.setConfigurationDiscovered(true);

        server.setHandler(context);
        server.start();
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();

        System.setProperty("project.home", ConfigUtil.filter("${project.home:.}", System.getProperties()));
        String projectHome = System.getProperty("project.home");
        properties.load(new FileInputStream(new File(ConfigUtil.filter("${project.home:.}", System.getProperties()), "application.properties")));
        ConfigUtil.addProperties(properties);
        __INIT_LOG: {
            String logbackConf = System.getProperty("logback.configurationFile", "${project.home}/conf/logback.xml");
            logbackConf = ConfigUtil.filter(logbackConf, System.getProperties());

            File logbackFile = new File(logbackConf);
            if (logbackFile.exists()) {
                System.out.println("Log system load configuration form " + logbackConf);
                System.setProperty("logback.configurationFile", logbackConf);
                break __INIT_LOG;
            }

            String log4jConf = System.getProperty("log4j.configuration", "${project.home}/conf/log4j.xml");
            log4jConf = ConfigUtil.filter(log4jConf, System.getProperties());
            File log4jFile = new File(log4jConf);

            if (!log4jFile.exists()) {
                log4jConf = System.getProperty("log4j.configuration", "${project.home}/conf/log4j.properties");
                log4jConf = ConfigUtil.filter(log4jConf, System.getProperties());
                log4jFile = new File(log4jConf);
            }

            if (log4jFile.exists()) {
                try {
                    System.setProperty("log4j.configuration", log4jConf);
                    System.out.println("Log system load configuration form " + log4jConf);

                    Log4jConfigurer.initLogging(log4jConf, 30 * 1000);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    break __INIT_LOG;
                } catch (FileNotFoundException e1) {
                    // INGORE
                }
            }
        }

        VenusJettyServer server = new VenusJettyServer();
        server.setContextPath(ConfigUtil.filter("${webapp.context:/}").toString());
        server.setWebapp(ConfigUtil.filter("${webapp.dir:" + projectHome + "/webapp}").toString());
        server.setPort(Integer.parseInt(ConfigUtil.filter("${webapp.port:8080}").toString()));

        server.start();
    }

}
