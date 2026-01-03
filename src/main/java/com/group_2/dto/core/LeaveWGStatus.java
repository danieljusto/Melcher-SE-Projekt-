package com.group_2.dto.core;

// Result of checking if a user can leave their WG
public record LeaveWGStatus(boolean canLeave, double balance, String message) {
}
