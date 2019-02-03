package com.cjburkey.conquerer.util.property;

/**
 * Created by CJ Burkey on 2019/02/03
 */
public class BoolProperty extends Property<Boolean> {

    public BoolProperty() {
        super(false);
    }

    public BoolProperty(boolean value) {
        super(value);
    }

    /**
     * @deprecated Use {@link #getb() getb()} instead
     */
    @Override
    @Deprecated
    public Boolean get() {
        return getb();
    }

    public boolean getb() {
        return super.get();
    }

    public void set(boolean newValue) {
        super.set(newValue);
    }

    public void toggle() {
        set(!getb());
    }

}
