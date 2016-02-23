package me.mneri.jessy.dummy;

import fr.inria.jessy.DistributedJessy;
import fr.inria.jessy.Jessy;

public class DummyServer {
    private Jessy mJessy;

    public DummyServer() {
        try {
            mJessy = DistributedJessy.getInstance();
            mJessy.addEntity(DummyEntity.class);
            mJessy.addSecondaryIndex(DummyEntity.class, String.class, "mValue");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DummyServer client = new DummyServer();
    }
}
