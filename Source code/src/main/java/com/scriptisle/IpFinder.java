package com.scriptisle;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class IpFinder {
    private String myIp = null;
    private Map<InetAddress, String> otherIp = null;

    //create an object for your ip
    public IpFinder() {
        findMyIp();
    }

    //create an object for other ips on your connection
    public IpFinder(String myIp) {
        myIp = myIp.substring(0, myIp.lastIndexOf('.'));
        this.otherIp = new HashMap<InetAddress, String>();

        findOtherIp(myIp);
    }

    //Find your ip using socket
    private void findMyIp() {
        try {
            Socket s = new Socket("www.google.com", 80);
            this.myIp = s.getLocalAddress().getHostAddress();
            s.close();
        } catch (final Exception e) {
            System.out.println("Get an error trying to find your ip.\n" + e);
            System.exit(1);
        }
    }

    //Find other ip addresses connected to your router
    private void findOtherIp(String myIp) {
        CountDownLatch latch = new CountDownLatch(253);
        LinkedList<InetAddress> otherIp = new LinkedList<InetAddress>();

        for (int i = 1; i < 254; i++) {
            String host = myIp + "." + i;
            new Thread(() -> {
                try {
                    if (InetAddress.getByName(host).isReachable(5000)) {
                        this.otherIp.put(InetAddress.getByName(host), InetAddress.getByName(host).getHostName());
                    }
                } catch (final Exception e) {
                    System.out.println("Get an error trying to find other ip.\n" + e);
                    System.exit(1);
                }

                latch.countDown();
            }).start();
        }

        try {
            latch.await();
        } catch (final Exception e) {
            System.out.println("Get an error trying to wait other threads.\n" + e);
            System.exit(1);
        }
    }

    //Return your ip
    public String getMyIp() {
        return this.myIp;
    }

    //Return other ip addresses information
    public Map<InetAddress, String> getOtherIp() {
        return this.otherIp;
    }
}

