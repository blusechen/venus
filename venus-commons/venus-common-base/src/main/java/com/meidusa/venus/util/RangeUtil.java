package com.meidusa.venus.util;

import org.apache.commons.lang.StringUtils;

import com.meidusa.toolkit.common.util.StringUtil;

public class RangeUtil {
    public static Range getVersionRange(String version) {
        Range versionRange = null;
        if (!StringUtil.isEmpty(version)) {
            version = version.trim();
            String[] tmps = StringUtils.split(version, "{}[], ");
            int[] rages = new int[tmps.length];
            for (int i = 0; i < tmps.length; i++) {
                rages[i] = Integer.valueOf(tmps[i]);
            }

            if (version.startsWith("[")) {
                versionRange = new BetweenRange(rages);
            } else {
                versionRange = new ArrayRange(rages);
            }
            return versionRange;
        } else {
            return new DefaultRange();
        }
    }
}
