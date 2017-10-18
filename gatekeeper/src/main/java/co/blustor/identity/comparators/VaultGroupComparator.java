package co.blustor.identity.comparators;

import java.util.Comparator;

import co.blustor.identity.vault.VaultGroup;

public class VaultGroupComparator implements Comparator<VaultGroup> {
    @Override
    public int compare(VaultGroup o1, VaultGroup o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
