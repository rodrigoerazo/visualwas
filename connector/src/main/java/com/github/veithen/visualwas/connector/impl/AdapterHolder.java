package com.github.veithen.visualwas.connector.impl;

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.feature.AdapterFactory;
import com.github.veithen.visualwas.connector.feature.CloseListener;

final class AdapterHolder<T> {
    private final AdapterFactory<T> factory;
    private T adapter;
    
    AdapterHolder(AdapterFactory<T> factory) {
        this.factory = factory;
    }
    
    synchronized T getAdapter(AdminService adminService) {
        if (adapter == null) {
            adapter = factory.createAdapter(adminService);
        }
        return adapter;
    }
    
    synchronized void closing() {
        if (adapter instanceof CloseListener) {
            ((CloseListener)adapter).closing();
        }
    }
}
