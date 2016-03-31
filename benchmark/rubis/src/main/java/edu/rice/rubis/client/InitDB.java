/*
 * RUBiS
 * Copyright (C) 2002, 2003, 2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: jmob@objectweb.org
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
 *
 * Initial developer(s): Emmanuel Cecchet, Julie Marguerite
 * Contributor(s): Massimo Neri <hello@mneri.me>
 */

package edu.rice.rubis.client;

import static fr.inria.jessy.transaction.TransactionState.*;

import edu.rice.rubis.client.RUBiSProperties.CategoryDef;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

import java.util.Date;
import java.util.Random;

import org.imdea.rubis.benchmark.transaction.*;
import org.imdea.rubis.benchmark.util.TextUtils;

/**
 * This program initializes the RUBiS database according to the rubis.properties file
 * found in the classpath.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class InitDB {
    private Jessy mJessy;
    private RUBiSProperties mProps;
    private Random rand = new Random();

    public InitDB(RUBiSProperties props, Jessy jessy) {
        mJessy = jessy;
        mProps = props;
    }

    private int execTransaction(Transaction trans) {
        ExecutionHistory h = null;

        try {
            h = trans.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (h == null || h.getTransactionState() != COMMITTED)
            return -1;

        return 0;
    }

    private void generateBids() {
        float bid = 1.0f;

        for (int i = 0; i < mProps.getNbOfBids(); i++) {
            try {
                long itemId = rand.nextInt(mProps.getTotalItems());
                long userId = rand.nextInt(mProps.getNbOfUsers());

                StoreBidTransaction trans = new StoreBidTransaction(mJessy, i, userId, itemId, 1, bid, bid + 1.0f, new
                        Date());

                if (execTransaction(trans) == -1)
                    System.err.println("Failed to bid " + i + " on item " + itemId);

                bid += 1.0f;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateCategories() {
        for (int i = 0; i < mProps.getCategories().size(); i++) {
            try {
                String name = mProps.getCategories().get(i).name;
                RegisterCategoryTransaction trans = new RegisterCategoryTransaction(mJessy, i, name);

                if (execTransaction(trans) == -1)
                    System.err.println("Failed to add category " + name + " (" + i + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateComments() {
        int[] values = {-5, -3, 0, 3, 5};
        int rating = values[rand.nextInt(5)];

        for (int i = 0; i < mProps.getNbOfComments(); i++) {
            String comment = TextUtils.randomString(1, mProps.getCommentMaxLength());
            long fromId = rand.nextInt(mProps.getNbOfUsers());
            long toId = rand.nextInt(mProps.getNbOfUsers());
            long itemId = rand.nextInt(mProps.getTotalItems());

            try {
                StoreCommentTransaction trans = new StoreCommentTransaction(mJessy, i, fromId, toId, itemId, rating,
                        new Date(), comment);

                if (execTransaction(trans) == -1)
                    System.err.println("Failed to add comment for item #" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method add items to the database according to the parameters
     * given in the database.properties file.
     */
    private void generateItems() {
        long itemId = 0;

        for (int i = 0; i < mProps.getCategories().size(); i++) {
            CategoryDef def = mProps.getCategories().get(i);

            for (int j = 0; j < def.nbOfItems; j++) {
                String name = "RUBiS automatically generated item #" + j;
                String description = TextUtils.randomString(1, mProps.getCommentMaxLength());
                float initialPrice = rand.nextFloat() * 5000 + 1;
                int duration = rand.nextInt(7) + 1;
                float reservePrice = initialPrice + rand.nextFloat() * 1000;
                float buyNow = rand.nextFloat() * 1000;
                int quantity = rand.nextInt(100) + 100;

                long sellerId = rand.nextInt(mProps.getNbOfUsers()) + 1;

                try {
                    Transaction trans = new RegisterItemTransaction(mJessy, itemId++, name, description, initialPrice,
                            quantity, reservePrice, buyNow, 0, 0.0f, new Date(), new Date(), sellerId, j);

                    if (execTransaction(trans) == -1)
                        System.err.println("Failed to add item " + name + " (" + j + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateRegions() {
        for (int i = 0; i < mProps.getRegions().size(); i++) {
            try {
                String name = mProps.getRegions().get(i);
                RegisterRegionTransaction trans = new RegisterRegionTransaction(mJessy, i, name);

                if (execTransaction(trans) == -1)
                    System.err.println("Failed to add region " + name + " (" + i + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method add users to the database according to the parameters
     * given in the database.properties file.
     */
    private void generateUsers() {
        int nbOfUsers = mProps.getNbOfUsers();
        int nbOfRegions = mProps.getNbOfRegions();

        for (int i = 0; i < nbOfUsers; i++) {
            String firstname = "Great" + i;
            String lastname = "User" + i;
            String nickname = "user" + i;
            String email = firstname + "." + lastname + "@rubis.com";
            String password = "password" + i;
            String regionName = mProps.getRegions().get(i % nbOfRegions);

            try {
                RegisterUserTransaction trans = new RegisterUserTransaction(mJessy, i, firstname, lastname,
                        nickname, password, email, regionName);

                if (execTransaction(trans) == -1) {
                    System.err.println("Failed to add user " + firstname + "|" + lastname + "|" + nickname + "|" +
                            email + "|" + password + "|" + regionName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        System.out.println("Generating categories");
        generateCategories();
        System.out.println("Generating regions");
        generateRegions();
        System.out.println("Generating users");
        generateUsers();
        System.out.println("Generating items");
        generateItems();
        System.out.println("Generating bids");
        generateBids();
        System.out.println("Generating comments");
        generateComments();
        Runtime.getRuntime().exit(0);
    }
}
