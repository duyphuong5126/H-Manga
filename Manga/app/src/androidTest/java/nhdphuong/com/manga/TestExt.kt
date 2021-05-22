package nhdphuong.com.manga

import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector

fun uiSelectorById(id: String): UiSelector {
    return UiSelector().resourceId("${BuildConfig.APPLICATION_ID}:id/$id")
}

fun UiDevice.findObjectById(id: String): UiObject {
    return findObject(uiSelectorById(id))
}