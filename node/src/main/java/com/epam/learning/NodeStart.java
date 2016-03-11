package com.epam.learning;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

import java.util.Random;

public class NodeStart {

    private static final Random random = new Random();

    public static void main(String[] args) {
        Ignite ignite = Ignition.start("config/ignite-config.xml");
        initCache(ignite);
    }

    private static void initCache(Ignite ignite) {
        IgniteCache<Integer, Client> clients = ignite.cache("clients");
        for (int i = 0; i < 30; i++) {
            clients.putIfAbsent(i, client(i));
        }
    }

    private static Client client(int i) {
        return new Client(i, random.nextInt(100000), "ph" + random.nextInt(5));
    }
}
