package org.imdea.benchmark.rubis;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.imdea.benchmark.rubis.util.TextUtils;

public class RUBiSBenchmark {
    private static final int INITIAL_CATEGORIES = 200;
    private static final int INITIAL_REGIONS = 200;
    private static final int INITIAL_USERS = 100;
    private static final int INITIAL_ITEMS = INITIAL_USERS * 2;

    private static final float BIDS_MIN_BID = 10.0f;
    private static final float BIDS_MAX_BID = 200.0f;
    private static final float BIDS_MIN_MAX_BID = 50.0f;
    private static final float BIDS_MAX_MAX_BID = 200.0f;

    private static final int CATEGORY_NAME_MIN_LENGTH = 8;
    private static final int CATEGORY_NAME_MAX_LENGTH = 16;

    private static final int COMMENT_COMMENT_MIN_LENGTH = 100;
    private static final int COMMENT_COMMENT_MAX_LENGTH = 500;

    private static final int ITEM_DESC_MIN_LENGTH = 100;
    private static final int ITEM_DESC_MAX_LENGTH = 500;
    private static final int ITEM_NAME_MIN_LENGTH = 8;
    private static final int ITEM_NAME_MAX_LENGTH = 24;
    private static final float ITEM_MAX_BUY_NOW = 200.0f;
    private static final float ITEM_MAX_PRICE = 100.0f;
    private static final int ITEM_MAX_QUANTITY = 100;
    private static final float ITEM_MAX_RESERVE_PRICE = 100.0f;

    private static final int REGION_NAME_MIN_LENGTH = 8;
    private static final int REGION_NAME_MAX_LENGTH = 16;

    private static final int USER_EMAIL_MIN_LENGTH = 10;
    private static final int USER_EMAIL_MAX_LENGTH = 20;
    private static final int USER_FIRSTNAME_MIN_LENGTH = 5;
    private static final int USER_FIRSTNAME_MAX_LENGTH = 10;
    private static final int USER_LASTNAME_MIN_LENGTH = 5;
    private static final int USER_LASTNAME_MAX_LENGTH = 10;
    private static final int USER_NICKNAME_MIN_LENGTH = 8;
    private static final int USER_NICKNAME_MAX_LENGTH = 16;
    private static final int USER_PASSWORD_MIN_LENGTH = 8;
    private static final int USER_PASSWORD_MAX_LENGTH = 16;

    private RUBiSClient mClient;

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

    private void issueRandomAboutMe() {
        Random rand = new Random();
        long userId = rand.nextLong() % INITIAL_USERS;
        mClient.aboutMe(userId);
    }

    private void issueRandomSearchItemsByCategory() {
        Random rand = new Random();
        long categoryId = rand.nextLong() % INITIAL_CATEGORIES;
        mClient.searchItemByCategory(categoryId);
    }

    private void issueRandomSearchItemsByRegion() {
        Random rand = new Random();
        long regionId = rand.nextLong() % INITIAL_REGIONS;
        mClient.searchItemByRegion(regionId);
    }

    private void issueRandomViewBidHistory() {
        Random rand = new Random();
        long itemId = rand.nextLong() % INITIAL_ITEMS;
        mClient.viewBidHistory(itemId);
    }

    private void issueRandomViewItem() {
        Random rand = new Random();
        long itemId = rand.nextLong() % INITIAL_ITEMS;
        mClient.viewItem(itemId);
    }

    private void issueRandomUserInfo() {
        Random rand = new Random();
        long userId = rand.nextLong() % INITIAL_USERS;
        mClient.viewUserInfo(userId);
    }

    private void registerRandomBid() {
        Random rand = new Random();
        long userId = rand.nextLong() % INITIAL_USERS;
        long itemId = rand.nextLong() % INITIAL_ITEMS;
        int qty = 1;
        float bid = rand.nextFloat() * (BIDS_MAX_BID - BIDS_MIN_BID) + BIDS_MIN_BID;
        float maxBid = rand.nextFloat() * (BIDS_MAX_MAX_BID - BIDS_MAX_MAX_BID) + BIDS_MIN_MAX_BID;
        mClient.storeBid(userId, itemId, qty, bid, maxBid);
    }

