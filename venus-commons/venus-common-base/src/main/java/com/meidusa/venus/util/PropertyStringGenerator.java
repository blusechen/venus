package com.meidusa.venus.util;

public abstract class PropertyStringGenerator {
    private String suffix;
    private String prefix;

    protected void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    protected void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getKey(String str, Object object) {
        StringBuffer buffer = new StringBuffer();
        while (true) {
            int pre = str.indexOf(prefix);
            int suf = str.indexOf(suffix);
            if (pre != -1 && suf != -1) {
                String key = str.substring(pre + prefix.length(), suf + 1 - suffix.length());
                buffer.append(str.substring(0, pre));
                buffer.append(this.getString(key, object));
            } else {
                buffer.append(str);
                break;
            }
            str = str.substring(suf + 1, str.length());
        }

        return buffer.toString();

    };

    protected abstract String getString(String key, Object object);

    public PropertyStringGenerator(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static class MyKeyGenerationUtil extends PropertyStringGenerator {

        public MyKeyGenerationUtil(String prefix, String suffix) {
            super(prefix, suffix);
        }

        @Override
        protected String getString(String key, Object object) {
            return key + key;
        }

    }

    public static void main(String[] args) {
        MyKeyGenerationUtil util = new MyKeyGenerationUtil("${", "}");
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            util.getKey("a ${xxx}, ${xxxx}", null);

        }
        System.out.println(System.currentTimeMillis() - begin);
        Runtime runtime = Runtime.getRuntime();
        System.out.println(runtime.freeMemory());

    }
}
