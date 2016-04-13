package com.meidusa.venus.backend.authenticate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.net.authenticate.server.AuthenticateProvider;
import com.meidusa.venus.VenusMetaInfo;
import com.meidusa.venus.exception.CodedException;
import com.meidusa.venus.exception.VenusExceptionCodeConstant;
import com.meidusa.venus.io.network.VenusFrontendConnection;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AuthenPacket;
import com.meidusa.venus.io.packet.AuthenPacketFactory;
import com.meidusa.venus.io.packet.DummyAuthenPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.PasswordAuthenPacket;
import com.meidusa.venus.io.utils.StringUtil;

public abstract class AbstractAuthenticateProvider<T extends VenusFrontendConnection,V> extends AuthenticateProvider<T, V> {
    private static Logger logger = LoggerFactory.getLogger(SimpleAuthenticateProvider.class);
    private String username;
    private String password;
    private boolean useDummy = true;

    public boolean isUseDummy() {
        return useDummy;
    }

    public void setUseDummy(boolean useDummy) {
        this.useDummy = useDummy;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void beforeAuthing(T conn) {
        HandshakePacket handshake = new HandshakePacket();
        handshake.challenge = StringUtil.getRandomString(12);
        handshake.version = VenusMetaInfo.VENUS_VERSION;
        conn.setSeed(handshake.challenge);
        conn.write(handshake.toByteBuffer());
    }

    public void handle(T conn, V data){
        handleData(conn,transferData(data));
    }
    
    protected abstract byte[] transferData(V data);
    
    protected void handleData(T conn, byte[] message) {

        /** 此时接收到的应该是认证数据，保存数据为认证提供数据 */
        int type = AbstractServicePacket.getType(message);

        if (type != PacketConstant.PACKET_TYPE_AUTHEN) {
            ErrorPacket error = new ErrorPacket();
            error.message = "connection must be authenticated before it send other type packet!";
            error.errorCode = VenusExceptionCodeConstant.AUTHEN_EXCEPTION;
            conn.write(error.toByteBuffer());
            return;
        }

        AuthenPacket authenPacket;
        try {
            authenPacket = AuthenPacketFactory.getInstance().createAuthenPacket(message);

            // set connection context
            {
                conn.setClientId(authenPacket.clientId);
                conn.setSerializeType(authenPacket.shakeSerializeType);
            }
            if (this.isUseDummy()) {
                conn.setAuthenticated(true);
                conn.write(new OKPacket().toByteBuffer());
            } else {
                if (authenPacket instanceof PasswordAuthenPacket) {
                    PasswordAuthenPacket passPacket = (PasswordAuthenPacket) authenPacket;
                    boolean isSuccess = false;
                    if (StringUtils.equals(this.username, passPacket.username)) {
                        isSuccess = true;
                    }
                    if (isSuccess) {
                        byte[] pBytes = PasswordAuthenPacket.encryptPasswd(password, conn.getSeed());
                        isSuccess = ArrayUtils.isEquals(pBytes, passPacket.encryptedPasswd);
                    }
                    if (isSuccess) {
                        onAuthingSuccess(conn);
                    } else {
                        onAuthingError(conn, "username or password error", VenusExceptionCodeConstant.AUTHEN_EXCEPTION);
                    }

                    if (logger.isInfoEnabled()) {
                        logger.info("Accepting authenticate request: conn=" + conn.getId() + ", using PasswordAuthenPacket,username="+passPacket.username+", result=" + isSuccess);
                    }
                    return;
                } else if (authenPacket instanceof DummyAuthenPacket) {
                    onAuthingError(conn, " Dummy authentication not support!!", VenusExceptionCodeConstant.AUTHEN_EXCEPTION);

                    if (logger.isInfoEnabled()) {
                        logger.info("Accepting authenticate request: conn=" + conn.getId() + ", using DummyAuthenPacket, result=false");
                    }
                    return;
                } else {
                    onAuthingError(conn, " Unknown the authentication type ", VenusExceptionCodeConstant.AUTHEN_EXCEPTION);
                    if (logger.isInfoEnabled()) {
                        logger.info("Accepting authenticate request: conn=" + conn.getId() + ", result=false");
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof CodedException) {
                onAuthingError(conn, e.getMessage(), ((CodedException) e).getErrorCode());
            } else {
                onAuthingError(conn, e.getMessage(), VenusExceptionCodeConstant.SERVICE_UNAVAILABLE_EXCEPTION);
            }
            return;
        }

    }

    protected void onAuthingError(T conn, String message, int errorCode) {
        ErrorPacket error = new ErrorPacket();
        error.message = message;
        error.errorCode = errorCode;
        conn.write(error.toByteBuffer());
    }

    protected void onAuthingSuccess(T conn) {
        conn.setAuthenticated(true);
        conn.write(new OKPacket().toByteBuffer());
    }
}
