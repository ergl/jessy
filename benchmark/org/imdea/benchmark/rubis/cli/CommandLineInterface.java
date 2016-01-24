package org.imdea.benchmark.rubis.cli;

import static org.imdea.benchmark.rubis.cli.Constants.*;

import java.io.File;

import org.apache.commons.cli.*;

import org.imdea.benchmark.rubis.RUBiSBenchmark;

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
