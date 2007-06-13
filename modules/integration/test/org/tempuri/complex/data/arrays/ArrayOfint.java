package org.tempuri.complex.data.arrays;


public class ArrayOfint {

    protected Integer[] _int;

    public Integer[] get_int() {
        if (_int == null) {
            _int = new Integer[0];
        }
        return this._int;
    }

    public void set_int(Integer[] _int) {
        this._int = _int;
    }
}
