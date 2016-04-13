package com.meidusa.venus.backend.authenticate;

import com.meidusa.toolkit.common.util.Tuple;
import com.meidusa.venus.io.network.VenusFrontendConnection;

public class SimpleAuthenticateProvider<T extends VenusFrontendConnection> extends AbstractAuthenticateProvider<T,Tuple<Long, byte[]>> {

    @Override
    protected byte[] transferData(Tuple<Long, byte[]> data) {
        return data.right;
    }
}
