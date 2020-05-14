package com.youtushuju.lingdongapp.device;

public interface DataStructInterface {
    public String Dump(); // 序列化
    public boolean Restore(String json); // 实例化
}
