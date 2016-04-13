package com.meidusa.venus.util;

import java.util.Arrays;

public class ArrayRange implements Range {
    private int[] arrays = null;

    public ArrayRange(int[] arrays) {
        this.arrays = arrays;
        Arrays.sort(this.arrays);
    }

    @Override
    public boolean contains(int number) {
        return Arrays.binarySearch(arrays, number) >= 0;
    }

    public int hashCode() {
        int i = 0;
        if (arrays != null) {
            for (int z : arrays) {
                i = i ^ z;
            }
        }
        return 42 + i;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof ArrayRange) {
            ArrayRange range = (ArrayRange) obj;
            return Arrays.equals(arrays, range.arrays);
        } else {
            return false;
        }
    }

    public String toString() {
        if (arrays == null)
            return "null";
        int iMax = arrays.length - 1;
        if (iMax == -1)
            return "{}";

        StringBuilder b = new StringBuilder();
        b.append('{');
        for (int i = 0;; i++) {
            b.append(arrays[i]);
            if (i == iMax)
                return b.append('}').toString();
            b.append(", ");
        }
    }
    
    public static void main(String[] args) {
		Range range = new ArrayRange(new int[]{2});
		System.out.println(range.contains(2));
	}
}
