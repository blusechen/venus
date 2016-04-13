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

import com.meidusa.venus.benchmark.util.CmdLineParser;
import com.meidusa.venus.benchmark.util.CmdLineParser.BooleanOption;
import com.meidusa.venus.benchmark.util.CmdLineParser.StringOption;

/**
 * This example shows how to dynamically create basic output for a --help option.
 */
public class CmdLineParserTest {
    protected static CmdLineParser parser = new CmdLineParser(System.getProperty("application", "benchmark"));
    protected static CmdLineParser.Option debugOption = parser.addOption(new BooleanOption('d', "debug", false, false, true,
            "show the interaction with the server-side information"));
    protected static CmdLineParser.Option userOption = parser.addOption(new StringOption('u', "user", true, true, "root", "mysql user name"));
    protected static CmdLineParser.Option passwordOption = parser.addOption(new StringOption('P', "password", true, false, null, "mysql password"));
    protected static CmdLineParser.Option sqlOption = parser.addOption(new StringOption('s', "sql", true, false, null, "query sql"));
    protected static CmdLineParser.Option helpOption = parser.addOption(new BooleanOption('?', "help", false, false, true, "Show this help message"));

    public static void main(String[] args) {
        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            parser.printUsage();
            System.exit(2);
        }

        if (Boolean.TRUE.equals(parser.getOptionValue(helpOption))) {
            parser.printUsage();
            System.exit(0);
        }

        // Extract the values entered for the various options -- if the
        // options were not specified, the corresponding values will be
        // null.
        Boolean verboseValue = (Boolean) parser.getOptionValue(debugOption);
        String nameValue = (String) parser.getOptionValue(sqlOption);

        // For testing purposes, we just print out the option values
        System.out.println("verbose: " + verboseValue);
        System.out.println("sql: " + nameValue);

        // Extract the trailing command-line arguments ('a_nother') in the
        // usage string above.
        String[] otherArgs = parser.getRemainingArgs();
        System.out.println("remaining args: ");
        for (int i = 0; i < otherArgs.length; ++i) {
            System.out.println(otherArgs[i]);
        }

        // In a real program, one would pass the option values and other
        // arguments to a function that does something more useful.

        System.exit(0);
    }

}