    private void registerRandomBuyNow() {
        Random rand = new Random();
        long buyerId = rand.nextLong() % INITIAL_USERS;
        long itemId = rand.nextLong() % INITIAL_ITEMS;
        int qty = 1;
        mClient.storeBuyNow(buyerId, itemId, qty);
    }

    private void registerRandomCategory() {
        String name = TextUtils.randomString(CATEGORY_NAME_MIN_LENGTH, CATEGORY_NAME_MAX_LENGTH);
        mClient.registerCategory(name);
    }

    private void registerRandomComment() {
        Random rand = new Random();
        long fromUserId = rand.nextLong() % INITIAL_USERS;
        long toUserId = rand.nextLong() % INITIAL_USERS;
        long itemId = rand.nextLong() % INITIAL_ITEMS;
        int rating = rand.nextInt(6);
        String comment = TextUtils.randomString(COMMENT_COMMENT_MIN_LENGTH, COMMENT_COMMENT_MAX_LENGTH);
        mClient.storeComment(fromUserId, toUserId, itemId, rating, comment);
    }

    private void registerRandomItem() {
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
        mClient.registerItem(name, description, initialPrice, quantity, reservePrice, buyNow, startDate, endDate,
                seller, category);
    }

    private void registerRandomRegion() {
        String name = TextUtils.randomString(REGION_NAME_MIN_LENGTH, REGION_NAME_MAX_LENGTH);
        mClient.registerRegion(name);
    }

    private void registerRandomUser() {
        Random rand = new Random();
        String firstname = TextUtils.randomString(USER_FIRSTNAME_MIN_LENGTH, USER_FIRSTNAME_MAX_LENGTH);
        String lastname = TextUtils.randomString(USER_LASTNAME_MIN_LENGTH, USER_LASTNAME_MAX_LENGTH);
        String nickname = TextUtils.randomString(USER_NICKNAME_MIN_LENGTH, USER_NICKNAME_MAX_LENGTH);
        String password = TextUtils.randomString(USER_PASSWORD_MIN_LENGTH, USER_PASSWORD_MAX_LENGTH);
        String email = TextUtils.randomString(USER_EMAIL_MIN_LENGTH, USER_EMAIL_MAX_LENGTH);
        long region = rand.nextInt(INITIAL_REGIONS);
        mClient.registerUser(firstname, lastname, nickname, password, email, region);
    }

    public void start(int iters) {
        mClient.connect();
        init();
        workload(iters);
        mClient.end();
        System.out.println("Hello");
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

                    for (int i = 0; i < iters; i++) {
                        float choice = random.nextFloat();

                        if (choice <= 0.005f) {
                            registerRandomItem();
                        } else if (choice <= 0.01f) {
                            issueRandomAboutMe();
                        } else if (choice <= 0.015f) {
                            registerRandomCategory();
                        } else if (choice <= 0.02f) {
                            registerRandomItem();
                        } else if (choice <= 0.26f) {
                            registerRandomRegion();
                        } else if (choice <= 0.5f) {
                            issueRandomSearchItemsByCategory();
                        } else if (choice <= 0.51f) {
                            issueRandomSearchItemsByRegion();
                        } else if (choice <= 0.52f) {
                            registerRandomBid();
                        } else if (choice <= 0.53f) {
                            registerRandomBuyNow();
                        } else if (choice <= 0.73f) {
                            registerRandomComment();
                        } else if (choice <= 0.51f) {
                            issueRandomViewBidHistory();
                        } else if (choice <= 0.99f) {
                            issueRandomViewItem();
                        } else if (choice <= 1f) {
                            issueRandomUserInfo();
                        }
                    }
                }
            });
        }
    }
}
