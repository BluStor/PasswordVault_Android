package co.blustor.pwv.comparators;

import android.support.annotation.NonNull;

import java.util.Comparator;

import co.blustor.pwv.database.VaultGroup;

public class VaultGroupComparator implements Comparator<VaultGroup> {
    @Override
    public int compare(@NonNull VaultGroup o1, @NonNull VaultGroup o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
