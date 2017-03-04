package co.blustor.passwordvault.comparators;

import java.util.Comparator;

import co.blustor.passwordvault.database.VaultEntry;

public class VaultEntryComparator implements Comparator<VaultEntry> {
    @Override
    public int compare(VaultEntry o1, VaultEntry o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}
