package co.blustor.passwordvault.comparators;

import java.util.Comparator;

import co.blustor.passwordvault.database.VaultGroup;

public class VaultGroupComparator implements Comparator<VaultGroup> {
    @Override
    public int compare(VaultGroup o1, VaultGroup o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
