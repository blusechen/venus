package com.meidusa.venus.util;

public class DefaultRange implements Range {

    public int hashCode() {
        return 242;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof DefaultRange) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(int number) {
        return true;
    }

    public String toString() {
        return "";
    }
}
