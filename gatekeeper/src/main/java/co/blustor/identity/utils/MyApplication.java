package co.blustor.identity.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.SparseIntArray;

import co.blustor.identity.R;

@SuppressLint("Registered")
public class MyApplication extends Application {

    private static final SparseIntArray ICONS;

    static {
        ICONS = new SparseIntArray();
        ICONS.append(0, R.drawable.ic_00);
        ICONS.append(1, R.drawable.ic_01);
        ICONS.append(2, R.drawable.ic_02);
        ICONS.append(3, R.drawable.ic_03);
        ICONS.append(4, R.drawable.ic_04);
        ICONS.append(5, R.drawable.ic_05);
        ICONS.append(6, R.drawable.ic_06);
        ICONS.append(7, R.drawable.ic_07);
        ICONS.append(8, R.drawable.ic_08);
        ICONS.append(9, R.drawable.ic_09);
        ICONS.append(10, R.drawable.ic_10);
        ICONS.append(11, R.drawable.ic_11);
        ICONS.append(12, R.drawable.ic_12);
        ICONS.append(13, R.drawable.ic_13);
        ICONS.append(14, R.drawable.ic_14);
        ICONS.append(15, R.drawable.ic_15);
        ICONS.append(16, R.drawable.ic_16);
        ICONS.append(17, R.drawable.ic_17);
        ICONS.append(18, R.drawable.ic_18);
        ICONS.append(19, R.drawable.ic_19);
        ICONS.append(20, R.drawable.ic_20);
        ICONS.append(21, R.drawable.ic_21);
        ICONS.append(22, R.drawable.ic_22);
        ICONS.append(23, R.drawable.ic_23);
        ICONS.append(24, R.drawable.ic_24);
        ICONS.append(25, R.drawable.ic_25);
        ICONS.append(26, R.drawable.ic_26);
        ICONS.append(27, R.drawable.ic_27);
        ICONS.append(28, R.drawable.ic_28);
        ICONS.append(29, R.drawable.ic_29);
        ICONS.append(30, R.drawable.ic_30);
        ICONS.append(31, R.drawable.ic_31);
        ICONS.append(32, R.drawable.ic_32);
        ICONS.append(33, R.drawable.ic_33);
        ICONS.append(34, R.drawable.ic_34);
        ICONS.append(35, R.drawable.ic_35);
        ICONS.append(36, R.drawable.ic_36);
        ICONS.append(37, R.drawable.ic_37);
        ICONS.append(38, R.drawable.ic_38);
        ICONS.append(39, R.drawable.ic_39);
        ICONS.append(40, R.drawable.ic_40);
        ICONS.append(41, R.drawable.ic_41);
        ICONS.append(42, R.drawable.ic_42);
        ICONS.append(43, R.drawable.ic_43);
        ICONS.append(44, R.drawable.ic_44);
        ICONS.append(45, R.drawable.ic_45);
        ICONS.append(46, R.drawable.ic_46);
        ICONS.append(47, R.drawable.ic_47);
        ICONS.append(48, R.drawable.ic_48);
        ICONS.append(49, R.drawable.ic_49);
        ICONS.append(50, R.drawable.ic_50);
        ICONS.append(51, R.drawable.ic_51);
        ICONS.append(52, R.drawable.ic_52);
        ICONS.append(53, R.drawable.ic_53);
        ICONS.append(54, R.drawable.ic_54);
        ICONS.append(55, R.drawable.ic_55);
        ICONS.append(56, R.drawable.ic_56);
        ICONS.append(57, R.drawable.ic_57);
        ICONS.append(58, R.drawable.ic_58);
        ICONS.append(59, R.drawable.ic_59);
        ICONS.append(60, R.drawable.ic_60);
        ICONS.append(61, R.drawable.ic_61);
        ICONS.append(62, R.drawable.ic_62);
        ICONS.append(63, R.drawable.ic_63);
        ICONS.append(64, R.drawable.ic_64);
        ICONS.append(65, R.drawable.ic_65);
        ICONS.append(66, R.drawable.ic_66);
        ICONS.append(67, R.drawable.ic_67);
        ICONS.append(68, R.drawable.ic_68);
    }

    public static SparseIntArray getIcons() {
        return ICONS;
    }
}
