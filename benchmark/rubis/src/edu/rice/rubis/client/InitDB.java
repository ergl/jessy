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

import fr.inria.jessy.Jessy;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

import java.net.URL;
import java.util.Date;
import java.util.Random;

import org.imdea.rubis.benchmark.transaction.RegisterItemTransaction;
import org.imdea.rubis.benchmark.transaction.RegisterUserTransaction;
import org.imdea.rubis.benchmark.transaction.StoreBidTransaction;
import org.imdea.rubis.benchmark.transaction.StoreCommentTransaction;
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
    private int[] itemsPerCategory;
    private Jessy mJessy;
    private Random rand = new Random();
    private RUBiSProperties rubis = null;

    /**
     * Creates a new <code>InitDB</code> instance.
     */
    public InitDB(Jessy jessy) {
        mJessy = jessy;
        rubis = new RUBiSProperties();
        itemsPerCategory = rubis.getItemsPerCategory();
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

    /**
     * This method add items to the database according to the parameters
     * given in the database.properties file.
     */
    public void generateItems() {
        // Items specific variables
        String name;
        String description;
        float initialPrice;
        float reservePrice;
        float buyNow;
        int duration;
        int quantity;
        int categoryId;
        int sellerId;
        int oldItems = rubis.getNbOfOldItems();
        int activeItems = rubis.getTotalActiveItems();
        int totalItems = oldItems + activeItems;
        int[] ratingValue = {-5, -3, 0, 3, 5};
        int nbBids;

        // All purpose variables
        int i, j;
        URL url;
        String HTTPreply;

        // Cache variables
        int getItemDescriptionLength = rubis.getItemDescriptionLength();
        float getPercentReservePrice = rubis.getPercentReservePrice();
        float getPercentBuyNow = rubis.getPercentBuyNow();
        float getPercentUniqueItems = rubis.getPercentUniqueItems();
        int getMaxItemQty = rubis.getMaxItemQty();
        int getCommentMaxLength = rubis.getCommentMaxLength();
        int getNbOfCategories = rubis.getNbOfCategories();
        int getNbOfUsers = rubis.getNbOfUsers();
        int getMaxBidsPerItem = rubis.getMaxBidsPerItem();

        System.out.println("Generating " + oldItems + " old items and " + activeItems + " active items.");

        for (i = 0; i < totalItems; i++) {
            // Generate the item
            name = "RUBiS automatically generated item #" + (i + 1);
            int descriptionLength = rand.nextInt(getItemDescriptionLength) + 1;
            description = TextUtils.randomString(descriptionLength);
            initialPrice = rand.nextInt(5000) + 1;
            duration = rand.nextInt(7) + 1;

            if (i < oldItems) { // This is an old item
                duration = -duration; // give a negative auction duration so that auction will be over
                if (i < getPercentReservePrice * oldItems / 100)
                    reservePrice = rand.nextInt(1000) + initialPrice;
                else
                    reservePrice = 0;
                if (i < getPercentBuyNow * oldItems / 100)
                    buyNow = rand.nextInt(1000) + initialPrice + reservePrice;
                else
                    buyNow = 0;
                if (i < getPercentUniqueItems * oldItems / 100)
                    quantity = 1;
                else
                    quantity = rand.nextInt(getMaxItemQty) + 1;
            } else {
                if (i < getPercentReservePrice * activeItems / 100)
                    reservePrice = rand.nextInt(1000) + initialPrice;
                else
                    reservePrice = 0;
                if (i < getPercentBuyNow * activeItems / 100)
                    buyNow = rand.nextInt(1000) + initialPrice + reservePrice;
                else
                    buyNow = 0;
                if (i < getPercentUniqueItems * activeItems / 100)
                    quantity = 1;
                else
                    quantity = rand.nextInt(getMaxItemQty) + 1;
            }

            categoryId = i % getNbOfCategories;

            while (itemsPerCategory[categoryId] == 0)
                categoryId = (categoryId + 1) % getNbOfCategories;

            if (i >= oldItems)
                itemsPerCategory[categoryId]--;

            sellerId = rand.nextInt(getNbOfUsers) + 1;

            try {
                Transaction trans = new RegisterItemTransaction(mJessy, i + 1, name, description, initialPrice,
                        quantity, reservePrice, buyNow, 0, 0.0f, new Date(), new Date(), sellerId, categoryId + 1);
                int state = execTransaction(trans);

                if (state == -1)
                    System.err.println("Failed to add item " + name + " (" + (i + 1) + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Generate random bids
            nbBids = rand.nextInt(getMaxBidsPerItem);

            for (j = 0; j < nbBids; j++) {
                int addBid = rand.nextInt(10) + 1;

                try {
                    StoreBidTransaction trans = new StoreBidTransaction(mJessy, j, rand.nextInt(getNbOfUsers) +
                            1, i + 1, quantity, initialPrice + addBid, initialPrice + addBid * 2, new Date());
                    int state = execTransaction(trans);

                    if (state == -1)
                        System.err.println("Failed to bid #" + j + " on item " + name);

                    initialPrice += addBid; // We use initialPrice as minimum bid
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Generate a comment for the item
            int rating = rand.nextInt(5);
            int commentLength = rand.nextInt(getCommentMaxLength) + 1;
            String comment = TextUtils.randomString(commentLength);

            try {
                StoreCommentTransaction trans = new StoreCommentTransaction(mJessy, i + 1, rand.nextInt(getNbOfUsers)
                        + 1, sellerId, i + 1, ratingValue[rating], new Date(), comment);
                int state = execTransaction(trans);

                if (state == -1)
                    System.err.println("Failed to add comment for item #" + (i + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (i % 10 == 0)
                System.out.print(".");
        }

        System.out.println(" Done!");
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

        int nbOfUsers = rubis.getNbOfUsers();
        int nbOfRegions = rubis.getNbOfRegions();

        for (i = 0; i < nbOfUsers; i++) {
            firstname = "Great" + (i + 1);
            lastname = "User" + (i + 1);
            nickname = "user" + (i + 1);
            email = firstname + "." + lastname + "@rubis.com";
            password = "password" + (i + 1);
            regionName = (String) rubis.getRegions().elementAt(i % nbOfRegions);

            // Call the HTTP server to register this user
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
