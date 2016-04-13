package com.meidusa.venus.io.pool;

import com.meidusa.toolkit.common.poolable.MultipleLoadBalanceObjectPool;
import com.meidusa.toolkit.common.poolable.ObjectPool;
import com.meidusa.toolkit.common.poolable.PoolableObjectPool;
import com.meidusa.toolkit.net.BackendConnectionPool;
import com.meidusa.toolkit.util.StringUtil;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.network.VenusBIOConnection;
import com.meidusa.venus.io.network.VenusBIOConnectionFactory;

public class VenusBIOPoolUtil {
	public static ObjectPool createObjectPool(String ipAddress,Authenticator authenticator){
        if (!StringUtil.isEmpty(ipAddress)) {
            String ipList[] = StringUtil.split(ipAddress, ", ");
            PoolableObjectPool bioPools[] = new PoolableObjectPool[ipList.length];
            
            for (int i = 0; i < ipList.length; i++) {
                VenusBIOConnectionFactory bioFactory = new VenusBIOConnectionFactory();
                if (authenticator != null) {
                    bioFactory.setAuthenticator(authenticator);
                }

                bioPools[i] = new PoolableObjectPool();
               
            	bioPools[i].setTestOnBorrow(true);
                bioPools[i].setTestWhileIdle(true);

                bioFactory.setNeedPing(true);
                
                String temp[] = StringUtil.split(ipList[i], ":");
                if (temp.length > 1) {
                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(Integer.valueOf(temp[1]));
                } else {
                    bioFactory.setHost(temp[0]);
                    bioFactory.setPort(16800);
                }
               
                bioPools[i].setName("B-" + bioFactory.getHost()+":"+bioFactory.getPort());
                bioPools[i].setFactory(bioFactory);
                bioPools[i].init();
            }

            if (ipList.length > 1) {
                MultipleLoadBalanceObjectPool bioPool = new MultipleLoadBalanceObjectPool(MultipleLoadBalanceObjectPool.LOADBALANCING_ROUNDROBIN, bioPools);

                bioPool.setName("B-V-" + ipAddress);

                bioPool.init();
                
                return bioPool;
                
            } else {
            	return bioPools[0];
            }
        } else {
            throw new IllegalArgumentException("ipaddress cannot be null");
        }
	}
	
	public static void main(String[] args) {
		ObjectPool pool = VenusBIOPoolUtil.createObjectPool("127.0.0.1", null);
		VenusBIOConnection conn = null;
		try {
			conn = (VenusBIOConnection)pool.borrowObject();
			
			//conn.write(bts);
		} catch (Exception e) {
			
			e.printStackTrace();
		}finally{
			if(conn != null){
				try {
					pool.returnObject(conn);
				} catch (Exception e) {
				}
			}
		}
		
	}
}
