package com.sl.zklock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

public class DistributeLock {

    private static ZkClient client;

    private String current_path;

    private String wait_path;

    private String root_path = "/lock3";


    public DistributeLock() {
        if (!client.exists(root_path)) {
            client.createPersistent(root_path);
        }
    }

    static {
        client = new ZkClient("localhost:2181");
    }

    public void lock() {
        if (tryLock()) {
            System.out.println(Thread.currentThread().getName()
                    + "->" + current_path + "获得锁成功");
            return;
        }
        try {
            waitForLock(wait_path);
            System.out.println(Thread.currentThread().getName()
                    + "->" + current_path + "获得锁成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForLock(String prev) throws Exception {
        if (client.exists(prev)) {
            System.out.println(Thread.currentThread().getName()
                    + "等待的节点是:" + prev);
            CountDownLatch latch = new CountDownLatch(1);
            client.subscribeDataChanges(prev, new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    latch.countDown();
                }

                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                    //latch.countDown();
                }
            });
            latch.await();
        }
    }

    public boolean tryLock() {
        current_path = client.createEphemeralSequential(root_path + "/", "1");
        System.out.println
                (Thread.currentThread().getName() +
                        "->创建节点:" + current_path);

        List<String> children = client.getChildren(root_path);
        SortedSet<String> sortSet = new TreeSet<>();
        for (String str : children) {
            sortSet.add(str);
        }
        String first = sortSet.first();
        if (first.equals(current_path.replace(root_path + "/", ""))) {
            return true;
        }
        SortedSet<String> headSet = sortSet.headSet(current_path.replace(root_path + "/", ""));
        if (!headSet.isEmpty()) {
            wait_path = root_path + "/" + headSet.last();
        }
        return false;
    }

    public void unlock() {
        client.writeData(current_path, "hello");
    }

}
