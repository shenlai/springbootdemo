package com.sl.springlearning.aop;


/**
 * CGLIB 代理
 */
public class MathCaculator {


    public int div(int i, int j) {
        System.out.println("MathCalculator...div...");
        return i / j;
    }
}
