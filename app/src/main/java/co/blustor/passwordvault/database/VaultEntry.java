package co.blustor.passwordvault.database;

import java.util.UUID;

public class VaultEntry {
    private static final String TAG = "VaultEntry";

    private UUID mUUID;
    private String mTitle;
    private String mUsername;
    private String mPassword;
    private String mUri = "";

    public VaultEntry(UUID uuid, String title, String username, String password) {
        mUUID = uuid;
        mTitle = title;
        mUsername = username;
        mPassword = password;
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

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }
}
