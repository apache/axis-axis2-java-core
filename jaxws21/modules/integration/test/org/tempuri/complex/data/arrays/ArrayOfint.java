package org.tempuri.complex.data.arrays;


public class ArrayOfint {

    protected Integer[] _int;

    public Integer[] getInt() {
        if (_int == null) {
            _int = new Integer[0];
        }
        return this._int;
    }

}
