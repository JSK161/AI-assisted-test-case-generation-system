package com.nun.aitestcase.vo;

public class ProjectStatisticsVO {

    private int totalCount;
    private int adoptedCount;
    private int needsRevisionCount;
    private int rejectedCount;
    private int pendingCount;
    private double adoptionRate;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getAdoptedCount() {
        return adoptedCount;
    }

    public void setAdoptedCount(int adoptedCount) {
        this.adoptedCount = adoptedCount;
    }

    public int getNeedsRevisionCount() {
        return needsRevisionCount;
    }

    public void setNeedsRevisionCount(int needsRevisionCount) {
        this.needsRevisionCount = needsRevisionCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public double getAdoptionRate() {
        return adoptionRate;
    }

    public void setAdoptionRate(double adoptionRate) {
        this.adoptionRate = adoptionRate;
    }
}
