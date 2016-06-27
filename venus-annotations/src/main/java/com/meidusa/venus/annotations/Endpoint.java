/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.*;

/**
 * Annotation to expose a service method with Service Gate Container.
 * 
 * @author Sun Ning
 * @author structchen
 * @since 2010-3-5
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Endpoint {

    /**
     * 对外暴露的名称，默认为该方法的方法名
     * 
     * @return 接口名称
     */
    String name() default "";

    /**
     * 是否是异步，默认为：false
     * 
     * @return 是否是异步
     */
    boolean async() default false;

    /**
     * <li>客户端：等待该方法返回结果的超时时间 <li>服务端：客服端请求进入队列，等待执行线程开始执行的时间 <li>时间单位：毫秒
     * 
     * @return 超时时间 (单位：毫秒)
     */
    int timeWait() default 30000;

    /**
     * 用户负载均衡算法的Key
     * 
     * @return
     */
    String loadbalancingKey() default "";
}
