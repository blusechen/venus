package com.meidusa.venus.io.asm.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Hello {
    private String name;
    private String greeting;
    private int age;
    private float ppp;
    private Double cost;
    private Map<String, Long> map;
    private List<String> list;
    private Date today;
    private int[] numbers;
    private Map<String, Inner> innerMap;
    private Inner inner;

    public Map<String, Inner> getInnerMap() {
        return innerMap;
    }

    public void setInnerMap(Map<String, Inner> innerMap) {
        this.innerMap = innerMap;
    }

    public Inner getInner() {
        return inner;
    }

    public void setInner(Inner inner) {
        this.inner = inner;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public float getPpp() {
        return ppp;
    }

    public void setPpp(float ppp) {
        this.ppp = ppp;
    }

    public Map<String, Long> getMap() {
        return map;
    }

    public void setMap(Map<String, Long> map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public String toString() {
        return "Hello [name=" + name + ", greeting=" + greeting + ", age=" + age + ", cost=" + cost + ", map=" + map + "]";
    }

}
