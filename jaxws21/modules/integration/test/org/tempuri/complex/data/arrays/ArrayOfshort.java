package org.tempuri.complex.data.arrays;

public class ArrayOfshort {

    protected Short[] _short;

    public Short[] get_short() {
        if (_short == null) {
            _short = new Short[0];
        }
        return this._short;
    }

    public void set_short(Short[] _short) {
        this._short = _short;
    }
}
