/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.*;

/**
 * 异常编码注释，该annotation只用于Exception class
 * venus 客户端、服务端启动之后开始扫描相关的类库，并且将exceptionCode 与Class 对应 注册到ExceptionFactory中
 * 方便用于Exception的序列化与反序列化
 * 接口中定义的Exception，务必要增加这个ExcetionCode注释
 * 
 * @author Struct
 * @since 3.0.9 
 * @deprecated replaced by {@code RemoteException}
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionCode {
    
    /**
     * 错误编码
     * @return
     */
    int errorCode();
}
