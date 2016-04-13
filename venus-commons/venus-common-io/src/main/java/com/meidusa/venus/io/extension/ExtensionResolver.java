package com.meidusa.venus.io.extension;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.meidusa.fastbson.FastBsonSerializer;
import com.meidusa.fastjson.parser.ParserConfig;
import com.meidusa.fastjson.serializer.SerializeConfig;
import com.meidusa.toolkit.common.bean.PureJavaReflectionProvider;

public class ExtensionResolver {
    protected static final Logger logger = LoggerFactory.getLogger(ExtensionResolver.class);
    private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public static void doScanExtension() {
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/META-INF/venus.io.extension.ini";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (logger.isInfoEnabled()) {
                    logger.info("Scanning " + resource);
                }
                if (resource.isReadable()) {
                    try {
                        Ini ini = new Ini();
                        ini.load(resource.getInputStream());

                        Section section = ini.get("bson");
                        if (section != null) {
                            register(section, 1);
                        }

                        Section decoder = ini.get("json.decoder");
                        if (decoder != null) {
                            register(decoder, 2);
                        }

                        Section encoder = ini.get("json.encoder");
                        if (encoder != null) {
                            register(encoder, 3);
                        }

                    } catch (Throwable ex) {
                        logger.error("register extension error", ex);
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Ignored because not readable: " + resource);
                    }
                }
            }
        } catch (IOException ex) {
            logger.error("register extension error", ex);
        }
    }

    private static void register(Section section, int type) {
        Set<Entry<String, String>> sets = section.entrySet();
        for (Entry<String, String> entry : sets) {
            String key = entry.getKey();
            String value = entry.getValue();
            String itype = null;
            if (!StringUtils.isEmpty(value) && !StringUtils.isEmpty(key)) {
                try {
                    Class<?> bean = Class.forName(value.trim());
                    Object object = PureJavaReflectionProvider.getInstance().newInstance(bean);
                    Class<?> clazz = Class.forName(key);
                    if (type == 1) {
                        itype = "bson";
                        FastBsonSerializer.registerSerializer(clazz, (com.meidusa.fastbson.serializer.ObjectSerializer) object);
                    } else if (type == 2) {
                        itype = "json Deserializer";
                        ParserConfig.getGlobalInstance().putDeserializer(clazz, (com.meidusa.fastjson.parser.deserializer.ObjectDeserializer) object);
                    } else if (type == 3) {
                        itype = "json Serializer";
                        SerializeConfig.getGlobalInstance().put(clazz, (com.meidusa.fastjson.serializer.ObjectSerializer) object);
                    }
                    logger.info("register type=" + itype + ", key=" + key + ", class=" + value);
                } catch (ClassNotFoundException e) {
                    logger.error("register error,key=" + key + ",value=" + value, e);
                }

            }
        }
    }

}
