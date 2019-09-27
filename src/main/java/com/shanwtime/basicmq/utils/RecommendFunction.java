package com.shanwtime.basicmq.utils;

/**
 * Created by tlbgqq on 2017/11/17.
 */
@FunctionalInterface
public interface RecommendFunction<T, E, R> {

    R apply(T arg1, E arg2);
}
