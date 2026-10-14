package vn.garden.core;

public interface SaveStore {
    String read(String key);
    void write(String key, String value);
}
