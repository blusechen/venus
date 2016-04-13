/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
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

package com.meidusa.venus.client;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.venus.notify.InvocationListener;

public class InvocationListenerContainer {
    private Map<String, Map<Integer, Tuple<InvocationListener, Type>>> listenerMap = new HashMap<String, Map<Integer, Tuple<InvocationListener, Type>>>();

    @SuppressWarnings("unchecked")
    public void putInvocationListener(InvocationListener object, Type type) {
        Map<Integer, Tuple<InvocationListener, Type>> map = listenerMap.get(object.getClass().getName());
        if (map == null) {
            synchronized (listenerMap) {
                map = listenerMap.get(object.getClass().getName());
                if (map == null) {
                    map = new HashMap<Integer, Tuple<InvocationListener, Type>>();
                    listenerMap.put(object.getClass().getName(), map);
                }
            }
        }
        map.put(System.identityHashCode(object), new Tuple(object, type));
    }

    @SuppressWarnings("unchecked")
    public Tuple<InvocationListener, Type> getInvocationListener(String clazz, int identityHashCode) {
        Map<Integer, Tuple<InvocationListener, Type>> map = listenerMap.get(clazz);
        if (map == null) {
            synchronized (listenerMap) {
                map = listenerMap.get(clazz);
                if (map == null) {
                    map = new HashMap<Integer, Tuple<InvocationListener, Type>>();
                    listenerMap.put(clazz, map);
                    return null;
                }
            }
        }

        Tuple<InvocationListener, Type> tuple = map.get(identityHashCode);
        return tuple;
    }

}
