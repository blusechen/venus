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
package com.meidusa.venus.benchmark.venus.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.ServicePacketBuffer;
import com.meidusa.venus.io.utils.GZipUtil;

public class JsonVenusRequestPacket extends AbstractServiceRequestPacket {

    private static final long serialVersionUID = 1L;

    public JsonVenusRequestPacket() {
        type = PACKET_TYPE_SERVICE_REQUEST;
    }

    public String params;

    protected void readBody(ServicePacketBuffer buffer) {
        super.readBody(buffer);
        if (buffer.hasRemaining()) {

            byte f = (byte) (this.flags & CAPABILITY_GZIP);
            if (f == CAPABILITY_GZIP) {
                byte[] bts = buffer.readLengthCodedBytes();
                if (bts != null & bts.length > 0) {
                    bts = GZipUtil.decompress(bts);
                    params = new String(bts, PACKET_CHARSET);

                }
            } else {
                params = buffer.readLengthCodedString(PACKET_CHARSET);
            }
        }
    }

    protected void writeBody(ServicePacketBuffer buffer) throws UnsupportedEncodingException {
        super.writeBody(buffer);
        if (params != null) {
            byte[] bts = params.getBytes(PACKET_CHARSET);
            if (bts != null) {
                if (PacketConstant.AUTO_COMPRESS_SIZE > 0 && bts.length > PacketConstant.AUTO_COMPRESS_SIZE) {
                    buffer.writeLengthCodedBytes(GZipUtil.compress(bts));
                    this.flags = (byte) (this.flags | CAPABILITY_GZIP);
                } else {
                    buffer.writeLengthCodedBytes(bts);
                }
            } else {
                buffer.writeInt(0);
            }
        } else {
            buffer.writeInt(0);
        }
    }

}
