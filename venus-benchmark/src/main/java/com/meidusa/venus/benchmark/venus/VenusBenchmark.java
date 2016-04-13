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

import org.apache.commons.lang.StringUtils;

import com.meidusa.toolkit.net.BackendConnection;
import com.meidusa.toolkit.net.factory.BackendConnectionFactory;
import com.meidusa.venus.benchmark.AbstractBenchmark;
import com.meidusa.venus.benchmark.AbstractBenchmarkClient;
import com.meidusa.venus.benchmark.BenchmarkContext;
import com.meidusa.venus.benchmark.net.VenusBenchmarkConnectionFactory;
import com.meidusa.venus.benchmark.util.CmdLineParser;
import com.meidusa.venus.benchmark.util.CmdLineParser.IntegerOption;
import com.meidusa.venus.benchmark.util.CmdLineParser.StringOption;
import com.meidusa.venus.io.authenticate.Authenticator;
import com.meidusa.venus.io.authenticate.DummyAuthenticator;
import com.meidusa.venus.io.authenticate.UserPasswordAuthenticator;
import com.meidusa.venus.io.packet.PacketConstant;

public class VenusBenchmark extends AbstractBenchmark {
    protected static CmdLineParser.Option serialOption = parser.addOption(new IntegerOption('s', "serialize", true, false,
            "serialize Type(json=0,bson=1),default:0"));
    protected static CmdLineParser.Option clientIdOption = parser.addOption(new IntegerOption('i', "clientID", true, false, "client id"));
    protected static CmdLineParser.Option userOption = parser.addOption(new StringOption('u', "user", true, false, "user name"));
    protected static CmdLineParser.Option passwordOption = parser.addOption(new StringOption('p', "password", true, false, "password"));

    public static void main(String[] args) throws Exception {
        try {
            parser.parse(args);
            Boolean value = (Boolean) parser.getOptionValue(helpOption, false);
            if (value != null && value.booleanValue()) {
                parser.printUsage();
                System.exit(2);
            }
            parser.checkRequired();
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            parser.printUsage();
            System.exit(2);
        }

        AbstractBenchmark.setBenchmark(new VenusBenchmark());
        AbstractBenchmark.main(args);
    }

    private VenusBenchmarkConnectionFactory factory;

    @Override
    public BackendConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = new VenusBenchmarkConnectionFactory();
            String password = (String) parser.getOptionValue(passwordOption);
            String username = (String) parser.getOptionValue(userOption);
            if (!StringUtils.isEmpty(password)) {
                UserPasswordAuthenticator authenticator = new UserPasswordAuthenticator();
                authenticator.setPassword(password);
                authenticator.setUsername(username);
                factory.setAuthenticator(authenticator);
            }

            Integer clientId = (Integer) parser.getOptionValue(clientIdOption);
            if (clientId != null) {
                Authenticator authenticator = factory.getAuthenticator();
                if (authenticator instanceof DummyAuthenticator) {
                    DummyAuthenticator dor = (DummyAuthenticator) authenticator;
                    dor.setClientId(clientId);
                }
            }

            Integer serialValue = (Integer) parser.getOptionValue(serialOption);
            if (serialValue != null) {
                byte value = serialValue.byteValue();
                factory.getAuthenticator().setSerializeType(value);
            } else {
                factory.getAuthenticator().setSerializeType(PacketConstant.CONTENT_TYPE_JSON);
            }
        }
        return factory;
    }

    public AbstractBenchmarkClient<?, BenchmarkContext> newBenchmarkClient(BackendConnection conn, BenchmarkContext context) {
        return new VenusBenchmarkClient(conn, context);
    }

}
