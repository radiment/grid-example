package com.epam.learning;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

import java.util.Random;

public class NodeStart {

    private static final Random random = new Random();

    public static void main(String[] args) {
        Ignite ignite = Ignition.start("config/ignite-config.xml");
        initCache(args, ignite);
    }

    private static void initCache(String[] args, Ignite ignite) {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        int start = Integer.parseInt(args[0]);
        int end = Integer.parseInt(args[1]);
        for (int i = start; i < end; i++) {
            clients.put(i, client(i));
        }
    }

    private static Client client(int i) {
        return new Client(i, random.nextInt(100000), "ph");
    }
}
