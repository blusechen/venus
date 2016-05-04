package com.meidusa.venus.io.packet;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.StringUtils;

import com.meidusa.venus.io.utils.StringUtil;

public class PasswordAuthenPacket extends DummyAuthenPacket {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    public transient String password;
    public transient String challenge;
    public byte[] encryptedPasswd;

    public PasswordAuthenPacket() {
        this.authType = PacketConstant.AUTHEN_TYPE_PASSWORD;
    }

    @Override
    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        encryptedPasswd = encryptPasswd(password, challenge);
        //it was a bug, username write twice 
        buffer.writeLengthCodedString(username,PACKET_CHARSET);
        buffer.writeLengthCodedBytes(encryptedPasswd);

    }

    @Override
    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        //it was a bug, username write twice 
        username = buffer.readLengthCodedString(PACKET_CHARSET);
        this.encryptedPasswd = buffer.readLengthCodedBytes();
    }

    public static byte[] encryptPasswd(String password, String challenge) {
        if (!StringUtils.isEmpty(challenge) && !StringUtils.isEmpty(password)) {
            String passwdMd5;
            try {
                passwdMd5 = StringUtil.md5(password);
                return StringUtil.scramble411(passwdMd5, challenge);
            } catch (NoSuchAlgorithmException e) {
            }

        }
        return null;
    }
}
