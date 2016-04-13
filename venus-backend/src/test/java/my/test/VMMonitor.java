package my.test;

import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * Class description.
 * 
 * @author Mavlarn
 */
public class VMMonitor {

  public static Set<String> youngGCNames = new HashSet<String>();
	public static Set<String> oldGCNames = new HashSet<String>();

	static {
		// Oracle (Sun) HotSpot
		youngGCNames.add("Copy"); // -XX:+UseSerialGC
		youngGCNames.add("ParNew"); // -XX:+UseParNewGC
		youngGCNames.add("PS Scavenge"); // -XX:+UseParallelGC

		// Oracle (BEA) JRockit
		youngGCNames.add("Garbage collection optimized for short pausetimes Young Collector"); // -XgcPrio:pausetime
		youngGCNames.add("Garbage collection optimized for throughput Young Collector"); // -XgcPrio:throughput
		youngGCNames.add("Garbage collection optimized for deterministic pausetimes Young Collector"); // -XgcPrio:deterministic

		// Oracle (Sun) HotSpot
		oldGCNames.add("MarkSweepCompact"); // -XX:+UseSerialGC
		oldGCNames.add("PS MarkSweep"); // -XX:+UseParallelGC and
										// (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
		oldGCNames.add("ConcurrentMarkSweep"); // -XX:+UseConcMarkSweepGC

		// Oracle (BEA) JRockit
		oldGCNames.add("Garbage collection optimized for short pausetimes Old Collector"); // -XgcPrio:pausetime
		oldGCNames.add("Garbage collection optimized for throughput Old Collector"); // -XgcPrio:throughput
		oldGCNames.add("Garbage collection optimized for deterministic pausetimes Old Collector"); // -XgcPrio:deterministic

	}

	static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	public static void main(String[] args) throws InterruptedException {
		if (args == null || args.length == 0) {
			System.out.println("Please specify the target PID to attach.");
		}

		/*// attach to the target application
		VirtualMachine vm;
		try {
			vm = VirtualMachine.attach(args[0]);
		} catch (AttachNotSupportedException e) {
			System.err.println("Target application doesn't support attach API.");
			return;
		} catch (IOException e) {
			System.err.println("Error during attaching to target application.");
			return;
		}

		try {
			// get the connector address
			String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			MBeanServerConnection serverConn;
			// no connector address, so we start the JMX agent
			if (connectorAddress == null) {
				String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib"
						+ File.separator + "management-agent.jar";
				vm.loadAgent(agent);

				// agent is started, get the connector address
				connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			}

			// establish connection to connector server
			JMXServiceURL url = new JMXServiceURL(connectorAddress);
			JMXConnector connector = JMXConnectorFactory.connect(url);
			serverConn = connector.getMBeanServerConnection();
			ObjectName objName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);

			// Get standard attribute "VmVendor"
			String vendor = (String) serverConn.getAttribute(objName, "VmVendor");
			System.out.println("vendor:" + vendor);

			VMMonitor test = new VMMonitor();
			String[] gcNames = getGCName();
			while(true) {
				for (String currName : gcNames) {
					objName = new ObjectName("java.lang:type=GarbageCollector,name=" + currName);
					Long collectionCount = (Long) serverConn.getAttribute(objName, "CollectionCount");
					Long collectionTime = (Long) serverConn.getAttribute(objName, "CollectionTime");
					
					StringBuilder sb = new StringBuilder("[");
					sb.append(getGCType(currName)).append("\t: ");
					sb.append("Count=" + collectionCount);
					sb.append(" \tGCTime=" + collectionTime);
					sb.append("]");
					System.out.println(sb.toString());
					
					System.out.println(test.monitorMemory());
					System.out.println(test.monitorMemoryPool());
				}
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	public static String formatBytes(long byteSize) {
		if (byteSize > 1024) {
			long kbsize = byteSize / 1024;
			return kbsize + " K";
		} else {
			return "" + byteSize;
		}
	}

	public static String getGCType(String name) {
		if (youngGCNames.contains(name)) {
			return "Minor GC";
		} else if (oldGCNames.contains(name)) {
			return "Full GC";
		} else {
			return name;
		}
	}

	public static String[] getGCName() {
		
		List<GarbageCollectorMXBean> gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();
		String[] rtnName = new String[gcmbeans.size()];
		int index = 0;
		for (GarbageCollectorMXBean gc : gcmbeans) {
			rtnName[index] = gc.getName();
			index++;
		}
		return rtnName;
	}

	//get GC information of current VM
	public String monitorGC() {
		StringBuilder sb = new StringBuilder("GC:");
		List<GarbageCollectorMXBean> gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();
		for (GarbageCollectorMXBean gc : gcmbeans) {
			sb.append("[" + getGCType(gc.getName()) + ": ");
			sb.append("Count=" + gc.getCollectionCount());
			sb.append(" GCTime=" + gc.getCollectionTime());
			sb.append("]");
		}
		return sb.toString();
	}

	public String monitorMemory() {
		StringBuilder sb = new StringBuilder("Memory:");
		MemoryMXBean mmbean = ManagementFactory.getMemoryMXBean();
		MemoryUsage hmu = mmbean.getHeapMemoryUsage();
		sb.append("[HeapMemoryUsage:");
		sb.append(" Used=" + formatBytes(hmu.getUsed() * 8));
		sb.append(" Committed=" + formatBytes(hmu.getCommitted() * 8));
		sb.append("]");

		MemoryUsage nhmu = mmbean.getNonHeapMemoryUsage();
		sb.append("[NonHeapMemoryUsage:");
		sb.append(" Used=" + formatBytes(nhmu.getUsed() * 8));
		sb.append(" Committed=" + formatBytes(nhmu.getCommitted() * 8));
		sb.append("]");
		return sb.toString();
	}

	public String monitorMemoryPool() {
		StringBuilder sb = new StringBuilder("MemoryPool:");
		List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean p : pools) {
			sb.append("[" + p.getName() + ":");
			MemoryUsage u = p.getUsage();
			sb.append(" Used=" + formatBytes(u.getUsed() * 8));
			sb.append(" Committed=" + formatBytes(u.getCommitted() * 8));
			sb.append("]");
		}
		return sb.toString();
	}
}