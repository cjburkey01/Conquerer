package com.cjburkey.conquerer.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;

/**
 * Created by CJ Burkey on 2019/01/18
 */
@SuppressWarnings("unused")
public interface IAppender<T> {
    
    void put(T data);
    int getPos();
    
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
        
        public int getPos() {
            return buffer.position();
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
        
        public int getPos() {
            return buffer.position();
        }
        
    }
    
    class FloatCollectionAppender implements IAppender<Float> {
        
        private final Collection<Float> collection;
        
        public FloatCollectionAppender(Collection<Float> collection) {
            this.collection = collection;
        }
        
        public void put(Float data) {
            collection.add(data);
        }
        
        public void put(float data) {
            put(Float.valueOf(data));
        }
        
        public int getPos() {
            return collection.size();
        }
        
    }
    
    class ShortCollectionAppender implements IAppender<Short> {
        
        private final Collection<Short> collection;
        
        public ShortCollectionAppender(Collection<Short> collection) {
            this.collection = collection;
        }
        
        public void put(Short data) {
            collection.add(data);
        }
        
        public void put(short data) {
            put(Short.valueOf(data));
        }
        
        public int getPos() {
            return collection.size();
        }
        
    }
    
}
