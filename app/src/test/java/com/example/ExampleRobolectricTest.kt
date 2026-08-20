package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.animekai.adblock.AdBlocker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AnimeKai", appName)
  }

  @Test
  fun `adblocker identifies ad urls`() {
    assertTrue(AdBlocker.isAd("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
    assertTrue(AdBlocker.isAd("https://popads.net/serve.js"))
    assertTrue(AdBlocker.isAd("https://propellerclick.com/clck.php?id=123"))
    assertTrue(AdBlocker.isAd("https://bet365.com/landing"))
    assertTrue(AdBlocker.isAd("https://adsterra.com/direct_link.php"))
    assertFalse(AdBlocker.isAd("https://animekai.be/home"))
    assertFalse(AdBlocker.isAd("https://animekai.be/watch/attack-on-titan"))
  }

  @Test
  fun `adblocker detects suspicious schemes`() {
    assertTrue(AdBlocker.isSuspiciousScheme("market://details?id=com.fake.adapp"))
    assertTrue(AdBlocker.isSuspiciousScheme("intent://scan/#Intent;scheme=zxing;package=com.ad;end"))
    assertTrue(AdBlocker.isSuspiciousScheme("tg://resolve?domain=spampromo"))
    assertFalse(AdBlocker.isSuspiciousScheme("https://animekai.be/"))
  }

  @Test
  fun `adblocker blocks rogue redirects from animekai to third party ad pages`() {
    // Legitimate navigation within AnimeKai
    assertFalse(AdBlocker.shouldBlockNavigation("https://animekai.be/watch/naruto", "https://animekai.be/home"))
    assertFalse(AdBlocker.shouldBlockNavigation("https://megacloud.tv/embed-2/v=123", "https://animekai.be/watch/naruto"))

    // Rogue click-anywhere redirect to unknown third party
    assertTrue(AdBlocker.shouldBlockNavigation("https://random-dating-trap.com/landing", "https://animekai.be/home"))
    assertTrue(AdBlocker.shouldBlockNavigation("https://bet365.com/register", "https://animekai.be/watch/one-piece"))
    assertTrue(AdBlocker.shouldBlockNavigation("market://details?id=spam", "https://animekai.be/home"))
  }

  @Test
  fun `whitelist overrides ad blocking`() {
    val adUrl = "https://custom-ads.net/banner.js"
    AdBlocker.addWhitelist("custom-ads.net")
    assertTrue(AdBlocker.isWhitelisted(adUrl))
    assertFalse(AdBlocker.isAd(adUrl))
    AdBlocker.removeWhitelist("custom-ads.net")
  }
}
