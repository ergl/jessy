package me.mneri.jessy.dummy;

import fr.inria.jessy.DistributedJessy;
import fr.inria.jessy.Jessy;

public class DummyClient {
    private Jessy mJessy;

    public DummyClient() {
        try {
            mJessy = DistributedJessy.getInstance();
            mJessy.addEntity(DummyEntity.class);
            mJessy.addSecondaryIndex(DummyEntity.class, String.class, "mValue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        for (int i = 0; i < 10; i++) {
            try {
                (new InsertTransaction(mJessy, Integer.toString(i), Integer.toString(i % 2))).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            (new ReadTransaction(mJessy, Integer.toString(4))).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            (new ReadBySecondaryTransaction(mJessy, Integer.toString(0))).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DummyClient client = new DummyClient();
        client.execute();
    }
}
