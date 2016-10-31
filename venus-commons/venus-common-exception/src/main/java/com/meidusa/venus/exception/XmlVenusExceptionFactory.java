package com.meidusa.venus.exception;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.xmlrules.FromXmlRuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.meidusa.toolkit.common.bean.PureJavaReflectionProvider;
import com.meidusa.toolkit.common.bean.ReflectionProvider;
import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.venus.annotations.ExceptionCode;
import com.meidusa.venus.annotations.RemoteException;
import com.meidusa.venus.digester.DigesterRuleParser;
import com.meidusa.venus.exception.xml.ExceptionConfig;
import com.meidusa.venus.util.ClasspathAnnotationScanner;

@SuppressWarnings("deprecation")
public class XmlVenusExceptionFactory implements VenusExceptionFactory {
    private static Logger logger = LoggerFactory.getLogger(XmlVenusExceptionFactory.class);
    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static boolean SCANNED = false;
    private static Map<Integer, ExceptionConfig> codeMap = new HashMap<Integer, ExceptionConfig>();
    private static Map<Class, ExceptionConfig> classMap = new HashMap<Class, ExceptionConfig>();
    static {
        Map<Class<?>,ExceptionCode> exceptionCodeMap = ClasspathAnnotationScanner.find(Exception.class,ExceptionCode.class);
        for(Map.Entry<Class<?>, ExceptionCode> entry: exceptionCodeMap.entrySet()){
            ExceptionConfig config = new ExceptionConfig();
            config.setErrorCode(entry.getValue().errorCode());
            config.setType(entry.getKey());
            codeMap.put(config.getErrorCode(), config);
            classMap.put(entry.getKey(), config);
        }
        
        Map<Class<?>,RemoteException> rMap = ClasspathAnnotationScanner.find(Exception.class,RemoteException.class);
        for(Map.Entry<Class<?>, RemoteException> entry: rMap.entrySet()){
            ExceptionConfig config = new ExceptionConfig();
            config.setErrorCode(entry.getValue().errorCode());
            config.setType(entry.getKey());
            codeMap.put(config.getErrorCode(), config);
            classMap.put(entry.getKey(), config);
        }
        
    }
   
    
    private static ReflectionProvider reflectionProvider = PureJavaReflectionProvider.getInstance();
    private String[] configFiles;

    public String[] getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(String[] configFiles) {
        this.configFiles = configFiles;
    }

    public void addException(Class<? extends CodedException> clazz) {
        CodedException exception = (CodedException) reflectionProvider.newInstance(clazz);
        ExceptionConfig config = new ExceptionConfig();
        config.setErrorCode(exception.getErrorCode());
        config.setType(clazz);
        codeMap.put(config.getErrorCode(), config);
        classMap.put(clazz, config);
    }

    @Override
    public Exception getException(int errcode, String message) {
        ExceptionConfig config = codeMap.get(errcode);
        if (config == null) {
            return new DefaultVenusException(errcode, message);
        } else {
            Constructor[] constructors = null;
            try {
                constructors = config.getType().getConstructors();
                for (Constructor constructor : constructors) {
                    if (Modifier.isPublic(constructor.getModifiers())) {
                        Class type[] = constructor.getParameterTypes();
                        if (type.length == 1) {
                            if (type[0] == String.class) {
                                try {
                                    return (Exception) constructor.newInstance(message);
                                } catch (Exception e) {
                                    logger.error("create exception instance error", e);
                                    return new DefaultVenusException(errcode, "create exception instance error", e);
                                }
                            }
                        } else if (type.length == 2) {
                            if (type[0] == String.class && type[1] == Throwable.class) {
                                try {
                                    return (Exception) constructor.newInstance(new Object[] { message, null });
                                } catch (Exception e) {
                                    logger.error("create exception instance error", e);
                                    return new DefaultVenusException(errcode, "create exception instance error", e);
                                }
                            } else if (type[0] == Throwable.class && type[1] == String.class) {
                                try {
                                    return (Exception) constructor.newInstance(new Object[] { null, message });
                                } catch (Exception e) {
                                    logger.error("create exception instance error", e);
                                    return new DefaultVenusException(errcode, "create exception instance error", e);
                                }
                            }
                        } else if (type.length == 0) {
                            try {
                                return (Exception) constructor.newInstance();
                            } catch (Exception e) {
                                logger.error("create exception instance error", e);
                                return new DefaultVenusException(errcode, "create exception instance error", e);
                            }
                        }
                    }
                }
            } catch (SecurityException e) {
                logger.error("exception new instance error", e);
            }
        }

        return new DefaultVenusException(errcode, message);
    }

