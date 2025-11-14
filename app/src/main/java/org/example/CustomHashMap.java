package org.example;

import java.util.Map;
import java.util.Objects;

class Node<K, V> {
    int hash;
    K key;
    V value;
    Node<K, V> next;

    Node(int hash, K key, V value) {
        this.hash = hash;
        this.key = key;
        this.value = value;
    }
}

public class CustomHashMap<K, V> implements Map<K, V> {
    private int size;

    private final double loadFactor = 0.75;
    private Node<K, V>[] table;
    private int threshold;

    CustomHashMap() {
        size = 0;
        table = null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private int hash(Object key) {
        if (key == null)
            return 0;
        int h = key.hashCode();
        return (h ^ (h >> 16));
    }

    //    @Override
    public V put(K key, V value) {
        int hash = hash(key);

        if (table == null || table.length == 0) {
            int DEFAULT_SIZE = 16;
            table = (Node<K, V>[]) new Node[DEFAULT_SIZE];
            threshold = (int) (DEFAULT_SIZE * loadFactor);
        }
        int bucketIdx = hash & (table.length - 1);

        if (table[bucketIdx] == null) {
            table[bucketIdx] = new Node<K, V>(hash, key, value);
            size++;
            return null;
        } else {
            Node<K, V> node = table[bucketIdx];
            Node<K, V> prev = null;
            while (node != null) {
                if (node.hash == hash && Objects.equals(key, node.key)) {
                    node.value = value;
                    return node.value;

                }
                prev = node;
                node = node.next;
            }
            if (prev != null) {
                prev.next = new Node<K, V>(hash, key, value);
                size++;
            }
        }
        if (size >= threshold) {
            resize();
        }
        return null;
    }

    private int newTableSize(int size) {
        return size << 1;
    }

    private void resize() {
        int oldCap = table.length;
        int newCap = newTableSize(oldCap);
        Node<K, V>[] oldTable = table;
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCap];
        threshold = (int) (newCap * loadFactor);
        for (int i = 0; i < oldCap; i++) {
            if (oldTable[i] == null) continue;
            Node<K, V> node = oldTable[i];
            if (node.next == null) {
                newTable[node.hash & (newCap - 1)] = node;
                continue;
            }
            Node<K, V> loHead = null, hiHead = null, loTail = null, hiTail = null;
            while (node != null) {
                Node<K, V> next = node.next;
                if ((node.hash & oldCap) == 0) {
                    if (loTail == null) {
                        loHead = node;

                    } else {
                        loTail.next = node;
                    }
                    loTail = node;
                } else {
                    if (hiTail == null) {
                        hiHead = node;

                    } else {
                        hiTail.next = node;
                    }
                    hiTail = node;
                }

                node = next;
            }
            if (loHead != null) {
                loTail.next = null;
            }
            if (hiTail != null) {
                hiTail.next = null;
            }
            newTable[i] = loHead;
            newTable[i + oldCap] = hiHead;
            oldTable[i] = null;

        }
        table = newTable;


    }

    //    @Override
    public V get(Object key) {
        if (table == null || table.length == 0) return null;
        int h = hash(key);
        int bucketIdx = h & (table.length - 1);
        if (table[bucketIdx] == null) {
            return null;
        } else {
            Node<K, V> node = table[bucketIdx];
            while (node != null) {
                if (node.hash == h && Objects.equals(key, node.key)) {
                    return node.value;
                }
                node = node.next;
            }
        }
        return null;
    }
}
