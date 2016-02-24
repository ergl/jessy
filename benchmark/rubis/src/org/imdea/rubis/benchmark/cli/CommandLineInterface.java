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
import edu.rice.rubis.client.RUBiSProperties;

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
        Option client = Option.builder(OPT_CLIENT).longOpt(OPT_CLIENT_LONG).desc(OPT_CLIENT_DESC).build();
        group.addOption(client);
        Option server = Option.builder(OPT_SERVER).longOpt(OPT_SERVER_LONG).desc(OPT_SERVER_DESC).build();
        group.addOption(server);
    	Option help = Option.builder(OPT_HELP).longOpt(OPT_HELP_LONG).desc(OPT_HELP_DESC).build();
    	group.addOption(help);
        group.setRequired(true);
        mOptions.addOptionGroup(group);
        mOptions.addOption(OPT_PROPERTIES, OPT_PROPERTIES_LONG, true, OPT_PROPERTIES_DESC);
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
            } else if (cmd.hasOption(OPT_CLIENT)) {
                String propFileName = cmd.getOptionValue(OPT_PROPERTIES, OPT_PROPERTIES_DEFAULT);
                startClient(propFileName);
            } else if (cmd.hasOption(OPT_SERVER)) {
                startServer();
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
            jessy.addEntity(BidEntity.ItemIdIndex.class);
            jessy.addSecondaryIndex(BidEntity.ItemIdIndex.class, Long.class, "mItemId");
            jessy.addEntity(BidEntity.UserIdIndex.class);
            jessy.addSecondaryIndex(BidEntity.UserIdIndex.class, Long.class, "mUserId");
            jessy.addEntity(BuyNowEntity.class);
            jessy.addEntity(BuyNowEntity.BuyerIdIndex.class);
            jessy.addSecondaryIndex(BuyNowEntity.BuyerIdIndex.class, Long.class, "mBuyerId");
            jessy.addEntity(BuyNowEntity.ItemIdIndex.class);
            jessy.addSecondaryIndex(BuyNowEntity.ItemIdIndex.class, Long.class, "mItemId");
            jessy.addEntity(CategoryEntity.class);
            jessy.addEntity(CategoryEntity.Scanner.class);
            jessy.addSecondaryIndex(CategoryEntity.Scanner.class, String.class, "mDummy");
            jessy.addEntity(CommentEntity.class);
            jessy.addEntity(CommentEntity.FromUserIdIndex.class);
            jessy.addSecondaryIndex(CommentEntity.FromUserIdIndex.class, Long.class, "mFromUserId");
            jessy.addEntity(CommentEntity.ItemIdIndex.class);
            jessy.addSecondaryIndex(CommentEntity.ItemIdIndex.class, Long.class, "mItemId");
            jessy.addEntity(CommentEntity.ToUserIdIndex.class);
            jessy.addSecondaryIndex(CommentEntity.ToUserIdIndex.class, Long.class, "mToUserId");
            jessy.addEntity(ItemEntity.class);
            jessy.addEntity(ItemEntity.CategoryIdIndex.class);
            jessy.addSecondaryIndex(ItemEntity.CategoryIdIndex.class, Long.class, "mCategoryId");
            jessy.addEntity(ItemEntity.SellerIndex.class);
            jessy.addSecondaryIndex(ItemEntity.SellerIndex.class, Long.class, "mSeller");
            jessy.addEntity(RegionEntity.class);
            jessy.addEntity(RegionEntity.NameIndex.class);
            jessy.addSecondaryIndex(RegionEntity.NameIndex.class, String.class, "mName");
            jessy.addEntity(RegionEntity.Scanner.class);
            jessy.addSecondaryIndex(RegionEntity.Scanner.class, String.class, "mDummy");
            jessy.addEntity(UserEntity.class);
            jessy.addEntity(UserEntity.NicknameIndex.class);
            jessy.addSecondaryIndex(UserEntity.NicknameIndex.class, String.class, "mNickname");
            jessy.addEntity(UserEntity.RegionIdIndex.class);
            jessy.addSecondaryIndex(UserEntity.RegionIdIndex.class, Long.class, "mRegionId");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startClient(String propFileName) {
        try {
            Jessy jessy = DistributedJessy.getInstance();
            setupJessyInstance(jessy);

            RUBiSProperties properties = new RUBiSProperties(propFileName);
            ClientEmulator client = new ClientEmulator(properties, jessy);
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        try {
            Jessy jessy = DistributedJessy.getInstance();
            setupJessyInstance(jessy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
