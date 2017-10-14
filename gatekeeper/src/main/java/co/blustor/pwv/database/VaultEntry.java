package co.blustor.pwv.database;

import java.util.UUID;

public class VaultEntry {

    private final UUID mGroupUUID;
    private final UUID mUUID;
    private String mTitle = "";
    private String mUsername = "";
    private String mPassword = "";
    private String mUrl = "";
    private String mNotes = "";
    private Integer mIconId = 0;

    public VaultEntry(UUID groupUUID, UUID uuid, String title, String username, String password) {
        mGroupUUID = groupUUID;
        mUUID = uuid;
        mTitle = title;
        mUsername = username;
        mPassword = password;
    }

    public UUID getGroupUUID() {
        return mGroupUUID;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public Integer getIconId() {
        return mIconId;
    }

    public void setIconId(Integer iconId) {
        mIconId = iconId;
    }
}
