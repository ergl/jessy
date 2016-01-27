package org.imdea.benchmark.rubis;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.imdea.benchmark.rubis.util.TextUtils;

public class RUBiSBenchmark {
    private static final float BIDS_MAX_BID = 200.0f;
    private static final float BIDS_MAX_MAX_BID = 200.0f;
    private static final float BIDS_MIN_BID = 10.0f;
    private static final float BIDS_MIN_MAX_BID = 50.0f;

    private static final int CATEGORY_NAME_MAX_LENGTH = 16;
    private static final int CATEGORY_NAME_MIN_LENGTH = 8;

    private static final int COMMENT_COMMENT_MAX_LENGTH = 500;
    private static final int COMMENT_COMMENT_MIN_LENGTH = 100;

    private static final int INITIAL_CATEGORIES = 200;
    private static final int INITIAL_REGIONS = 200;
    private static final int INITIAL_USERS = 100;
    private static final int INITIAL_ITEMS = INITIAL_USERS * 2;

    private static final int ITEM_DESC_MAX_LENGTH = 500;
    private static final int ITEM_DESC_MIN_LENGTH = 100;
    private static final float ITEM_MAX_BUY_NOW = 200.0f;
    private static final float ITEM_MAX_PRICE = 100.0f;
    private static final int ITEM_MAX_QUANTITY = 100;
    private static final float ITEM_MAX_RESERVE_PRICE = 100.0f;
    private static final int ITEM_NAME_MAX_LENGTH = 24;
    private static final int ITEM_NAME_MIN_LENGTH = 8;

    private static final int REGION_NAME_MAX_LENGTH = 16;
    private static final int REGION_NAME_MIN_LENGTH = 8;

    private static final int USER_EMAIL_MAX_LENGTH = 20;
    private static final int USER_EMAIL_MIN_LENGTH = 10;
    private static final int USER_FIRSTNAME_MAX_LENGTH = 10;
    private static final int USER_FIRSTNAME_MIN_LENGTH = 5;
    private static final int USER_LASTNAME_MAX_LENGTH = 10;
    private static final int USER_LASTNAME_MIN_LENGTH = 5;
    private static final int USER_NICKNAME_MAX_LENGTH = 16;
    private static final int USER_NICKNAME_MIN_LENGTH = 8;
    private static final int USER_PASSWORD_MAX_LENGTH = 16;
    private static final int USER_PASSWORD_MIN_LENGTH = 8;

    private int mAborted;
    private RUBiSClient mClient;
    private int mCommitted;
    private final Object mLock = new Object();

    public RUBiSBenchmark() {
        try {
            mClient = new RUBiSClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        for (int i = 0; i < INITIAL_CATEGORIES; i++)
            registerRandomCategory();

        for (int i = 0; i < INITIAL_REGIONS; i++)
            registerRandomRegion();

        for (int i = 0; i < INITIAL_USERS; i++)
            registerRandomUser();

        for (int i = 0; i < INITIAL_ITEMS; i++)
            registerRandomItem();
    }

    private int issueRandomAboutMe() {
        Random rand = new Random();
        long userId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        return mClient.aboutMe(userId);
    }

    private int issueRandomSearchItemsByCategory() {
        Random rand = new Random();
        long categoryId = Math.abs(rand.nextLong()) % INITIAL_CATEGORIES;
        return mClient.searchItemByCategory(categoryId);
    }

    private int issueRandomSearchItemsByRegion() {
        Random rand = new Random();
        long categoryId = Math.abs(rand.nextLong()) % INITIAL_CATEGORIES;
        long regionId = Math.abs(rand.nextLong()) % INITIAL_REGIONS;
        return mClient.searchItemByRegion(regionId, categoryId);
    }

    private int issueRandomUserInfo() {
        Random rand = new Random();
        long userId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        return mClient.viewUserInfo(userId);
    }

    private int issueRandomViewBidHistory() {
        Random rand = new Random();
        long itemId = Math.abs(rand.nextLong()) % INITIAL_ITEMS;
        return  mClient.viewBidHistory(itemId);
    }

    private int issueRandomViewItem() {
        Random rand = new Random();
        long itemId = Math.abs(rand.nextLong()) % INITIAL_ITEMS;
        return mClient.viewItem(itemId);
    }

    private int registerRandomBid() {
        Random rand = new Random();
        long userId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        long itemId = Math.abs(rand.nextLong()) % INITIAL_ITEMS;
        int qty = 1;
        float bid = rand.nextFloat() * (BIDS_MAX_BID - BIDS_MIN_BID) + BIDS_MIN_BID;
        float maxBid = rand.nextFloat() * (BIDS_MAX_MAX_BID - BIDS_MAX_MAX_BID) + BIDS_MIN_MAX_BID;
        return mClient.storeBid(userId, itemId, qty, bid, maxBid);
    }

    private int registerRandomBuyNow() {
        Random rand = new Random();
        long buyerId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        long itemId = Math.abs(rand.nextLong()) % INITIAL_ITEMS;
        int qty = 1;
        return mClient.storeBuyNow(buyerId, itemId, qty);
    }

    private int registerRandomCategory() {
        String name = TextUtils.randomString(CATEGORY_NAME_MIN_LENGTH, CATEGORY_NAME_MAX_LENGTH);
        return mClient.registerCategory(name);
    }

    private int registerRandomComment() {
        Random rand = new Random();
        long fromUserId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        long toUserId = Math.abs(rand.nextLong()) % INITIAL_USERS;
        long itemId = Math.abs(rand.nextLong()) % INITIAL_ITEMS;
        int rating = rand.nextInt(6);
        String comment = TextUtils.randomString(COMMENT_COMMENT_MIN_LENGTH, COMMENT_COMMENT_MAX_LENGTH);
        return mClient.storeComment(fromUserId, toUserId, itemId, rating, comment);
    }

    private int registerRandomItem() {
        Random rand = new Random();
        String name = TextUtils.randomString(ITEM_NAME_MIN_LENGTH, ITEM_NAME_MAX_LENGTH);
        String description = TextUtils.randomString(ITEM_DESC_MIN_LENGTH, ITEM_DESC_MAX_LENGTH);
        float initialPrice = rand.nextFloat() * ITEM_MAX_PRICE;
        int quantity = rand.nextInt(ITEM_MAX_QUANTITY);
        float reservePrice = rand.nextFloat() * ITEM_MAX_RESERVE_PRICE;
        float buyNow = rand.nextFloat() * ITEM_MAX_BUY_NOW;
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (30l * 24l * 60l * 60l * 1000l));
        long seller = rand.nextInt(INITIAL_USERS);
        long category = rand.nextInt(INITIAL_CATEGORIES);
        return mClient.registerItem(name, description, initialPrice, quantity, reservePrice, buyNow, startDate, endDate,
                seller, category);
    }

