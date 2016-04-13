package com.meidusa.venus.io;

import com.meidusa.venus.io.packet.AbstractServicePacket;
import com.meidusa.venus.io.packet.AbstractServiceRequestPacket;

public interface ServiceFilter {
	
		/**
		 * 
		 * @param request <code>SerializeServiceRequestPacket</code>
		 */
		public abstract void before(AbstractServicePacket request);
		
		/**
		 * 
		 * @param request <code>ServiceResponsePacket</code> or <code>ErrorPacket</code> or <code>OKPacket</code>
		 */
		public abstract void after(AbstractServicePacket request);
}
