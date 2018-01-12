package co.blustor.identity.utils

import android.app.Application
import android.util.Log
import android.util.SparseIntArray

import co.blustor.identity.R
import com.zwsb.palmsdk.PalmSDK

class MyApplication : Application() {
    companion object {
        val tag = "MyApplication"
        val icons = SparseIntArray()

        init {
            icons.append(0, R.drawable.ic_00)
            icons.append(1, R.drawable.ic_01)
            icons.append(2, R.drawable.ic_02)
            icons.append(3, R.drawable.ic_03)
            icons.append(4, R.drawable.ic_04)
            icons.append(5, R.drawable.ic_05)
            icons.append(6, R.drawable.ic_06)
            icons.append(7, R.drawable.ic_07)
            icons.append(8, R.drawable.ic_08)
            icons.append(9, R.drawable.ic_09)
            icons.append(10, R.drawable.ic_10)
            icons.append(11, R.drawable.ic_11)
            icons.append(12, R.drawable.ic_12)
            icons.append(13, R.drawable.ic_13)
            icons.append(14, R.drawable.ic_14)
            icons.append(15, R.drawable.ic_15)
            icons.append(16, R.drawable.ic_16)
            icons.append(17, R.drawable.ic_17)
            icons.append(18, R.drawable.ic_18)
            icons.append(19, R.drawable.ic_19)
            icons.append(20, R.drawable.ic_20)
            icons.append(21, R.drawable.ic_21)
            icons.append(22, R.drawable.ic_22)
            icons.append(23, R.drawable.ic_23)
            icons.append(24, R.drawable.ic_24)
            icons.append(25, R.drawable.ic_25)
            icons.append(26, R.drawable.ic_26)
            icons.append(27, R.drawable.ic_27)
            icons.append(28, R.drawable.ic_28)
            icons.append(29, R.drawable.ic_29)
            icons.append(30, R.drawable.ic_30)
            icons.append(31, R.drawable.ic_31)
            icons.append(32, R.drawable.ic_32)
            icons.append(33, R.drawable.ic_33)
            icons.append(34, R.drawable.ic_34)
            icons.append(35, R.drawable.ic_35)
            icons.append(36, R.drawable.ic_36)
            icons.append(37, R.drawable.ic_37)
            icons.append(38, R.drawable.ic_38)
            icons.append(39, R.drawable.ic_39)
            icons.append(40, R.drawable.ic_40)
            icons.append(41, R.drawable.ic_41)
            icons.append(42, R.drawable.ic_42)
            icons.append(43, R.drawable.ic_43)
            icons.append(44, R.drawable.ic_44)
            icons.append(45, R.drawable.ic_45)
            icons.append(46, R.drawable.ic_46)
            icons.append(47, R.drawable.ic_47)
            icons.append(48, R.drawable.ic_48)
            icons.append(49, R.drawable.ic_49)
            icons.append(50, R.drawable.ic_50)
            icons.append(51, R.drawable.ic_51)
            icons.append(52, R.drawable.ic_52)
            icons.append(53, R.drawable.ic_53)
            icons.append(54, R.drawable.ic_54)
            icons.append(55, R.drawable.ic_55)
            icons.append(56, R.drawable.ic_56)
            icons.append(57, R.drawable.ic_57)
            icons.append(58, R.drawable.ic_58)
            icons.append(59, R.drawable.ic_59)
            icons.append(60, R.drawable.ic_60)
            icons.append(61, R.drawable.ic_61)
            icons.append(62, R.drawable.ic_62)
            icons.append(63, R.drawable.ic_63)
            icons.append(64, R.drawable.ic_64)
            icons.append(65, R.drawable.ic_65)
            icons.append(66, R.drawable.ic_66)
            icons.append(67, R.drawable.ic_67)
            icons.append(68, R.drawable.ic_68)
        }
    }

    override fun onCreate() {
        super.onCreate()

        PalmSDK.init(this, "sdk@blustor.com", object : PalmSDK.InitSDKCallback {
            override fun onSuccess() {
                Log.d(tag, "PalmSDK init success.")
            }

            override fun onError() {
                Log.d(tag, "PalmSDK init error.")
            }
        })
    }
}
