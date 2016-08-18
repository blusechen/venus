package com.meidusa.venus.extension.athena;

import java.io.IOException;
import java.io.InputStream;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.meidusa.venus.extension.athena.delegate.AthenaReporterDelegate;
import com.meidusa.venus.extension.athena.delegate.AthenaTransactionDelegate;

/**
 * Created by GodzillaHua on 7/3/16.
 */
public final class AthenaExtensionResolver {

    private static Logger logger = LoggerFactory.getLogger(AthenaExtensionResolver.class);

    private static AthenaExtensionResolver instance = new AthenaExtensionResolver();
    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private AthenaExtensionResolver(){

    }

    public static AthenaExtensionResolver getInstance() {
        return instance;
    }

    public void resolver(){
        Ini ini = new Ini();
        String athenaExtensionIniLocation = PathMatchingResourcePatternResolver.CLASSPATH_URL_PREFIX + "/META-INF/venus.extension.athena.ini";
        Resource resource = resourcePatternResolver.getResource(athenaExtensionIniLocation);
        if (resource.exists()) {
        	InputStream is = null; 
            try {
            	is = resource.getInputStream();
                ini.load(is);
            } catch (IOException e) {
                logger.error("load athena ini file error", e);
                return;
            }finally {
                try{
                	if(is != null){
                		is.close();
                	}
                }catch (Exception e) {
                    logger.warn("resource cannot be close correctly", e);
                    //ignore
                }
            }

            Profile.Section metricSection = ini.get("metric");
            if (metricSection != null) {
                String metricReportClassName = metricSection.get("metric.reporter");
                AthenaMetricReporter metricReporter = newInstance(metricReportClassName);
                AthenaReporterDelegate.getDelegate().setMetricReporter(metricReporter);
            }

            Profile.Section problemSection = ini.get("problem");
            if (problemSection != null) {
                String problemReportClassName = problemSection.get("problem.reporter");
                AthenaProblemReporter problemReporter = newInstance(problemReportClassName);
                AthenaReporterDelegate.getDelegate().setProblemReporter(problemReporter);
            }

            Profile.Section transactionSection = ini.get("transaction");
            if (transactionSection != null) {
                String clientTransactionClassName = transactionSection.get("client.transaction");
                AthenaClientTransaction clientTransaction = newInstance(clientTransactionClassName);
                AthenaTransactionDelegate.getDelegate().setClientTransactionReporter(clientTransaction);
            }

            if (transactionSection != null) {
                String serverTransactionClassName = transactionSection.get("server.transaction");
                AthenaServerTransaction serverTransaction = newInstance(serverTransactionClassName);
                AthenaTransactionDelegate.getDelegate().setServerTransactionReporter(serverTransaction);
            }
        }
    }

    private <T> T newInstance(String className){

        if (className == null) {
            return null;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.error("load class error " + className, e);
            return null;
        }

        try {
            return (T) (clazz.newInstance());
        } catch (InstantiationException e) {
            logger.error("instantiate class error " + className, e);
            return null;
        } catch (IllegalAccessException e) {
            logger.error("class cannot be access error " + className, e);
            return null;
        }
    }

    public static void main(String[] args) {
        AthenaExtensionResolver.getInstance().resolver();
    }

}
