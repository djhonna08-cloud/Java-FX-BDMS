package com.example;

public class MethodTest {
    public void publicMethod() {
        System.out.println("public");
    }
    
    private String privateMethod(int param) {
        return "private";
    }
    
    protected static List<String> staticMethod() {
        return new ArrayList<>();
    }
    
    public <T> T genericMethod(T input) {
        return input;
    }
}
