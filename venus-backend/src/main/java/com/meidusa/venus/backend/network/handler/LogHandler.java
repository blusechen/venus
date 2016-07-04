package com.meidusa.venus.backend.network.handler;

import com.meidusa.venus.exception.ExceptionLevel;
import org.slf4j.Logger;

/**
 * Created by godzillahua on 7/4/16.
 */
public class LogHandler {

    public static void logDependsOnLevel(ExceptionLevel level, Logger specifiedLogger, String msg, Throwable e) {
        switch (level) {
            case DEBUG:
                specifiedLogger.debug(msg, e);
                break;
            case INFO:
                specifiedLogger.info(msg, e);
                break;
            case TRACE:
                specifiedLogger.trace(msg, e);
                break;
            case WARN:
                specifiedLogger.warn(msg, e);
                break;
            case ERROR:
                specifiedLogger.error(msg, e);
                break;
            default:
                break;
        }
    }
}
