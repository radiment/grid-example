package com.epam.learning;

import org.apache.ignite.Ignition;

public class NodeStart {
    public static void main(String[] args) {
        Ignition.start("config/ignite-config.xml");
    }
}
