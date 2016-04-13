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

package com.meidusa.venus.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.util.Log4jConfigurer;

import com.meidusa.toolkit.common.bean.config.ConfigUtil;
import com.meidusa.toolkit.common.util.collection.Collections;
import com.meidusa.toolkit.net.AuthingableBackendConnection;
import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.ConnectionConnector;
import com.meidusa.toolkit.net.ConnectionManager;
import com.meidusa.toolkit.net.factory.BackendConnectionFactory;
import com.meidusa.toolkit.util.TimeUtil;
import com.meidusa.venus.benchmark.util.CmdLineParser;
import com.meidusa.venus.benchmark.util.CmdLineParser.BooleanOption;
import com.meidusa.venus.benchmark.util.CmdLineParser.IntegerOption;
import com.meidusa.venus.benchmark.util.CmdLineParser.LongOption;
import com.meidusa.venus.benchmark.util.CmdLineParser.StringOption;
import com.meidusa.venus.benchmark.util.ObjectMapLoader;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AbstractBenchmark {
    private static AbstractBenchmark benckmark;

    protected static void setBenchmark(AbstractBenchmark benckmark) {
        AbstractBenchmark.benckmark = benckmark;
    }

    private static Properties properties = new Properties();
    protected static CmdLineParser parser = new CmdLineParser(System.getProperty("application", "benchmark"));

    protected static CmdLineParser.Option debugOption = parser.addOption(new BooleanOption('d', "debug", false, false, true,
            "show the interaction with the server-side information"));
    protected static CmdLineParser.Option showErrorOption = parser.addOption(new BooleanOption('e', "error", false, false, true, "show error information"));

    protected static CmdLineParser.Option portOption = parser.addOption(new IntegerOption('P', "port", true, true, "server port"));
    protected static CmdLineParser.Option hostOption = parser.addOption(new StringOption('h', "host", true, true, "127.0.0.1", "server host")); // NOPMD
                                                                                                                                                // by
                                                                                                                                                // structchen
                                                                                                                                                // on
                                                                                                                                                // 13-10-18
                                                                                                                                                // 上午11:42
    protected static CmdLineParser.Option connOption = parser.addOption(new IntegerOption('c', "conn", true, true, "The number of concurrent connections"));
    protected static CmdLineParser.Option totalOption = parser.addOption(new LongOption('n', "total", true, true, "total requests"));
    protected static CmdLineParser.Option timeoutOption = parser.addOption(new IntegerOption('t', "timeout", true, false, -1,
            "query timeout, default value=-1 "));

    protected static CmdLineParser.Option contextOption = parser.addOption(new StringOption('C', "context", true, false, "Context xml File"));
    protected static CmdLineParser.Option requestOption = parser.addOption(new StringOption('f', "file", true, false, "request xml File"));

    protected static CmdLineParser.Option connModelption = parser.addOption(new BooleanOption('m', "model", true, false, false, "only connect model"));

    protected static CmdLineParser.Option bufferOption = parser.addOption(new IntegerOption('b', "buffer", true, false, 64, "socket buffer size"));

    protected static CmdLineParser.Option log4jOption = parser.addOption(new StringOption('l', "log4j", true, false, "warn",
            "log4j level[debug,info,warn,error]"));

    protected static CmdLineParser.Option helpOption = parser.addOption(new BooleanOption('?', "help", false, false, true, "Show this help message"));
    private static Map<String, RandomData> randomMap = new HashMap<String, RandomData>();
    private static Map contextMap = new HashMap() {
        private static final long serialVersionUID = 1L;

        public Object put(Object key, Object value) {
            if (value instanceof RandomData) {
                randomMap.put((String) key, (RandomData) value);
            }
            super.put(key, value);
            return value;
        }
    };

    private List<AbstractBenchmarkClient> benchmarkClientList = Collections.synchronizedList(new ArrayList<AbstractBenchmarkClient>());

    public List<AbstractBenchmarkClient> getBenchmarkClientList() {
        return benchmarkClientList;
    }

    public CmdLineParser getCmdLineParser() {
        return parser;
    }

    public AbstractBenchmark() {
        Random random = new Random();
        contextMap.put("random", random);
        contextMap.put("atomicInteger", new AtomicInteger());
        contextMap.put("atomicLong", new AtomicLong());

        String requestXml = (String) parser.getOptionValue(requestOption);
        if (requestXml != null) {
            File reqestXmlFile = new File(requestXml);
            if (reqestXmlFile.exists() && reqestXmlFile.isFile()) {
                try {
                    properties.loadFromXML(new FileInputStream(reqestXmlFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            } else {
                System.err.println("requestFile not found or is not file :" + reqestXmlFile.getAbsolutePath());
                System.exit(-1);
            }
        }

        String contextFile = (String) parser.getOptionValue(contextOption);

        if (contextFile != null) {
            File contextXmlFile = new File(contextFile);
            if (contextXmlFile.exists() && contextXmlFile.isFile()) {
                try {
                    ObjectMapLoader.load(contextMap, new FileInputStream(contextXmlFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            } else {
                System.err.println("requestFile not found or not file :" + contextXmlFile.getAbsolutePath());
                System.exit(-1);
            }
        }

    }

    public abstract BackendConnectionFactory getConnectionFactory();

    protected ConnectionConnector connector;

    public void setConnector(ConnectionConnector connector) {
        this.connector = connector;
    }

    private static AtomicLong timeOutCount = new AtomicLong(0);

    public Map<String, Object> getNextRequestContextMap() {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.putAll(contextMap);
        for (Map.Entry<String, RandomData> entry : randomMap.entrySet()) {
            Object obj = null;
            do {
                obj = entry.getValue().nextData();
            } while (obj == null);
            temp.put(entry.getKey(), obj);
        }
        return temp;
    }

    public static AbstractBenchmark getInstance() {
        return AbstractBenchmark.benckmark;
    }

    public abstract AbstractBenchmarkClient newBenchmarkClient(BackendConnection conn, BenchmarkContext context);

    public static void main(String[] args) throws Exception {

        String level = (String) parser.getOptionValue(log4jOption);
        if (level != null) {
            System.setProperty("benchmark.level", level);
        } else {
            System.setProperty("benchmark.level", "warn");
        }

        final Boolean value = (Boolean) parser.getOptionValue(debugOption, false);
        final Boolean showError = (Boolean) parser.getOptionValue(showErrorOption, false);

        __INIT_LOG: {
            String logbackConf = System.getProperty("logback.configurationFile", "${project.home}/conf/logback.xml");
            logbackConf = ConfigUtil.filter(logbackConf, System.getProperties());

            File logbackFile = new File(logbackConf);
            if (logbackFile.exists()) {
                System.out.println("Log system load configuration form " + logbackConf);
                System.setProperty("logback.configurationFile", logbackConf);
                break __INIT_LOG;
            }

            String log4jConf = System.getProperty("log4j.configuration", "${project.home}/conf/log4j.xml");
            log4jConf = ConfigUtil.filter(log4jConf, System.getProperties());
            File log4jFile = new File(log4jConf);

            if (!log4jFile.exists()) {
                log4jConf = System.getProperty("log4j.configuration", "${project.home}/conf/log4j.properties");
                log4jConf = ConfigUtil.filter(log4jConf, System.getProperties());
                log4jFile = new File(log4jConf);
            }

            if (log4jFile.exists()) {
                try {
                    System.setProperty("log4j.configuration", log4jConf);
                    System.out.println("Log system load configuration form " + log4jConf);

                    Log4jConfigurer.initLogging(log4jConf, 30 * 1000);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                    break __INIT_LOG;
                } catch (FileNotFoundException e1) {
                }
            }
        }

        final int conn = (Integer) parser.getOptionValue(connOption);
        final long total = (Long) parser.getOptionValue(totalOption);
        String ip = parser.getOptionValue(hostOption).toString();

        final BenchmarkContext context = new BenchmarkContext((int) total);
        final CountDownLatch reportLatcher = new CountDownLatch(1);
        final AtomicLong errorNum = new AtomicLong(0);
        int port = (Integer) parser.getOptionValue(portOption);

        final ConnectionConnector connector = new ConnectionConnector("Connection Connector");

        final ConnectionManager[] managers = new ConnectionManager[Runtime.getRuntime().availableProcessors()];

        for (int i = 0; i < managers.length; i++) {
            managers[i] = new ConnectionManager("Manager-" + i, -1);
            managers[i].start();
        }

        connector.setProcessors(managers);
        connector.start();

        final Integer timeout = (Integer) parser.getOptionValue(timeoutOption, -1);

        Thread.sleep(100L);
        System.out.println("Connection manager started....");
        final CountDownLatch createLatch = new CountDownLatch(conn);
        final CountDownLatch startCreateLatch = new CountDownLatch(1);
        Boolean model = (Boolean) parser.getOptionValue(connModelption);
        if (model == null || !model.booleanValue()) {
            new Thread() {
                long lastCount = context.getResponseLatcher().getCount();
                long lastTime = TimeUtil.currentTimeMillis();
                {
                    this.setDaemon(true);
                }

                public void run() {
                    try {
                        startCreateLatch.await();
                    } catch (InterruptedException e1) {
                    }
                    while (context.getResponseLatcher().getCount() > 0) {
                        long current = context.getResponseLatcher().getCount();
                        long currentTime = TimeUtil.currentTimeMillis();
                        long tps = 0;
                        if (currentTime > lastTime) {
                            tps = (lastCount - current) * 1000 / (currentTime - lastTime);
                        } else {
                            tps = (lastCount - current);
                        }
                        lastCount = current;
                        lastTime = currentTime;
                        System.out.println(new Date() + "    request=" + (total - context.getRequestLatcher().getCount()) + ",  compeleted="
                                + (total - lastCount) + ", errorResult=" + context.errorNum.get() + ", TPS=" + tps + ", timeoutNum=" + timeOutCount.get()
                                + " ,conns=" + connector.getBackends());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }

                        if (context.getRequestLatcher().getCount() == 0) {
                            if (context.getResponseLatcher().getCount() - errorNum.get() <= 0) {
                                break;
                            }
                        }
                    }

                    for (long i = 0; i < errorNum.get(); i++) {
                        context.getResponseLatcher().countDown();
                    }

                    System.out.println(new Date() + "    request=" + (total - context.getRequestLatcher().getCount()) + ",  compeleted="
                            + (total - context.getResponseLatcher().getCount()) + ", errorResult=" + context.errorNum.get() + ", timeoutNum="
                            + timeOutCount.get() + " ,conns=" + connector.getBackends());
                    reportLatcher.countDown();
                }

            }.start();

        } else {
            new Thread() {
                {
                    this.setDaemon(true);
                }

                public void run() {

                    while (createLatch.getCount() > 0) {
                        System.out.println(new Date() + "     conns=" + connector.getBackends());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }

                    System.out.println(new Date() + "     compeleted=" + (conn));
                }

            }.start();
        }

        System.out.println("\r\nconnect to ip=" + ip + ",port=" + port + ",connection size=" + conn + ",total request=" + total);
        final AbstractBenchmark benckmark = AbstractBenchmark.getInstance();
        benckmark.setConnector(connector);

        final BackendConnectionFactory factory = benckmark.getConnectionFactory();

        factory.setHost(ip);
        factory.setPort(port);
        factory.setConnector(connector);

        final long createConnectionStartTime = System.nanoTime();

        final ExecutorService executor = Executors.newFixedThreadPool(Integer.getInteger("createConnectionThreadSize", 4));
        final InetSocketAddress address = new InetSocketAddress(ip, port);
        System.out.println("---------------- create connection-----------------");
        startCreateLatch.countDown();
        for (int i = 0; i < conn; i++) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {

                        BackendConnection conn = factory.make();

                        final AbstractBenchmarkClient client = benckmark.newBenchmarkClient(conn, context);
                        client.setBenchmark(benckmark);
                        client.setTimeout(timeout.intValue());
                        client.setDebug(value.booleanValue());
                        client.setShowError(showError.booleanValue());
                        client.putAllRequestProperties(properties);
                        client.init();

                        if (conn instanceof AuthingableBackendConnection) {
                            if (!((AuthingableBackendConnection) conn).isAuthenticated()) {
                                conn.close();
                            } else {
                                benckmark.benchmarkClientList.add(client);
                            }
                        } else {
                            benckmark.benchmarkClientList.add(client);
                        }
                        createLatch.countDown();
                    } catch (Exception e) {
                        System.err.println("connect to " + address + " error:");
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

            });

        }
        createLatch.await();
        final long createConnectionEndTime = System.nanoTime();
        System.out.println("---------------- end (" + TimeUnit.MILLISECONDS.convert(createConnectionEndTime - createConnectionStartTime, TimeUnit.NANOSECONDS)
                + "ms)-----------------");

        if (model != null && model.booleanValue()) {
            // when connect model no request after connections created.
            return;
        }

        for (AbstractBenchmarkClient client : benckmark.benchmarkClientList) {
            if (context.getRequestLatcher().getCount() > 0) {
                context.getRequestLatcher().countDown();
                client.startBenchmark();
            }
        }

        new Thread() {
            {
                this.setDaemon(true);
                this.setName("timeout check thread");
            }

            public void run() {
                while (context.getRequestLatcher().getCount() > 0) {
                    for (AbstractBenchmarkClient client : benckmark.benchmarkClientList) {
                        if (context.getRequestLatcher().getCount() > 0) {
                            if (client.checkTimeOut()) {
                                timeOutCount.incrementAndGet();
                                client.afterTimeout();
                                context.getResponseLatcher().countDown();
                            }
                        }
                    }
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    context.setRunning(false);
                    long endBenchmarkTime = System.nanoTime();
                    if (benckmark.benchmarkClientList.size() == 0) {
                        executor.shutdown();
                        connector.shutdown();
                        return;
                    }
                    long min = benckmark.benchmarkClientList.get(0).min;
                    long max = 0;
                    long minStart = benckmark.benchmarkClientList.get(0).start;
                    long maxend = 0;
                    long average = 0;
                    int totleConnection = 0;
                    for (AbstractBenchmarkClient connection : benckmark.benchmarkClientList) {
                        if (connection.count > 0) {
                            min = Math.min(min, connection.min);
                            max = Math.max(max, connection.max);
                            average += (connection.end - connection.start) / connection.count;
                            minStart = Math.min(minStart, connection.start);
                            maxend = Math.max(maxend, connection.end);
                            totleConnection++;
                        }
                    }
                    long time = TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS);

                    try {
                        reportLatcher.await(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                    }

                    long requested = (total - context.getResponseLatcher().getCount());
                    System.out.println("completed requests total=" + requested + ", cost="
                            + TimeUnit.MILLISECONDS.convert((maxend - minStart), TimeUnit.NANOSECONDS) + "ms , TPS="
                            + (time > 0 ? ((long) requested * 1000) / time : requested) + "/s");

                    double thisTime = (double) (TimeUnit.MICROSECONDS.convert(min, TimeUnit.NANOSECONDS)) / (double) 1000;
                    System.out.println("min=" + thisTime + " ms");
                    thisTime = (double) TimeUnit.MICROSECONDS.convert(max, TimeUnit.NANOSECONDS) / (double) 1000;
                    System.out.println("max=" + thisTime + " ms");
                    thisTime = (double) TimeUnit.MICROSECONDS.convert(average, TimeUnit.NANOSECONDS) / (double) (totleConnection * 1000);
                    DecimalFormat fmt = new DecimalFormat("#.###");
                    System.out.println("average=" + fmt.format(thisTime) + " ms");
                    System.out.println("timeout Num=" + timeOutCount.get());
                    System.out.println("Error result=" + context.errorNum.get());
                    System.out.println("connection Error Num=" + errorNum.get());
                    System.out.println("create Connections time="
                            + TimeUnit.MILLISECONDS.convert(createConnectionEndTime - createConnectionStartTime, TimeUnit.NANOSECONDS) + "ms");
                    long tpsTime = TimeUnit.MILLISECONDS.convert(endBenchmarkTime - createConnectionEndTime, TimeUnit.NANOSECONDS);
                    System.out.println("TPS(after connected)=" + (tpsTime > 0 ? ((long) requested * 1000) / tpsTime : requested) + "/s");
                    executor.shutdown();
                    connector.shutdown();
                }
            });
        } catch (java.lang.IllegalStateException e) {
            context.setRunning(false);
            executor.shutdown();
            connector.shutdown();
            System.exit(-1);
        }
        context.getRequestLatcher().await();
        context.setRunning(false);
        context.getResponseLatcher().await();
        System.exit(0);
    }
}
