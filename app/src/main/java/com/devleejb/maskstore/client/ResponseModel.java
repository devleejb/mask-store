package com.devleejb.maskstore.client;

public class ResponseModel<T> {
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public T getStores() {
        return stores;
    }

    public void setStores(T body) {
        this.stores = stores;
    }

    private T stores;
}