    private int registerRandomRegion() {
        String name = TextUtils.randomString(REGION_NAME_MIN_LENGTH, REGION_NAME_MAX_LENGTH);
        return mClient.registerRegion(name);
    }

    private int registerRandomUser() {
        Random rand = new Random();
        String firstname = TextUtils.randomString(USER_FIRSTNAME_MIN_LENGTH, USER_FIRSTNAME_MAX_LENGTH);
        String lastname = TextUtils.randomString(USER_LASTNAME_MIN_LENGTH, USER_LASTNAME_MAX_LENGTH);
        String nickname = TextUtils.randomString(USER_NICKNAME_MIN_LENGTH, USER_NICKNAME_MAX_LENGTH);
        String password = TextUtils.randomString(USER_PASSWORD_MIN_LENGTH, USER_PASSWORD_MAX_LENGTH);
        String email = TextUtils.randomString(USER_EMAIL_MIN_LENGTH, USER_EMAIL_MAX_LENGTH);
        long region = rand.nextInt(INITIAL_REGIONS);
        return mClient.registerUser(firstname, lastname, nickname, password, email, region);
    }

    public void start(int iters) {
        mClient.connect();
        init();
        workload(iters);
        //mClient.end();
    }

    private void workload(final int iters) {
        int physicalThreads = Runtime.getRuntime().availableProcessors() + 1;
        int virtualThreads = 2;
        Executor executor = Executors.newFixedThreadPool(physicalThreads);

        for (int i = 0; i < virtualThreads; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Random random = ThreadLocalRandom.current();
                    int outcome = 1;

                    for (int i = 0; i < iters; i++) {
                        float choice = random.nextFloat();

                        if (choice <= 0.005f) {
                            outcome = registerRandomItem();
                        } else if (choice <= 0.01f) {
                            outcome = issueRandomAboutMe();
                        } else if (choice <= 0.015f) {
                            outcome = registerRandomCategory();
                        } else if (choice <= 0.02f) {
                            outcome = registerRandomUser();
                        } else if (choice <= 0.26f) {
                            outcome = registerRandomRegion();
                        } else if (choice <= 0.5f) {
                            outcome = issueRandomSearchItemsByCategory();
                        } else if (choice <= 0.51f) {
                            outcome = issueRandomSearchItemsByRegion();
                        } else if (choice <= 0.52f) {
                            outcome = registerRandomBid();
                        } else if (choice <= 0.53f) {
                            outcome = registerRandomBuyNow();
                        } else if (choice <= 0.73f) {
                            outcome = registerRandomComment();
                        } else if (choice <= 0.51f) {
                            outcome = issueRandomViewBidHistory();
                        } else if (choice <= 0.99f) {
                            outcome = issueRandomViewItem();
                        } else {
                            outcome = issueRandomUserInfo();
                        }

                        synchronized (mLock) {
                            if (outcome == RUBiSClient.EXIT_SUCCESS)
                                mCommitted++;
                            else
                                mAborted++;

                            if ((mCommitted + mAborted) % 10 == 0)
                                System.out.println(mCommitted + "/" + mAborted + "]");
                        }
                    }
                }
            });
        }
    }
}