    @Override
    public int getErrorCode(Class<? extends Throwable> clazz) {
        ExceptionConfig config = classMap.get(clazz);
        if (config != null) {
            return config.getErrorCode();
        }
        return 0;

    }

    public synchronized void doScanExtension() {
        if(SCANNED){
            return;
        }
        SCANNED = true;
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/META-INF/venus.exception.xml";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (logger.isInfoEnabled()) {
                    logger.info("Scanning " + resource);
                }
                if (resource.isReadable()) {
                    load(resource);
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Ignored because not readable: " + resource);
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("read venus exception xml error", ex);
        }
        
    }
    
    public void load(Resource resource){
        URL eis = this.getClass().getResource("VenusSystemExceptionRule.xml");
        if (eis == null) {
            throw new VenusConfigException("classpath resource 'VenusSystemExceptionRule.xml' not found");
        }
        RuleSet ruleSet = new FromXmlRuleSet(eis, new DigesterRuleParser());
        Digester digester = new Digester();
        digester.addRuleSet(ruleSet);
        try{
            List<ExceptionConfig> list = (List<ExceptionConfig>) digester.parse(resource.getInputStream());
            for (ExceptionConfig config : list) {
    
                if (config.getErrorCode() == 0) {
                    Exception exception = (Exception) reflectionProvider.newInstance(config.getType());
                    if (exception instanceof CodedException) {
                        config.setErrorCode(((CodedException) exception).getErrorCode());
                    } else {
                        throw new VenusConfigException("exception type=" + config.getType()
                                + " must implement CodedException or errorCode must not be null");
                    }
                }
    
                codeMap.put(config.getErrorCode(), config);
                classMap.put(config.getType(), config);
            }
        }catch(Exception e){
            try {
                logger.error("parser "+resource.getURL()+" error", e);
            } catch (IOException e1) {
                logger.error("parser "+resource.getFilename()+" error", e);
            }
        }finally{
            digester.clear();
        }
    }
    
    public void init() {
        doScanExtension();
        //兼容 3.0.8以前版本
        if(configFiles == null){
            return;
        }
        
        for (String configFile : configFiles) {
            configFile = (String) ConfigUtil.filter(configFile);
            configFile = configFile.trim();
            URL eis = this.getClass().getResource("VenusSystemExceptionRule.xml");
            if (eis == null) {
                throw new VenusConfigException("classpath resource 'VenusSystemExceptionRule.xml' not found");
            }
            RuleSet ruleSet = new FromXmlRuleSet(eis, new DigesterRuleParser());
            Digester digester = new Digester();
            digester.addRuleSet(ruleSet);

            try {
                InputStream is = null;
                if (configFile.startsWith("classpath:")) {
                    configFile = configFile.substring("classpath:".length());
                    is = this.getClass().getClassLoader().getResourceAsStream(configFile);
                } else {
                    is = new FileInputStream(new File(configFile));
                }
                List<ExceptionConfig> list = (List<ExceptionConfig>) digester.parse(is);
                for (ExceptionConfig config : list) {

                    if (config.getErrorCode() == 0) {
                        Exception exception = (Exception) reflectionProvider.newInstance(config.getType());
                        if (exception instanceof CodedException) {
                            config.setErrorCode(((CodedException) exception).getErrorCode());
                        } else {
                            throw new VenusConfigException("exception type=" + config.getType()
                                    + " must implement CodedException or errorCode must not be null");
                        }
                    }

                    codeMap.put(config.getErrorCode(), config);
                    classMap.put(config.getType(), config);
                }
            } catch (Exception e) {
                logger.error("parser VenusSystemExceptionRule.xml error", e);
            }finally{
                digester.clear();
            }
        }
    }

}
