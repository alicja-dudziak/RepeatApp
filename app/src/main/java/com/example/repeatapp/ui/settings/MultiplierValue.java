package com.example.repeatapp.ui.settings;

public class MultiplierValue {


    static double GetValue(double value) {
        return (value / 10) + 1;
    }

    static int GetMultiplier(double value) {
        return (int) Math.round((value - 1) * 10);
    }

    public static double GetMultiplierRaisedValue(double value) {
        return value * 2;
    }
}
