package com.meidusa.venus.validate.validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Complicated {
    private Date date;
    private String string;
    private Map<String, String> map;
    private List<Integer> list;

    public static Complicated getDefault() {
        Complicated obj = new Complicated();
        obj.setDate(new Date(System.currentTimeMillis()));
        obj.setString("abcde");
        Map<String, String> defaultMap = new HashMap<String, String>();
        defaultMap.put("a", "A");
        defaultMap.put("b", "B");
        defaultMap.put("c", "C");
        obj.setMap(defaultMap);
        List<Integer> defaultList = new ArrayList<Integer>();
        defaultList.add(1);
        defaultList.add(2);
        defaultList.add(3);
        obj.setList(defaultList);
        return obj;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "CompoundObject [date=" + date + ", string=" + string + ", map=" + map + ", list=" + list + "]";
    }

}
