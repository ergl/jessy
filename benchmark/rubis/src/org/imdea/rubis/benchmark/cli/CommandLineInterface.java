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

import edu.rice.rubis.client.ClientEmulator;
import edu.rice.rubis.client.InitDB;

import fr.inria.jessy.DistributedJessy;
import fr.inria.jessy.Jessy;

import java.io.File;

import org.apache.commons.cli.*;

import org.imdea.rubis.benchmark.entity.*;

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
    	OptionGroup group = new OptionGroup();
    	Option help = Option.builder(OPT_HELP)
    			.longOpt(OPT_HELP_LONG)
    			.desc(OPT_HELP_DESC)
    			.build();
    	group.addOption(help);
        mOptions.addOptionGroup(group);
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
                String propFileName = cmd.getOptionValue(OPT_PROPERTIES);
                startEmulation(propFileName);
            }
        } catch (Exception e) {
            printHelp();
        }
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getJarName(), mOptions, true);
    }

    private void setupJessyInstance(Jessy jessy) {
        try {
            jessy.addEntity(BidEntity.class);
            jessy.addEntity(BuyNowEntity.class);
            jessy.addEntity(CategoryEntity.class);
            jessy.addEntity(CategoryEntity.class);
            jessy.addEntity(CommentEntity.class);
            jessy.addEntity(IndexEntity.class);
            jessy.addEntity(ItemEntity.class);
            jessy.addEntity(RegionEntity.class);
            jessy.addEntity(ScannerEntity.class);
            jessy.addEntity(UserEntity.class);
            jessy.addSecondaryIndex(BidEntity.class, Long.class, "mItemId");
            jessy.addSecondaryIndex(BidEntity.class, Long.class, "mUserId");
            jessy.addSecondaryIndex(BuyNowEntity.class, Long.class, "mBuyerId");
            jessy.addSecondaryIndex(BuyNowEntity.class, Long.class, "mItemId");
            jessy.addSecondaryIndex(CommentEntity.class, Long.class, "mFromUserId");
            jessy.addSecondaryIndex(CommentEntity.class, Long.class, "mItemId");
            jessy.addSecondaryIndex(CommentEntity.class, Long.class, "mToUserId");
            jessy.addSecondaryIndex(ItemEntity.class, Long.class, "mCategory");
            jessy.addSecondaryIndex(ItemEntity.class, Long.class, "mSeller");
            jessy.addSecondaryIndex(UserEntity.class, String.class, "mNickname");
            jessy.addSecondaryIndex(UserEntity.class, Long.class, "mRegion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startEmulation(String propFileName) {
        try {
            Jessy jessy = DistributedJessy.getInstance();
            setupJessyInstance(jessy);

            ClientEmulator client = new ClientEmulator(jessy);
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
