package nhdphuong.com.manga

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.By.pkg
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until.hasObject
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class HomeTest {
    private lateinit var device: UiDevice

    private val packageName: String = BuildConfig.APPLICATION_ID

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        device.pressHome()

        device.launcherPackageName.let {
            assertNotNull(it)
            device.wait(hasObject(pkg(it).depth(0)), LAUNCH_TIME_OUT)
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        context.packageManager
            .getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }?.let(context::startActivity)

        device.wait(hasObject(pkg(packageName)), LAUNCH_TIME_OUT)
    }

    @Test
    fun startHome() {
        val searchEditor = device.findObjectById("edtSearch")
        val searchButton = device.findObjectById("ibSearch")
        val searchResultTitle = device.findObjectById("mtv_search_result")

        searchEditor.waitForExists(UI_ELEMENT_TIME_OUT)

        searchEditor.clearTextField()
        searchEditor.text = "alp"

        searchButton.click()

        searchResultTitle.waitForExists(1000L)

        assertTrue(searchResultTitle.exists())
    }

    companion object {
        private const val LAUNCH_TIME_OUT = 5000L

        private const val UI_ELEMENT_TIME_OUT = 5000L
    }
}
