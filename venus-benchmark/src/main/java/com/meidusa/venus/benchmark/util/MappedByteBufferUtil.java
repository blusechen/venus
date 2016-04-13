package com.meidusa.venus.benchmark.util;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedByteBufferUtil {
    private static Logger logger = LoggerFactory.getLogger(MappedByteBufferUtil.class);
    /**
     * MappedByteBuffer只能通过调用FileChannel的map()取得, SUN提供了map()却没有提供unmap(). 因为在File.delete()时，会返回false,导致无法删除被映射过的文件。
     * 因此该方法只是一种解决 取消映射 的方法。
     * 
     * @param buffer
     */
    private static Method GetCleanerMethod;
    private static Method cleanMethod;
    static {
        try {
            if (GetCleanerMethod == null) {

                Method getCleanerMethod = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner", new Class[0]);
                getCleanerMethod.setAccessible(true);
                GetCleanerMethod = getCleanerMethod;
                cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean", new Class[0]);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void unmap(final MappedByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        synchronized (buffer) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        // sun.misc.Cleaner
                        Object cleaner = GetCleanerMethod.invoke(buffer, new Object[0]);
                        cleanMethod.invoke(cleaner, new Object[0]);
                    } catch (Exception e) {
                        logger.error("unmap  MappedByteBuffer error", e);
                    }
                    return null;
                }
            });
        }
    }
}
