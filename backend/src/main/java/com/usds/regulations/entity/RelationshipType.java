package com.usds.regulations.entity;

public enum RelationshipType {
    REDUNDANT,           // Regulations that duplicate each other
    CONFLICTING,         // Regulations that contradict each other
    COMPLEMENTARY,       // Regulations that work together
    SUPERSEDING,         // One regulation replaces another
    REFERENCED,          // One regulation references another
    OVERLAPPING,         // Regulations with overlapping scope
    DEPENDENT            // One regulation depends on another
}