package co.blustor.pwv.database;

import android.support.annotation.NonNull;

import java.util.UUID;

public class VaultEntry {

    @NonNull
    private final UUID mGroupUUID;
    @NonNull
    private final UUID mUUID;
    @NonNull
    private String mTitle = "";
    @NonNull
    private String mUsername = "";
    @NonNull
    private String mPassword = "";
    @NonNull
    private String mUrl = "";
    @NonNull
    private String mNotes = "";
    @NonNull
    private Integer mIconId = 0;

    public VaultEntry(@NonNull UUID groupUUID, @NonNull UUID uuid, @NonNull String title, @NonNull String username, @NonNull String password) {
        mGroupUUID = groupUUID;
        mUUID = uuid;
        mTitle = title;
        mUsername = username;
        mPassword = password;
    }

    @NonNull
    public UUID getGroupUUID() {
        return mGroupUUID;
    }

    @NonNull
    public UUID getUUID() {
        return mUUID;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(@NonNull String title) {
        mTitle = title;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }

    public void setUsername(@NonNull String username) {
        mUsername = username;
    }

    @NonNull
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(@NonNull String password) {
        mPassword = password;
    }

    @NonNull
    public String getUrl() {
        return mUrl;
    }

    public void setUrl(@NonNull String url) {
        mUrl = url;
    }

    @NonNull
    public String getNotes() {
        return mNotes;
    }

    public void setNotes(@NonNull String notes) {
        mNotes = notes;
    }

    @NonNull
    public Integer getIconId() {
        return mIconId;
    }

    public void setIconId(@NonNull Integer iconId) {
        mIconId = iconId;
    }
}
