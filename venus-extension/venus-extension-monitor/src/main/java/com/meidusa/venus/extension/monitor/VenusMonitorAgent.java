package com.meidusa.venus.extension.monitor;

import java.util.concurrent.ScheduledExecutorService;

import com.meidusa.toolkit.common.runtime.GlobalScheduler;
import com.meidusa.toolkit.net.util.LoopingThread;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.client.simple.SimpleServiceFactory;

public class VenusMonitorAgent extends LoopingThread{
	private String host;
	private int port;
	private int interval;
	private String apiName;
	//定时任务
    private ScheduledExecutorService executorService;
    
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	
	
	protected void willStart() {
		 //初始化定时任务
	    if (executorService == null) {
	        executorService = GlobalScheduler.getInstance();
	    }
    }
	
    
	public void iterate(){
		
	}

	
	public void sendMonitorEntity(){
		SimpleServiceFactory factory = new SimpleServiceFactory(host,port);
		
		
	}
    /**
     * 功能描述:下一次调用时间 <br>
     * 〈功能详细描述〉
     *
     * @param config
     * @return
     * @see [相关类/方法](可选)
     * @since [产品/模块版本](可选)
     */
    private long nextDelayTime() {
        long now = TimeUtil.currentTimeMillis() / 1000;
        long next = (now / interval + 1) * interval;
        long delay = next - now;
        if (delay <= 0) {
            return interval - delay;
        } else {
            return delay;
        }
    }
}
