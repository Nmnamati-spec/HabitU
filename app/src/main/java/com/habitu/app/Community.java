package com.habitu.app;

import java.util.List;

public class Community {
    private String communityId;
    private String name;
    private String icon;
    private String adminId;
    private boolean isLocked;
    private boolean isVisible;
    private List<String> memberIds;
    private List<String> pendingRequestIds;

    public Community() {}

    public String getCommunityId() { return communityId; }
    public void setCommunityId(String communityId) { this.communityId = communityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }
    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
    public List<String> getPendingRequestIds() { return pendingRequestIds; }
    public void setPendingRequestIds(List<String> pendingRequestIds) { this.pendingRequestIds = pendingRequestIds; }
}