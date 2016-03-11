package com.epam.learning;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import java.io.Serializable;

public class Client implements Serializable {

    @QuerySqlField(index = true)
    private int id;
    @QuerySqlField
    private int balance;
    @QuerySqlField(index = true)
    private String type;

    public Client(int id, int balance, String type) {
        this.type = type;
        this.balance = balance;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
