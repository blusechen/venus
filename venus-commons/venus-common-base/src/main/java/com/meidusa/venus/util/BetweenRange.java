package com.meidusa.venus.util;

import java.util.Arrays;

public class BetweenRange implements Range {
    private int[] arrays = null;

    public BetweenRange(int[] arrays) {
        this.arrays = arrays;
        if (arrays != null && arrays.length == 2) {
            Arrays.sort(this.arrays);
        } else {
            throw new InvalidParameterException("paramters size error");
        }
    }

    public int hashCode() {
        int i = 0;
        if (arrays != null) {
            for (int z : arrays) {
                i = i ^ z;
            }
        }
        return 142 + i;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof BetweenRange) {
            BetweenRange range = (BetweenRange) obj;
            return Arrays.equals(arrays, range.arrays);
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(int number) {
        return number >= arrays[0] && number <= arrays[1];
    }

    public String toString() {
        return Arrays.toString(arrays);
    }
}
