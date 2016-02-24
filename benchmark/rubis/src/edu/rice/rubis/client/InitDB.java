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
 * @author
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */

public class InitDB {
    private Jessy mJessy;
    private Random rand = new Random();
    private RUBiSProperties mProps;

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

    public void generateBids() {
        for (int i = 0; i < mProps.getTotalItems(); i++) {
            float bid = 1.0f;

            for (int j = 0; j < mProps.getMaxBidsPerItem(); j++) {
                try {
                    StoreBidTransaction trans = new StoreBidTransaction(mJessy, j, rand.nextInt(mProps.getNbOfUsers()),
                            i, 1, bid, bid + 1.0f, new Date());
                    int state = execTransaction(trans);

                    if (state == -1)
                        System.err.println("Failed to bid " + j + " on item " + i);

                    bid += 2.0f;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void generateCategories() {
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

    public void generateComments() {
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
                int state = execTransaction(trans);

                if (state == -1)
                    System.err.println("Failed to add comment for item #" + (i + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method add items to the database according to the parameters
     * given in the database.properties file.
     */
    public void generateItems() {
        for (CategoryDef def : mProps.getCategories()) {
            for (int i = 0; i < def.nbOfItems; i++) {
                String name = "RUBiS automatically generated item #" + (i + 1);
                String description = TextUtils.randomString(1, mProps.getCommentMaxLength());
                float initialPrice = rand.nextFloat() * 5000 + 1;
                int duration = rand.nextInt(7) + 1;

                float reservePrice;

                if (i < mProps.getPercentReservePrice() * mProps.getTotalItems() / 100)
                    reservePrice = rand.nextInt(1000) + initialPrice;
                else
                    reservePrice = 0;

                float buyNow;

                if (i < mProps.getPercentBuyNow() * mProps.getTotalItems() / 100)
                    buyNow = rand.nextInt(1000) + initialPrice + reservePrice;
                else
                    buyNow = 0;

                int quantity;

                if (i < mProps.getPercentUniqueItems() * mProps.getTotalItems() / 100)
                    quantity = 1;
                else
                    quantity = rand.nextInt(mProps.getMaxItemQty()) + 1;

                long sellerId = rand.nextInt(mProps.getNbOfUsers()) + 1;

                try {
                    Transaction trans = new RegisterItemTransaction(mJessy, i + 1, name, description, initialPrice,
                            quantity, reservePrice, buyNow, 0, 0.0f, new Date(), new Date(), sellerId, i);
                    int state = execTransaction(trans);

                    if (state == -1)
                        System.err.println("Failed to add item " + name + " (" + (i + 1) + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void generateRegions() {
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
    public void generateUsers() {
        String firstname;
        String lastname;
        String nickname;
        String email;
        String password;
        String regionName;
        int i;

        int nbOfUsers = mProps.getNbOfUsers();
        int nbOfRegions = mProps.getNbOfRegions();

        for (i = 0; i < nbOfUsers; i++) {
            firstname = "Great" + (i + 1);
            lastname = "User" + (i + 1);
            nickname = "user" + (i + 1);
            email = firstname + "." + lastname + "@rubis.com";
            password = "password" + (i + 1);
            regionName = mProps.getRegions().get(i % nbOfRegions);

            try {
                RegisterUserTransaction trans = new RegisterUserTransaction(mJessy, i + 1, firstname, lastname,
                        nickname, password, email, regionName);
                int state = execTransaction(trans);

                if (state == -1) {
                    System.err.println("Failed to add user " + firstname + "|" + lastname + "|" + nickname + "|" +
                            email + "|" + password + "|" + regionName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i % 100 == 0)
                System.out.print(".");
        }

        System.out.println(" Done!");
    }
}
