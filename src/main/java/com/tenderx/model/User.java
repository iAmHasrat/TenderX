package com.tenderx.model;

public class User {
    private String id;
    private String name;
    private String role; // "ISSUER", "BIDDER", "AUDITOR"
    private String organization;

    public User(String id, String name, String role, String organization) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.organization = organization;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getOrganization() { return organization; }

    @Override
    public String toString() {
        return name + " (" + role + " - " + organization + ")";
    }
}

