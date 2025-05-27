package com.emsi.fittracker.interfaces;

public interface DataCallback<T> {
    void onSuccess(T result);
    void onFailure(String error);
}
