/*
 * Copyright 2008-2108 amoeba.meidusa.com 
 * 
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU AFFERO GENERAL PUBLIC LICENSE as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU AFFERO GENERAL PUBLIC LICENSE for more details. 
 * 	You should have received a copy of the GNU AFFERO GENERAL PUBLIC LICENSE along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.meidusa.venus.benchmark.venus;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.bean.config.ParameterMapping;
import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.packet.AbstractPacket;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.benchmark.AbstractBenchmarkClient;
import com.meidusa.venus.benchmark.BenchmarkContext;
import com.meidusa.venus.benchmark.net.VenusBenchmarkConnection;
import com.meidusa.venus.benchmark.venus.packet.BsonVenusRequestPacket;
import com.meidusa.venus.benchmark.venus.packet.BsonVenusResponsePacket;
import com.meidusa.venus.benchmark.venus.packet.JsonVenusRequestPacket;
import com.meidusa.venus.benchmark.venus.packet.JsonVenusResponsePacket;
import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.ErrorPacket;
import com.meidusa.venus.io.packet.HandshakePacket;
import com.meidusa.venus.io.packet.OKPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.PingPacket;
import com.meidusa.venus.io.packet.PongPacket;

/**
 * 
 * @author Struct
 * 
 */
public class VenusBenchmarkClient extends AbstractBenchmarkClient<AbstractServicePacket, BenchmarkContext> {
    private static Logger logger = LoggerFactory.getLogger(VenusBenchmarkClient.class);
    private short serializeType;
    private AtomicLong sequence = new AtomicLong(0);
    private long lastRequestSequence;

    public VenusBenchmarkClient(BackendConnection connection, BenchmarkContext context) {
        super(connection, context);
        VenusBenchmarkConnection conn = (VenusBenchmarkConnection) connection;
        serializeType = conn.getSerializeType();
    }

    protected boolean responseIsCompleted(byte[] message) {
        long sequence = AbstractServicePacket.getPacketSequence(message);

        if (sequence == lastRequestSequence) {
            return true;
        } else {
            int type = AbstractServicePacket.getType(message);
            if (type != PacketConstant.PACKET_TYPE_PING && type != PacketConstant.PACKET_TYPE_PONG) {
                System.out.println("  !!!receive sequence=" + sequence + ",lastRequestSequence=" + lastRequestSequence);
            }
        }
        return false;
    }

    protected void afterMessageRecieved(byte[] message) {
        super.afterMessageRecieved(message);
        int type = AbstractServicePacket.getType(message);
        if (type == PacketConstant.PACKET_TYPE_ERROR) {
            this.context.getErrorNum().incrementAndGet();
            if (this.isShowError()) {
                ErrorPacket packet = new ErrorPacket();
                packet.init(message);

                System.out.println("  receive error:" + packet);
            }
        }
    }

    public AbstractServicePacket decodeRecievedPacket(byte[] message) {
        int type = AbstractServicePacket.getType(message);
        AbstractServicePacket packet = null;
        switch (type) {
            case PacketConstant.PACKET_TYPE_PING:
                packet = new PingPacket();
                packet.init(message);
                break;
            case PacketConstant.PACKET_TYPE_PONG:
                packet = new PongPacket();
                packet.init(message);
                break;
            case PacketConstant.PACKET_TYPE_SERVICE_REQUEST:
                if (serializeType == PacketConstant.CONTENT_TYPE_JSON) {
                    packet = new JsonVenusRequestPacket();
                } else {
                    packet = new BsonVenusRequestPacket();
                }
                packet.init(message);
                break;
            case PacketConstant.PACKET_TYPE_SERVICE_RESPONSE:
                if (serializeType == PacketConstant.CONTENT_TYPE_JSON) {
                    packet = new JsonVenusResponsePacket();
                } else {
                    packet = new BsonVenusResponsePacket();
                }
                packet.init(message);
                break;
            case PacketConstant.PACKET_TYPE_ERROR:
                packet = new ErrorPacket();
                packet.init(message);
                if (logger.isWarnEnabled()) {
                    logger.warn("return error packet=" + packet);
                }
                break;
            case PacketConstant.PACKET_TYPE_HANDSHAKE:
                packet = new HandshakePacket();
                packet.init(message);
                break;
            case PacketConstant.PACKET_TYPE_OK:
                packet = new OKPacket();
                packet.init(message);
                break;
            default:
                logger.error("error type=" + type + "\r\n" + StringUtil.dumpAsHex(message, message.length));
        }
        return packet;
    }

    final Map<String, String> parameterMap = new HashMap<String, String>();
    final Map<String, Object> beanParameterMap = new HashMap<String, Object>();

    public AbstractServicePacket createRequestPacket() {
        AbstractServicePacket packet = null;
        try {
            if (serializeType == PacketConstant.CONTENT_TYPE_BSON) {
                packet = new BsonVenusRequestPacket();
            } else {
                packet = new JsonVenusRequestPacket();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Map<String, Object> map = this.getNextRequestContextMap();
        ParameterMapping.mappingObjectField(packet, beanParameterMap, map, this, AbstractPacket.class);

        Map<String, Object> _parameterMap_ = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            String value = ConfigUtil.filterWtihOGNL(entry.getValue(), map, this);
            _parameterMap_.put(entry.getKey(), value);
        }
        AbstractServiceRequestPacket request = (AbstractServiceRequestPacket) packet;
        request.parameterMap = _parameterMap_;
        
        lastRequestSequence = sequence.incrementAndGet();
        packet.clientRequestId = lastRequestSequence;
        return packet;
    }

    public void init() {
        super.init();
        Properties properties = this.getRequestProperties();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith("parameterMap.")) {
                parameterMap.put(entry.getKey().toString().substring("parameterMap.".length()), entry.getValue().toString());
            } else {
                beanParameterMap.put(entry.getKey().toString(), entry.getValue());
            }
        }
    }

}
