/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.imdea.rubis.benchmark.cli;

import static org.imdea.rubis.benchmark.cli.Constants.*;

import java.io.File;

import org.apache.commons.cli.*;
import org.imdea.rubis.benchmark.RUBiSBenchmark;

public class CommandLineInterface {
    private String[] mArgs;
    private Options mOptions = new Options();

    public CommandLineInterface(String[] args) {
        mArgs = args;
        init();
    }

    private String getJarName() {
        File file = new File(CommandLineInterface.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.getName();
    }

    private void init() {
        mOptions.addOption(OPT_HELP, OPT_HELP_LONG, false, OPT_HELP_DESC);
        mOptions.addOption(OPT_ITERS, OPT_ITERS_LONG, true, OPT_ITERS_DESC);
    }

    public static void main(String... args) {
        CommandLineInterface cli = new CommandLineInterface(args);
        cli.parse();
    }

    private void parse() {
        try {
            CommandLineParser cmdParser = new DefaultParser();
            CommandLine cmd = cmdParser.parse(mOptions, mArgs);

            if (cmd.hasOption(OPT_HELP)) {
                printHelp();
            } else {
                int iters;

                if (cmd.hasOption(OPT_ITERS))
                    iters = Integer.parseInt(cmd.getOptionValue(OPT_ITERS));
                else
                    iters = OPT_ITERS_DEFAULT;

                RUBiSBenchmark benchmark = new RUBiSBenchmark();
                benchmark.start(iters);
            }
        } catch (Exception e) {
            printHelp();
        }
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getJarName(), mOptions, true);
    }
}
