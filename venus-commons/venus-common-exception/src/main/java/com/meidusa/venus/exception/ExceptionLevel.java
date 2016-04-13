package com.meidusa.venus.exception;

public enum ExceptionLevel {

    DEBUG(0), INFO(1), TRACE(2), WARN(3), ERROR(4);

    ExceptionLevel(int level) {
        this.levelNum = level;
    }

    private int levelNum = 0;

    boolean isDebugEnabled() {
        return levelNum >= 0;
    }

    boolean isInfoEnabled() {
        return levelNum >= 1;
    }

    boolean isWarnEnabled() {
        return levelNum >= 3;
    }

    boolean isErrorEnabled() {
        return levelNum >= 4;
    }
}
