package com.meidusa.venus.client.spring;

import com.meidusa.venus.client.nio.RegistryManager;
import com.meidusa.venus.client.nio.RegistryServer;
import com.meidusa.venus.client.spring.exception.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huawei on 5/17/16.
 */
public class RegistryBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        String id = element.getAttribute("id");
        String remoteIpAddressList = element.getAttribute("remoteIpAddressList");

        if (!StringUtils.hasLength(id)) {
            throw new VenusRegistryIdNotEmptyException();
        }

        if (!StringUtils.hasLength(remoteIpAddressList)) {
            throw new VenusRegistryRemoteIpAddressNotEmptyExcepiton();
        }

        String[] registryRemoteIpAddressArray = remoteIpAddressList.split(",");

        if (registryRemoteIpAddressArray == null || registryRemoteIpAddressArray.length <= 0) {
            throw new VenusRegistryRemoteIpAddressNotEmptyExcepiton();
        }

        List<RegistryServer> registryServers = new ArrayList<RegistryServer>();
        for (String registryRemoteIpAddress : registryRemoteIpAddressArray) {
            String[] array = StringUtils.split(registryRemoteIpAddress, ":");
            if (array == null || array.length != 2) {
                throw new VenusRegistryRemoteIpAddressMalformationException();
            }

            String hostname = array[0];
            int port;
            try {
                port = Integer.parseInt(array[1]);
            } catch (Exception e) {
                throw new VenusRegistryServerPortMalformationException();
            }

            RegistryServer server = new RegistryServer();
            server.setHostname(hostname);
            server.setPort(port);

            registryServers.add(server);
        }

        if (registryServers.size() <= 0) {
            throw new VenusRegistryServerListNotEmptyException();
        }

        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClass(RegistryManager.class);
        rbd.getPropertyValues().add("servers", registryServers);

        parserContext.getRegistry().registerBeanDefinition(id, rbd);
        parserContext.getRegistry().registerBeanDefinition(RegistryManager.class.getName(), rbd);

        return rbd;
    }
}
