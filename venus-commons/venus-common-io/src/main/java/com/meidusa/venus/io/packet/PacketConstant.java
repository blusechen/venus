/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General License for more details. 
 * 	You should have received a copy of the GNU General License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.venus.io.packet;

import java.nio.charset.Charset;
import java.util.Random;

public interface PacketConstant {

    int VENUS_CLIENT_ID = Integer.getInteger("venus.client.id", new Random().nextInt(Integer.MAX_VALUE));

    int VENUS_DEFAULT_PORT = 16800;

    byte[] EMPTY_TRACE_ID = new byte[16];

    String VENUS_CLIENT = "VENUS-JAVA-CLIENT";
    /**
     * 协议版本号
     */
    short PROTOCOL_VERSION = 2;

    int PACKET_HEADER_SIZE = 4;
    /**
     * 协议头长度
     */
    int SERVICE_HEADER_SIZE = 24;

    /**
     * Type在数据包中的起始位置
     */
    int TYPE_POSITION = 6;

    /**
     * 客户端ID的位置
     */
    int CLIENTID_POSITION = 12;

    /**
     * 请求系列号位置
     */
    int SEQUENCE_POSITION = 16;

    byte[] SERVICE_HEADER_PAD = new byte[SERVICE_HEADER_SIZE];

    int PACKET_TYPE_PING = 0x01000001;
    int PACKET_TYPE_PONG = 0x01000002;

    int PACKET_TYPE_SERVICE_REQUEST = 0x02000001;
    int PACKET_TYPE_SERVICE_RESPONSE = 0x02000002;

    int PACKET_TYPE_HANDSHAKE = 0x03000001;

    int PACKET_TYPE_AUTHEN = 0x03100000;

    int PACKET_TYPE_NOTIFY_PUBLISH = 0x04000001;

    int PACKET_TYPE_NOTIFY_SUBSCRIBE = 0x04000002;

    int PACKET_TYPE_VENUS_STATUS_REQUEST = 0x05000001;

    int PACKET_TYPE_VENUS_STATUS_RESPONSE = 0x05000002;
    
    int PACKET_TYPE_VENUS_SET_STATUS = 0x05000011;

    byte VENUS_STATUS_RUNNING = 0x01;
    byte VENUS_STATUS_SHUTDOWN = 0x02;
    byte VENUS_STATUS_OUT_OF_MEMORY = 0x04;

    /**
     * 标识虚拟认证
     */
    byte AUTHEN_TYPE_DUMMY = 0x01 << 0;

    /**
     * 标识用户名、密码方式认证
     */
    byte AUTHEN_TYPE_PASSWORD = 0x01 << 1;

    /**
     * 标识PKI方式认证（2.0.0-BETA版本还未实现）
     */
    byte AUTHEN_TYPE_PKI = 0x01 << 2;

    int PACKET_TYPE_OK = 0x00000001;
    int PACKET_TYPE_ERROR = 0xFFFFFFFF;

    Charset PACKET_CHARSET = Charset.forName("UTF8");

    int PACKET_TYPE_ROUTER = 0x08000001;

    byte CONTENT_TYPE_JSON = 0x00;
    byte CONTENT_TYPE_BSON = 0x01;
    byte CONTENT_TYPE_OBJECT = 0x02;

    int AUTH_CURRENT_SUPPORT = AUTHEN_TYPE_DUMMY | AUTHEN_TYPE_PASSWORD;

    int AUTO_COMPRESS_SIZE = Integer.getInteger("venus.compress.auto", -1);

    /**
     * 接受GZIP压缩的能力
     */
    byte CAPABILITY_GZIP = 1 << 4;

    /**
     * 客户端能力，消息监听
     */
    int CAPABILITY_LISTENER = 1 << 7;

    int CAPABILITIES = CAPABILITY_GZIP;
}
