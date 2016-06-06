package com.meidusa.venus.client.spring;

/**
 * Created by huawei on 5/18/16.
 */
public class NodeConstants {

    public static final String ELEMENT_REGISTRY_NAME = "registry";
    public static final String ATTRIBUTE_REGISTRY_ID_NAME = "id";
    public static final String ATTRIBUTE_REGISTRY_ADDRESS_LIST_NAME = "addressList";

    public static final String ELEMENT_SERVICES_NAME = "services";
    public static final String ATTRIBUTE_SERVICES_ID = "id";
    public static final String ATTRIBUTE_SERVICES_REF_REGISTRY_ID_NAME = "ref-registry-id";
    public static final String ELEMENT_SERVICE_AUTHENTICATION_DUMMY_AUTHENTICATOR = "dummyAuthentication";
    public static final String ELEMENT_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR = "userNamePasswordAuthentication";
    public static final String ATTRIBUTE_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR_USERNAME = "username";
    public static final String ATTRIBUTE_SERVICE_AUTHENTICATION_USERNAME_AUTHENTICATOR_PASSWORD = "password";
    public static final String ATTRIBUTE_SERVICE_SERIALIZER_TYPE = "serializerType";

    public static final String ELEMENT_SERVICE_NAME = "service";
    public static final String ATTRIBUTE_SERVICE_INTERFACE_NAME = "interface";
    public static final String ATTRIBUTE_SERVICE_OVERRIDE_NAME = "override";
    public static final String ATTRIBUTE_SERVICE_ADDRESS_LIST_NAME = "addressList";
    public static final String ATTRIBUTE_SERVICE_MAX_ACTIVE_NAME = "maxActive";
    public static final String ATTRIBUTE_SERVICE_MAX_IDLE_NAME = "maxIdle";
    public static final String ATTRIBUTE_SERVICE_MIN_IDLE_NAME = "minIdle";
    public static final String ATTRIBUTE_SERVICE_MIN_EVICTABLE_IDLE_TIME_MILLIS_NAME = "minEvictableIdleTimeMillis";
    public static final String ATTRIBUTE_SERVICE_TIME_BETWEEN_EVICTION_RUNS_MILLS_NAME = "timeBetweenEvictionRunsMillis";
    public static final String ATTRIBUTE_SERVICE_TEST_ON_BORROW_NAME = "testOnBorrow";
    public static final String ATTRIBUTE_SERVICE_TEST_WHILE_IDLE = "testWhileIdle";
    public static final String ATTRIBUTE_SERVICE_TIME_WAIT = "timeWait";
}
