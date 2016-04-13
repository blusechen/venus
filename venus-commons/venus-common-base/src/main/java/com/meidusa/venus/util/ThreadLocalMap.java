/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.venus.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal Context
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * @version $Id: ThreadLocalContext.java 3597 2006-11-23 08:11:58Z struct $
 */
public class ThreadLocalMap {
    private static Logger logger = LoggerFactory.getLogger(ThreadLocalMap.class);

    protected final static ThreadLocal<Map<Object, Object>> threadContext = new MapThreadLocal();

    private ThreadLocalMap() {
    };

    public static void put(Object key, Object value) {
        getContextMap().put(key, value);
    }

    public static Object remove(Object key) {
        return getContextMap().remove(key);
    }

    public static Object get(Object key) {
        return getContextMap().get(key);
    }

    public static boolean containsKey(Object key) {
        return getContextMap().containsKey(key);
    }

    private static class MapThreadLocal extends ThreadLocal<Map<Object, Object>> {
        protected Map<Object, Object> initialValue() {
            return new HashMap<Object, Object>() {

                private static final long serialVersionUID = 3637958959138295593L;

                public Object put(Object key, Object value) {
                    if (logger.isDebugEnabled()) {
                        if (containsKey(key)) {
                            logger.debug("Overwritten attribute to thread context: " + key + " = " + value);
                        } else {
                            logger.debug("Added attribute to thread context: " + key + " = " + value);
                        }
                    }

                    return super.put(key, value);
                }
            };
        }
    }

    protected static Map<Object, Object> getContextMap() {
        return (Map<Object, Object>) threadContext.get();
    }

    public static void reset() {
        getContextMap().clear();
    }
}
