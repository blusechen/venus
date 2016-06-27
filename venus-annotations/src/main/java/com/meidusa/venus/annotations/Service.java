package com.meidusa.venus.annotations;

import java.lang.annotation.*;

/**
 * specify service name
 * 
 * @author Sun Ning
 *
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    
   
    /**
     * 默认的服务名称为short className
     * @return
     */
    String name() default "";
    
    /**
     * 用于implement不为空的情况，实例化implement() 相关的类，并且决定是否是单例。
     * @return
     */
    boolean singleton() default true;
    
    /**
     * 服务版本号
     * @return
     */
    int version() default 0;
    /**
     * 该Service接口的本地默认实现，该字段不为空，则会采用implement()返回的类名进行相关的方法调用
     * @return
     */
    String implement() default "";
    
    /**
     * 服务描述
     * @return
     */
    String description() default "";

}