package com.meidusa.venus.metainfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.venus.annotations.Param;
import com.meidusa.venus.annotations.util.AnnotationUtil;

public class EndpointParameterUtil {
    private static Map<Method, EndpointParameter[]> parameterMap = new HashMap<Method, EndpointParameter[]>();

    public static EndpointParameter[] getPrameters(Method method) {
        EndpointParameter[] params = parameterMap.get(method);
        if (params == null) {
            synchronized (parameterMap) {

                params = parameterMap.get(method);
                if (params == null) {
                    Type[] itypes = method.getGenericParameterTypes();
                    Annotation[][] annotations = method.getParameterAnnotations();
                    params = new EndpointParameter[itypes.length];
                    for (int i = 0; i < itypes.length; i++) {
                        Param param = AnnotationUtil.getAnnotation(annotations[i], Param.class);

                        params[i] = new EndpointParameter();
                        params[i].setParamName(param.name());
                        params[i].setType(itypes[i]);
                        params[i].setOptional(param.optional());
                        params[i].setDefaultValue(param.defaultValue());
                    }
                    parameterMap.put(method, params);
                }

            }
        }

        return params;
    }
}
