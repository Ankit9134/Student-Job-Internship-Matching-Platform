package com.example.jobmatch.entity;

public class Enums {

    public enum WorkAuthStatus {
        CITIZEN, PERM_RESIDENT, F1_OPT, H1B_NEEDED, OTHER
    }

    public enum WorkMode {
        REMOTE, ONSITE, HYBRID, ANY
    }

    public enum RoleType {
        INTERNSHIP, FULL_TIME
    }

    public enum ApplicationStatus {
        APPLIED, INTERVIEWING, OFFER, REJECTED
    }
}
