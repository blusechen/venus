/**
 * 
 */
package com.meidusa.venus.backend.view;

/**
 * @author sunning
 * 
 */
public abstract class AbstractXMLSerializer implements Serializer {

    /*
     * (non-Javadoc)
     * @see com.meidusa.relation.servicegate.view.Serializer#serialize(java.lang.Object)
     */
    public abstract String serialize(Object o) throws Exception;
}
