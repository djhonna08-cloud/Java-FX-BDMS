package com.example;

/**
 * Model class for SMS templates
 */
public class SMSTemplate {
    private final int id;
    private final String name;
    private String template;
    private final String description;
    private final String category;
    
    public SMSTemplate(int id, String name, String template, String description, String category) {
        this.id = id;
        this.name = name;
        this.template = template;
        this.description = description;
        this.category = category;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}
