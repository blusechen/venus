package com.meidusa.venus.digester;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * 
 * @author Struct
 * 
 */
public class BeanPropertySetterRuleFactory extends AbstractObjectCreationFactory {

    public Object createObject(Attributes attributes) throws Exception {
        Rule beanPropertySetterRule = null;
        String propertyname = attributes.getValue("propertyname");
        String attrname = attributes.getValue("attrname");
        if (propertyname == null && attrname == null) {
            // call the setter method corresponding to the element name.
            beanPropertySetterRule = new BeanPropertySetterRule();
        } else {
            beanPropertySetterRule = new BeanPropertySetterRule(propertyname, attrname);
        }

        return beanPropertySetterRule;
    }

}
