package com.mfk.lockfree.benchmark;

public class Data {
    private final int data;

    public Data(int data) {
        this.data = data;
    }

    public int getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return this.data == data.data;
    }

    @Override
    public int hashCode() {
        return data;
    }
}
