package com.wilddog.toolbar.boardtoolbar;

import java.util.HashMap;

/**
 * Created by he on 2017/6/19.
 */

public class ParamHashMap extends HashMap<Integer,Integer> {

    public ParamHashMap add(Integer key, Integer value){
        put(key,value);
        return this;
    }


    public Integer getFloatingButtonIndex(int key){
        return get(key);
    }
}
