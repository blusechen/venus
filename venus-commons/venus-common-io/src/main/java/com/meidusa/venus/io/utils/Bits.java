/*
 * @(#)Bits.java	1.4 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.meidusa.venus.io.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Utility methods for packing/unpacking primitive values in/out of byte arrays using big-endian byte ordering.
 */
public class Bits {
    private final static int CRC_CHECKSUM = new Random().nextInt(Short.MAX_VALUE) & 0xFFFF;

    /*
     * Methods for unpacking primitive values from byte arrays starting at given offsets.
     */

    public static int readInt(InputStream in) throws IOException {
        int x = 0;
        x |= (0xFF & in.read()) << 0;
        x |= (0xFF & in.read()) << 8;
        x |= (0xFF & in.read()) << 16;
        x |= (0xFF & in.read()) << 24;
        return x;
    }

    public static long readLong(InputStream in) throws IOException {
        long x = 0;
        x |= (long) (0xFFL & in.read()) << 0;
        x |= (long) (0xFFL & in.read()) << 8;
        x |= (long) (0xFFL & in.read()) << 16;
        x |= (long) (0xFFL & in.read()) << 24;
        x |= (long) (0xFFL & in.read()) << 32;
        x |= (long) (0xFFL & in.read()) << 40;
        x |= (long) (0xFFL & in.read()) << 48;
        x |= (long) (0xFFL & in.read()) << 56;
        return x;
    }

    public static boolean getBoolean(byte[] b, int off) {
        return b[off] != 0;
    }

    public static char getChar(byte[] b, int off) {
        return (char) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
    }

    public static short getShort(byte[] b, int off) {
        return (short) (((b[off + 1] & 0xFF) << 0) + ((b[off + 0] & 0xFF) << 8));
    }

    public static int getInt(byte[] b, int off) {
        return ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF) << 24);
    }

    public static float getFloat(byte[] b, int off) {
        int i = ((b[off + 3] & 0xFF) << 0) + ((b[off + 2] & 0xFF) << 8) + ((b[off + 1] & 0xFF) << 16) + ((b[off + 0] & 0xFF) << 24);
        return Float.intBitsToFloat(i);
    }

    public static long getLong(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24)
                + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
    }

    public static double getDouble(byte[] b, int off) {
        long j = ((b[off + 7] & 0xFFL) << 0) + ((b[off + 6] & 0xFFL) << 8) + ((b[off + 5] & 0xFFL) << 16) + ((b[off + 4] & 0xFFL) << 24)
                + ((b[off + 3] & 0xFFL) << 32) + ((b[off + 2] & 0xFFL) << 40) + ((b[off + 1] & 0xFFL) << 48) + ((b[off + 0] & 0xFFL) << 56);
        return Double.longBitsToDouble(j);
    }

    /*
     * Methods for packing primitive values into byte arrays starting at given offsets.
     */

    public static void putBoolean(byte[] b, int off, boolean val) {
        b[off] = (byte) (val ? 1 : 0);
    }

    public static void putChar(byte[] b, int off, char val) {
        b[off + 1] = (byte) (val >>> 0);
        b[off + 0] = (byte) (val >>> 8);
    }

    public static void putShort(byte[] b, int off, short val) {
        b[off + 1] = (byte) (val >>> 0);
        b[off + 0] = (byte) (val >>> 8);
    }

    public static void putInt(byte[] b, int off, int val) {
        b[off + 3] = (byte) (val >>> 0);
        b[off + 2] = (byte) (val >>> 8);
        b[off + 1] = (byte) (val >>> 16);
        b[off + 0] = (byte) (val >>> 24);
    }

    public static void putFloat(byte[] b, int off, float val) {
        int i = Float.floatToIntBits(val);
        b[off + 3] = (byte) (i >>> 0);
        b[off + 2] = (byte) (i >>> 8);
        b[off + 1] = (byte) (i >>> 16);
        b[off + 0] = (byte) (i >>> 24);
    }

    public static void putLong(byte[] b, int off, long val) {
        b[off + 7] = (byte) (val >>> 0);
        b[off + 6] = (byte) (val >>> 8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off + 0] = (byte) (val >>> 56);
    }

    public static void putDouble(byte[] b, int off, double val) {
        long j = Double.doubleToLongBits(val);
        b[off + 7] = (byte) (j >>> 0);
        b[off + 6] = (byte) (j >>> 8);
        b[off + 5] = (byte) (j >>> 16);
        b[off + 4] = (byte) (j >>> 24);
        b[off + 3] = (byte) (j >>> 32);
        b[off + 2] = (byte) (j >>> 40);
        b[off + 1] = (byte) (j >>> 48);
        b[off + 0] = (byte) (j >>> 56);
    }

    public static int checksum(byte[] buf, int offset, int len) {
        int crc = CRC_CHECKSUM;

        for (int pos = offset; pos < len; pos++) {
            crc ^= (int) buf[pos]; // XOR byte into least sig. byte of crc

            for (int i = 8; i != 0; i--) { // Loop over each bit
                if ((crc & 0x0001) != 0) { // If the LSB is set
                    crc >>= 1; // Shift right and XOR 0xA001
                    crc ^= 0xA001;
                } else
                    // Else LSB is not set
                    crc >>= 1; // Just shift right
            }
        }
        // Note, this number has low and high bytes swapped, so use it
        // accordingly (or swap bytes)
        return crc;
    }

}
