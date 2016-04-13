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

package com.meidusa.venus.io.network;

import java.io.IOException;
import java.net.Socket;

import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.common.poolable.PoolableObject;
import com.meidusa.venus.io.packet.PacketConstant;
import com.meidusa.venus.io.packet.VenusStatusRequestPacket;
import com.meidusa.venus.io.packet.VenusStatusResponsePacket;

public class VenusBIOConnection extends AbstractBIOConnection implements PoolableObject {
    private static VenusStatusRequestPacket STATUS = new VenusStatusRequestPacket();
    private ObjectPool objectPool = null;
    private boolean active = false;
    public long createTimestamp;

    public VenusBIOConnection(Socket socket, long createStamp) {
        super(socket, createStamp);
        this.createTimestamp = createStamp;
    }

    public ObjectPool getObjectPool() {
        return objectPool;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRemovedFromPool() {
        return this.objectPool == null;
    }

    public void setActive(boolean isactive) {
        this.active = isactive;
    }

    public void setObjectPool(ObjectPool pool) {
        this.objectPool = pool;
    }

    @Override
    public boolean checkValid() {
        try {
            this.write(STATUS.toByteArray());
            VenusStatusResponsePacket packet = new VenusStatusResponsePacket();
            packet.init(this.read());
            if (packet.status == PacketConstant.VENUS_STATUS_SHUTDOWN || packet.status == PacketConstant.VENUS_STATUS_OUT_OF_MEMORY) {
                ObjectPool pool = this.getObjectPool();
                if (pool != null) {
                    pool.setValid(false);
                }
                return false;
            }
            return true;
        } catch (IOException e) {
            try {
                this.close();
            } catch (Exception e1) {
            }
        }
        return false;
    }

}
