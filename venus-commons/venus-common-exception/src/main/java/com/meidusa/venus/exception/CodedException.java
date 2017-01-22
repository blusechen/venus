package com.meidusa.venus.exception;

/**
 * 异常接口，实现该接口，则可以将异常通过网络做序列化与反序列化 否则一些服务端的异常将以<code>DefaultVenusException</code>在客户端体现
 * 
 * @author structchen
 * 
 */
@Deprecated
public interface CodedException {
    int getErrorCode();

    String getMessage();
}
