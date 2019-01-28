package com.cjburkey.conquerer.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Created by CJ Burkey on 2019/01/18
 */
@SuppressWarnings("unused")
public interface IAppender<T> {
    
    void put(T data);
    void put(int at, T data);
    int getPos();
    T at(int pos);
    
    class FloatBufferAppender implements IAppender<Float> {
        
        private final FloatBuffer buffer;
        
        public FloatBufferAppender(FloatBuffer buffer) {
            this.buffer = buffer;
        }
        
        public void put(Float data) {
            if (buffer.position() < buffer.limit()) buffer.put(data);
        }
        
        public void put(float data) {
            put(Float.valueOf(data));
        }
        
        public void put(int at, Float data) {
            if (buffer.position() < buffer.limit()) buffer.put(at, data);
        }
        
        public void put(int at, float data) {
            put(at, Float.valueOf(data));
        }
        
        public int getPos() {
            return buffer.position();
        }
        
        public Float at(int pos) {
            return buffer.get(pos);
        }
        
    }
    
    class ShortBufferAppender implements IAppender<Short> {
        
        private final ShortBuffer buffer;
        
        public ShortBufferAppender(ShortBuffer buffer) {
            this.buffer = buffer;
        }
        
        public void put(Short data) {
            if (buffer.position() < buffer.limit()) buffer.put(data);
        }
        
        public void put(short data) {
            put(Short.valueOf(data));
        }
        
        public void put(int at, Short data) {
            if (buffer.position() < buffer.limit()) buffer.put(at, data);
        }
        
        public void put(int at, short data) {
            put(at, Short.valueOf(data));
        }
        
        public int getPos() {
            return buffer.position();
        }
        
        public Short at(int pos) {
            return buffer.get(pos);
        }
        
    }
    
    class FloatCollectionAppender implements IAppender<Float> {
        
        private final List<Float> collection;
        
        public FloatCollectionAppender(List<Float> collection) {
            this.collection = collection;
        }
        
        public void put(Float data) {
            collection.add(data);
        }
        
        public void put(float data) {
            put(Float.valueOf(data));
        }
        
        public void put(int at, Float data) {
            collection.set(at, data);
        }
        
        public void put(int at, float data) {
            put(at, Float.valueOf(data));
        }
        
        public int getPos() {
            return collection.size();
        }
        
        public Float at(int pos) {
            return collection.get(pos);
        }
        
    }
    
    class ShortCollectionAppender implements IAppender<Short> {
        
        private final List<Short> collection;
        
        public ShortCollectionAppender(List<Short> collection) {
            this.collection = collection;
        }
        
        public void put(Short data) {
            collection.add(data);
        }
        
        public void put(short data) {
            put(Short.valueOf(data));
        }
        
        public void put(int at, Short data) {
            collection.set(at, data);
        }
        
        public void put(int at, short data) {
            put(at, Short.valueOf(data));
        }
        
        public int getPos() {
            return collection.size();
        }
        
        public Short at(int pos) {
            return collection.get(pos);
        }
        
    }
    
    @SuppressWarnings("WeakerAccess")
    class VoidAppender<T> implements IAppender<T> {
        
        private final T value;
        
        public VoidAppender(T value) {
            this.value = value;
        }
        
        public VoidAppender() {
            this(null);
        }
        
        public void put(T data) {
        }
        
        public void put(int at, T data) {
        }
        
        public int getPos() {
            return 0;
        }
        
        public T at(int pos) {
            return value;
        }
        
    }
    
}
