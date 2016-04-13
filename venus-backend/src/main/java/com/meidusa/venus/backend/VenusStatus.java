package com.meidusa.venus.backend;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meidusa.venus.io.packet.PacketConstant;

public class VenusStatus {
	private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);
	private final static String PS_OLD_GEN = "PS Old Gen";
	private static long OOMCount = 0;
	private static VenusStatus instance = new VenusStatus();
	public static VenusStatus getInstance(){
		return instance;
	}
	private static ShutdownListener listener = new ShutdownListener();
    static {
        Runtime.getRuntime().addShutdownHook(listener);
        new Thread(){
        	{
        		this.setDaemon(true);
        		this.setName("VM-OOM-Checker");
        	}
        	public void run(){
        		while(true){
        			List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
        			for(MemoryPoolMXBean bean : list){
        				if(StringUtils.equalsIgnoreCase(bean.getName(), PS_OLD_GEN)){
        					MemoryUsage usage = bean.getUsage();
        					
        					double freeRate =((double)(usage.getMax() - usage.getUsed())/(double)usage.getMax());
        					if( freeRate <= 1.0E-7){
        						OOMCount ++;
        						if(OOMCount>10){
        							if(VenusStatus.getInstance().getStatus() == PacketConstant.VENUS_STATUS_RUNNING){
        								VenusStatus.getInstance().setStatus(PacketConstant.VENUS_STATUS_OUT_OF_MEMORY );
        							}
        							logger.warn(bean.getName()+":"+usage);
        						}
        	        		}else{
        	        			OOMCount = 0;
        	        			VenusStatus.getInstance().setStatus((byte)((~PacketConstant.VENUS_STATUS_OUT_OF_MEMORY & VenusStatus.getInstance().getStatus()) & 0xff) );
        	        		}
        				}
        			}
	        		try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
        		}
        	}
        }.start();
    }

	private VenusStatus(){}
	
	private byte status = PacketConstant.VENUS_STATUS_RUNNING;;
	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	
	/*public static double getProcessCpuLoad() throws Exception {

		OperatingSystemMXBean  mbs    = ManagementFactory.getOperatingSystemMXBean();
	    ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
	    
	    
	    AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

	    if (list.isEmpty())     return Double.NaN;

	    Attribute att = (Attribute)list.get(0);
	    Double value  = (Double)att.getValue();

	    if (value == -1.0)      return Double.NaN;

	    return ((int)(value * 1000) / 10.0);        // returns a percentage value with 1 decimal point precision
	}*/
	
	public static void main(String[] args) throws Exception {
		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		
		List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
		for(MemoryPoolMXBean bean : list){
			System.out.println("------"+bean.getName()+"--------");
			MemoryUsage usage = bean.getUsage();
			System.out.println(bean);
			
			System.out.println(usage);
			
			/*if(StringUtils.equalsIgnoreCase(bean.getName(), PS_OLD_GEN)){
			}*/
		}
		
		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();
		for(GarbageCollectorMXBean mxBean : gcList){
			mxBean.getMemoryPoolNames();
			System.out.println("name="+mxBean.getName()+",times="+mxBean.getCollectionCount()+",time="+mxBean.getCollectionTime());
		}
		
		com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		// What % CPU load this current JVM is taking, from 0.0-1.0
		System.out.println(osBean.getSystemLoadAverage());

		// What % load the overall system is at, from 0.0-1.0
		//System.out.println(osBean.getSystemCpuLoad());
		//System.out.println(getProcessCpuLoad());
	}
}
