/**
 * 
 */
package com.meidusa.venus.backend.services;

/**
 * Manages only one instance
 * 
 * @author Sun Ning
 * @since 2010-3-4
 */
public class SingletonService extends Service {

    private Object instance;

    /**
     * @return the instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * @param instance the instance to set
     */
    public void setInstance(Object instance) {
        this.instance = instance;
    }

}
