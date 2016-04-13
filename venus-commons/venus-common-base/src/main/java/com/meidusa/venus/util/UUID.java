package com.meidusa.venus.util;

public class UUID {

    /*
     * The most significant 64 bits of this UUID.
     * @serial
     */
    private final long mostSigBits;

    /*
     * The least significant 64 bits of this UUID.
     * @serial
     */
    private final long leastSigBits;

    /*
     *  constructor which uses a byte array to construct the new UUID.
     */
    public UUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        this.mostSigBits = msb;
        this.leastSigBits = lsb;
    }

    public String toString() {
        return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-" + digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4)
                + "-" + digits(leastSigBits, 12));
    }
    
    public static String toString(byte[] data){
    	 long mostSigBits = 0;
         long leastSigBits = 0;
         assert data.length == 16;
         for (int i = 0; i < 8; i++)
        	 mostSigBits = (mostSigBits << 8) | (data[i] & 0xff);
         for (int i = 8; i < 16; i++)
        	 leastSigBits = (leastSigBits << 8) | (data[i] & 0xff);
         
         return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-" + digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4)
                 + "-" + digits(leastSigBits, 12));
    }

    /** Returns val represented by the specified number of hex digits. */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
    
    /**
     * Creates a <tt>UUID</tt> from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  name a string that specifies a <tt>UUID</tt>.
     * @return  a <tt>UUID</tt> with the specified value.
     * @throws IllegalArgumentException if name does not conform to the
     *         string representation as described in {@link #toString}.
     */
    public static UUID fromString(String name) {
        String[] components = name.split("-");
        if (components.length != 5)
            throw new IllegalArgumentException("Invalid UUID string: "+name);
        for (int i=0; i<5; i++)
            components[i] = "0x"+components[i];

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();

        return new UUID(mostSigBits, leastSigBits);
    }
    
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }
}