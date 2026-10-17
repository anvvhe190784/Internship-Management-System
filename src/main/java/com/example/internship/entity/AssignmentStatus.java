package com.example.internship.entity;

public enum AssignmentStatus {
  PENDING,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  public boolean canTransitionTo(AssignmentStatus target) {
    if (target == null || this == target) {
      return false;
    }
    return switch (this) {
      case PENDING -> target == IN_PROGRESS || target == CANCELLED;
      case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };
  }
}
