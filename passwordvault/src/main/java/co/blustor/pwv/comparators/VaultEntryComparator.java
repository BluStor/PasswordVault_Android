package co.blustor.pwv.comparators;

import android.support.annotation.NonNull;

import java.util.Comparator;

import co.blustor.pwv.database.VaultEntry;

public class VaultEntryComparator implements Comparator<VaultEntry> {
    @Override
    public int compare(@NonNull VaultEntry o1, @NonNull VaultEntry o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}
