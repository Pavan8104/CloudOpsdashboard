package com.cloudops.dashboard.model;

/**
 * User roles for CloudOps Dashboard
 *
 * Teen roles hain system mein - RBAC implement karne ke liye yahi use hote hain.
 * Har role ke alag-alag permissions hain - frontend aur backend dono jagah check hota hai.
 *
 * ADMIN  - Sab kuch kar sakta hai, incident create/delete, user management bhi
 * ENGINEER - Incidents dekh aur update kar sakta hai, read/write access services pe
 * VIEWER - Sirf dashboard dekh sakta hai, kuch change nahi kar sakta (stakeholders ke liye)
 */
public enum Role {
    // Full access - system administrators ke liye, bahut kum logon ko dena chahiye
    ROLE_ADMIN,

    // Operational access - on-call engineers, SRE team ke liye
    ROLE_ENGINEER,

    // Read-only access - managers, product team, ya koi bhi jo sirf status dekhna chahta ho
    ROLE_VIEWER
}
