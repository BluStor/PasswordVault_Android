package co.blustor.pwv.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import co.blustor.pwv.R;

@SuppressLint("Registered")
public class MyApplication extends Application {
    @NonNull
    private static final SparseIntArray sIcons;

    static {
        sIcons = new SparseIntArray();
        sIcons.append(0, R.drawable.ic_00);
        sIcons.append(1, R.drawable.ic_01);
        sIcons.append(2, R.drawable.ic_02);
        sIcons.append(3, R.drawable.ic_03);
        sIcons.append(4, R.drawable.ic_04);
        sIcons.append(5, R.drawable.ic_05);
        sIcons.append(6, R.drawable.ic_06);
        sIcons.append(7, R.drawable.ic_07);
        sIcons.append(8, R.drawable.ic_08);
        sIcons.append(9, R.drawable.ic_09);
        sIcons.append(10, R.drawable.ic_10);
        sIcons.append(11, R.drawable.ic_11);
        sIcons.append(12, R.drawable.ic_12);
        sIcons.append(13, R.drawable.ic_13);
        sIcons.append(14, R.drawable.ic_14);
        sIcons.append(15, R.drawable.ic_15);
        sIcons.append(16, R.drawable.ic_16);
        sIcons.append(17, R.drawable.ic_17);
        sIcons.append(18, R.drawable.ic_18);
        sIcons.append(19, R.drawable.ic_19);
        sIcons.append(20, R.drawable.ic_20);
        sIcons.append(21, R.drawable.ic_21);
        sIcons.append(22, R.drawable.ic_22);
        sIcons.append(23, R.drawable.ic_23);
        sIcons.append(24, R.drawable.ic_24);
        sIcons.append(25, R.drawable.ic_25);
        sIcons.append(26, R.drawable.ic_26);
        sIcons.append(27, R.drawable.ic_27);
        sIcons.append(28, R.drawable.ic_28);
        sIcons.append(29, R.drawable.ic_29);
        sIcons.append(30, R.drawable.ic_30);
        sIcons.append(31, R.drawable.ic_31);
        sIcons.append(32, R.drawable.ic_32);
        sIcons.append(33, R.drawable.ic_33);
        sIcons.append(34, R.drawable.ic_34);
        sIcons.append(35, R.drawable.ic_35);
        sIcons.append(36, R.drawable.ic_36);
        sIcons.append(37, R.drawable.ic_37);
        sIcons.append(38, R.drawable.ic_38);
        sIcons.append(39, R.drawable.ic_39);
        sIcons.append(40, R.drawable.ic_40);
        sIcons.append(41, R.drawable.ic_41);
        sIcons.append(42, R.drawable.ic_42);
        sIcons.append(43, R.drawable.ic_43);
        sIcons.append(44, R.drawable.ic_44);
        sIcons.append(45, R.drawable.ic_45);
        sIcons.append(46, R.drawable.ic_46);
        sIcons.append(47, R.drawable.ic_47);
        sIcons.append(48, R.drawable.ic_48);
        sIcons.append(49, R.drawable.ic_49);
        sIcons.append(50, R.drawable.ic_50);
        sIcons.append(51, R.drawable.ic_51);
        sIcons.append(52, R.drawable.ic_52);
        sIcons.append(53, R.drawable.ic_53);
        sIcons.append(54, R.drawable.ic_54);
        sIcons.append(55, R.drawable.ic_55);
        sIcons.append(56, R.drawable.ic_56);
        sIcons.append(57, R.drawable.ic_57);
        sIcons.append(58, R.drawable.ic_58);
        sIcons.append(59, R.drawable.ic_59);
        sIcons.append(60, R.drawable.ic_60);
        sIcons.append(61, R.drawable.ic_61);
        sIcons.append(62, R.drawable.ic_62);
        sIcons.append(63, R.drawable.ic_63);
        sIcons.append(64, R.drawable.ic_64);
        sIcons.append(65, R.drawable.ic_65);
        sIcons.append(66, R.drawable.ic_66);
        sIcons.append(67, R.drawable.ic_67);
        sIcons.append(68, R.drawable.ic_68);
    }

    @NonNull
    public static SparseIntArray getIcons() {
        return sIcons;
    }
}
