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
 * Contributor(s): Jeremy Philippe, Niraj Tolia, Massimo Neri
 */
package edu.rice.rubis.client;

import edu.rice.rubis.exception.InvalidTransitionTableException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This program check and get all information for the rubis.properties file
 * found in the classpath.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a>
 * @author <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @author <a href="mailto:hello@mneri.me">Massimo Neri</a>
 */

public class RUBiSProperties {
    public static class CategoryDef {
        public final String name;
        public final int nbOfItems;

        CategoryDef(String name, int nbOfItems) {
            this.name = name;
            this.nbOfItems = nbOfItems;
        }
    }

    private List<CategoryDef> mCategories;
    private int mCommentMaxLength;
    private int mItemDescriptionLength;
    private int mMaxNbOfTransitions;
    private int mNbOfBids;
    private int mNbOfClients;
    private int mNbOfComments;
    private int mNbOfItems;
    private int mNbOfItemsPerPage;
    private int mNbOfUsers;
    private List<String> mRegions;
    private int mTableColumns;
    private String mTableName;
    private int mTableRows;

    public RUBiSProperties(String filename) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(filename));
        init(props);
    }

    public List<CategoryDef> getCategories() {
        return mCategories;
    }

    public int getCommentMaxLength() {
        return mCommentMaxLength;
    }

    public int getItemDescriptionLength() {
        return mItemDescriptionLength;
    }

    public int getMaxNbOfTransitions() {
        return mMaxNbOfTransitions;
    }

    public int getNbOfBids() {
        return mNbOfBids;
    }

    public int getNbOfCategories() {
        return mCategories.size();
    }

    public int getNbOfClients() {
        return mNbOfClients;
    }

    public int getNbOfComments() {
        return mNbOfComments;
    }

    public int getNbOfItemsPerPage() {
        return mNbOfItemsPerPage;
    }

    public int getNbOfRegions() {
        return mRegions.size();
    }

    public int getNbOfUsers() {
        return mNbOfUsers;
    }

    public List<String> getRegions() {
        return mRegions;
    }

    public int getTotalItems() {
        return mNbOfItems;
    }

    private void init(Properties props) {
        try {
            mNbOfClients = Integer.valueOf(props.getProperty("clients"));
            mNbOfComments = Integer.valueOf(props.getProperty("comments"));
            mMaxNbOfTransitions = Integer.valueOf(props.getProperty("transitions"));
            mNbOfItemsPerPage = Integer.valueOf(props.getProperty("items_per_page"));
            mNbOfUsers = Integer.valueOf(props.getProperty("users"));
            mItemDescriptionLength = Integer.valueOf(props.getProperty("items_description_length"));
            mNbOfBids = Integer.valueOf(props.getProperty("bids"));
            mCommentMaxLength = Integer.valueOf(props.getProperty("comments_max_length"));

            String regionsFile = props.getProperty("regions_file");
            readRegionsFile(regionsFile);

            String categoriesFile = props.getProperty("categories_file");
            readCategoriesFile(categoriesFile);

            mTableName = props.getProperty("transitions_file");
            mTableColumns = Integer.valueOf(props.getProperty("transitions_file_columns"));
            mTableRows = Integer.valueOf(props.getProperty("transitions_file_rows"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TransitionTable newTransitionTable() {
        TransitionTable table = new TransitionTable(mTableColumns, mTableRows);

        if (!table.readExcelTextFile(mTableName))
            throw new InvalidTransitionTableException();

        return table;
    }

    private void readCategoriesFile(String name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(name));
        Pattern pattern = Pattern.compile("(.)+\\s*\\(\\s*(\\d+)\\s*\\)");
        ArrayList<CategoryDef> categories = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String category = matcher.group(1);
                int num = Integer.valueOf(matcher.group(2));
                categories.add(new CategoryDef(category, num));
                mNbOfItems += num;
            } else {
                System.err.println("Regions: invalid format, skipped: \"" + line + "\"");
            }
        }

        categories.trimToSize();
        mCategories = Collections.unmodifiableList(categories);
        reader.close();
    }

    private void readRegionsFile(String name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(name));
        ArrayList<String> regions = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null)
            regions.add(line);

        regions.trimToSize();
        mRegions = Collections.unmodifiableList(regions);
        reader.close();
    }
}