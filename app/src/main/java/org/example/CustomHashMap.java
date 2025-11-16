package org.example;

import java.util.*;


class Node<K, V> implements Map.Entry<K, V> {
    int hash;
    K key;
    V value;
    Node<K, V> next;

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V newValue) {
        V oldValue = this.value;
        this.value = newValue;
        return oldValue;
    }

    Node(int hash, K key, V value) {
        this.hash = hash;
        this.key = key;
        this.value = value;
    }
}


public class CustomHashMap<K, V> {
    private int size;
    private int modCount;
    private final double loadFactor = 0.75;
    private Node<K, V>[] table;
    private int threshold;

    CustomHashMap() {
        size = 0;
        table = null;
        modCount = 0;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        int index;
        Node<K, V>[] tableRef;
        Node<K, V> next;
        Node<K, V> prev;
        Node<K, V> lastReturned;
        int lastReturnedIndex;
        int expectedModCount;

        private int scanForNext(int index) {
            for (int i = index; i < tableRef.length; i++) {
                if (tableRef[i] != null) {
                    return i;
                }
            }
            return tableRef.length;
        }

        public HashIterator() {
            this.expectedModCount = modCount;
            tableRef = table == null ? (Node<K, V>[]) new Node[0] : table;
            index = scanForNext(0);
            lastReturned = null;
            lastReturnedIndex = -1;
            prev = null;
            next = index == tableRef.length ? null : tableRef[index];
        }

        @Override
        public boolean hasNext() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            return next != null;
        }

        protected abstract E element(Node<K, V> node);

        @Override

        public E next() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (next == null) {
                throw new NoSuchElementException();
            }
            Node<K, V> returned = next;
            int returnedBucket = index;

            Node<K, V> computedNext;
            int computedBucketIndex;
            if (returned.next == null) {
                computedBucketIndex = scanForNext(returnedBucket + 1);
                computedNext = computedBucketIndex < tableRef.length ? tableRef[computedBucketIndex] : null;
            } else {
                computedNext = returned.next;
                computedBucketIndex = returnedBucket;
            }

            if (returned.next == null) {
                prev = null;
                next = computedNext;
                index = computedBucketIndex;
            } else {
                prev = returned;
                next = computedNext;

            }
            lastReturned = returned;
            lastReturnedIndex = returnedBucket;
            return element(returned);
        }

        @Override
        public void remove() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<K, V> target = lastReturned;
            int bucket = lastReturnedIndex;
            if (prev == null) {
                table[bucket] = target.next;
            } else {
                prev.next = target.next;
            }
            lastReturned = null;
            lastReturnedIndex = -1;
            modCount++;
            expectedModCount++;
            size--;
        }
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
            modCount++;
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
                modCount++;
                size++;
            }
        }
        if (size >= threshold) {
            modCount++;
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

    //    @Override
    public V remove(Object key) {
        if (table == null || table.length == 0) return null;

        int h = hash(key);
        int bucketIdx = h & (table.length - 1);

        Node<K, V> node = table[bucketIdx];
        Node<K, V> prev = null;

        while (node != null) {
            if (node.hash == h && Objects.equals(key, node.key)) {
                if (prev == null) {
                    table[bucketIdx] = node.next;
                } else {
                    prev.next = node.next;
                }
                modCount++;
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }
        return null;
    }

    //    @Override
    public boolean containsKey(Object key) {
        return get(key) != null || (get(key) == null && containsKeyWithNullValue(key));
    }

    private boolean containsKeyWithNullValue(Object key) {
        if (table == null || table.length == 0) return false;
        int h = hash(key);
        int bucketIdx = h & (table.length - 1);
        Node<K, V> node = table[bucketIdx];
        while (node != null) {
            if (node.hash == h && Objects.equals(key, node.key)) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    private class KeyIterator extends HashIterator<K> {
        @Override
        protected K element(Node<K, V> node) {
            return node.key;
        }
    }

    private class ValueIterator extends HashIterator<V> {
        @Override
        protected V element(Node<K, V> node) {
            return node.value;
        }
    }

    private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
        @Override
        protected Map.Entry<K, V> element(Node<K, V> node) {
            return node;
        }
    }

}
