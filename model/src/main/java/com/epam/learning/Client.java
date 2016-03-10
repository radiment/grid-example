package com.epam.learning;

/**
 * Created by Yevgeniy_Vtulkin on 3/10/2016.
 */
public class Client {
    private int id;
    private int balance;
    private String type;

    public Client(String type, int balance, int id) {
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
