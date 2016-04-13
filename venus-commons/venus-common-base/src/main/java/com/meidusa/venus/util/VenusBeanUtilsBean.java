package com.meidusa.venus.util;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * @author daisy
 * 
 */
public class VenusBeanUtilsBean {

    private static BeanUtilsBean venusBUB;

    public static void setInstance(BeanUtilsBean beanUtilsBean) {
        venusBUB = beanUtilsBean;
    }

    public static BeanUtilsBean getInstance() {
        return venusBUB;
    }

}
