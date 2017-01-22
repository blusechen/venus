/**
 * 
 */
package com.meidusa.venus.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常编码注释，该annotation只用于Exception class
 * venus 客户端、服务端启动之后开始扫描相关的类库，并且将errorCode 与Class 对应 注册到ExceptionFactory中
 * 方便用于Exception的序列化与反序列化
 * 接口中定义的Exception，务必要增加这个RemoteException注释
 * 
 * @author Struct
 * @since 3.1.0 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteException {
    
	public enum Level {
		ERROR(3), WARN(2), INFO(1), DEBUG(0);

		private int level;
		
		private Level(int level){
			this.level = level;
		}
		public int toInt() {
			return this.level;
		}
	}
	
    /**
     * 错误编码
     * @return
     */
    int errorCode();
    
    /**
     * 是否属于
     * @return
     */
    Level level() default Level.DEBUG;
}
