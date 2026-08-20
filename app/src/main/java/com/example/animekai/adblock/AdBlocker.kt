package com.example.animekai.adblock

import android.net.Uri
import android.util.Log

object AdBlocker {
    private const val TAG = "AdBlocker"

    // High-impact ad networks, popup/popunder networks, video ad injection servers,
    // trackers, clickjackers, casino redirectors, and malware domains.
    private val BLOCKED_DOMAINS = hashSetOf(
        // Major Ad & Banner Networks
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adservice.google.com",
        "pagead2.googlesyndication.com",
        "pagead2.google.com",
        "adsystem.com",
        "adnxs.com",
        "adnxs-simple.com",
        "rubiconproject.com",
        "pubmatic.com",
        "criteo.com",
        "criteo.net",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "moatads.com",
        "openx.net",
        "smartadserver.com",
        "appnexus.com",
        "sovrn.com",
        "yieldmo.com",
        "teads.tv",
        "teads.com",
        "triplelift.com",
        "gumgum.com",
        "kargo.com",
        "pulsepoint.com",
        "sharethrough.com",
        "nativo.com",
        "simpli.fi",
        "adroll.com",
        "adform.net",
        "bidvertiser.com",
        "infolinks.com",
        "adsupply.com",
        "realsrv.com",
        "tsyndicate.com",
        "histats.com",
        "statcounter.com",
        "yandex.ru/ads",
        "aniview.com",
        "anyclip.com",
        "connatix.com",
        "primis.tech",
        "vidazoo.com",
        "seedtag.com",
        "vi-serve.com",
        "showheroes.com",
        "undertone.com",
        "vdo.ai",
        "adxprts.com",
        "adxad.com",
        "adbull.me",

        // Streaming Video Pop-Up, Pop-Under, and Redirect Networks (Major Culprits)
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "propellerclick.com",
        "adsterra.com",
        "exoclick.com",
        "syndication.exoclick.com",
        "monetag.com",
        "hilltopads.com",
        "clickadu.com",
        "juicyads.com",
        "admaven.com",
        "adcash.com",
        "popunder.net",
        "traffichaus.com",
        "plugrush.com",
        "ero-advertising.com",
        "trafficstars.com",
        "trafficfactory.biz",
        "trafficjunky.com",
        "rtmark.net",
        "ad-score.com",
        "adkernel.com",
        "onclickads.net",
        "onclickalgo.com",
        "richpush.co",
        "notix.io",
        "adpushup.com",
        "pushwoosh.com",
        "onesignal.com",
        "mgid.com",
        "revcontent.com",
        "zergnet.com",
        "adblade.com",
        "adtrue.com",
        "adkeeper.co",
        "alwingulla.com",
        "highcpmrevenuenetwork.com",
        "deloplen.com",
        "coinhive.com",
        "jsecoin.com",
        "cryptoloot.pro",
        "webminepool.com",
        "onclickgate.com",
        "onclickperformance.com",
        "creativecdn.com",
        "creativecircuits.net",
        "trafficjunky.net",
        "wpushsdk.com",
        "propu.sh",
        "yadro.ru",
        "counter.yadro.ru",
        "toplist.cz",
        "liveinternet.ru",
        "hotlog.ru",

        // Betting, Casino, Viral, and Scam Redirect Networks
        "bet365.com",
        "1xbet.com",
        "parimatch.com",
        "stake.com",
        "mostbet.com",
        "melbet.com",
        "linebet.com",
        "pin-up.bet",
        "vulkan.bet",
        "aviator.game",
        "1win.pro",
        "bc.game",
        "roobet.com",
        "chaturbate.com",
        "bongacams.com",
        "livejasmin.com",
        "stripchat.com",
        "fanza.co.jp",
        "brazzers.com",
        "adultfriendfinder.com",
        "camsoda.com",
        "imlive.com",
        "flirt4free.com",

        // Fake Alert / APK / Cleaner Landing Pages
        "cleaner.com",
        "antivirus-update.com",
        "vpn-promo.com",
        "spin2win.com",
        "sweepstakes-winner.com",
        "reward-center.com",
        "install-update.net",
        "system-security-alert.com",

        // Mobile Ad Attribution & Tracking Redirectors
        "trackier.com",
        "voluum.com",
        "appsflyer.com",
        "adjust.com",
        "kochava.com",
        "branch.io",
        "singular.net",
        "affise.com",
        "hasoffers.com",
        "cellxpert.com",
        "trafficmanager.com"
    )

    // Suspicious sub-resource path & query keywords
    private val BLOCKED_KEYWORDS = listOf(
        "/ads.",
        "/ads/",
        "/ad-",
        "/ad_",
        "_ad_",
        "-ad-",
        "/ad.",
        "ad_banner",
        "/banners/",
        "/banner.",
        "/banner/",
        "adsystem",
        "adserver",
        "adservice",
        "adunit",
        "ad_unit",
        "ad_type",
        "ad_slot",
        "ad_tag",
        "ad_client",
        "popunder",
        "pop_under",
        "popup.js",
        "pop-up.",
        "pop.js",
        "syndication",
        "monetag",
        "propellerclick",
        "propellerads",
        "clickadu",
        "admaven",
        "hilltopads",
        "onclickgate",
        "clck.php",
        "direct_link.php",
        "track.php",
        "out.php",
        "click.php",
        "goto.php",
        "jump.php",
        "redirect.php",
        "vast.xml",
        "vpaid",
        "vast.js",
        "prebid.js",
        "pubads",
        "gpt.js",
        "anti-adblock",
        "adblock-detector",
        "fuckadblock",
        "blockadblock"
    )

    // User-controlled whitelist
    private val WHITELISTED_DOMAINS = hashSetOf<String>()

    fun isWhitelisted(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowercaseUrl = url.lowercase()
        try {
            val uri = Uri.parse(lowercaseUrl)
            val host = uri.host ?: lowercaseUrl
            for (w in WHITELISTED_DOMAINS) {
                if (host == w || host.endsWith(".$w") || host.contains(w)) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return false
    }

    fun addWhitelist(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
        if (clean.isNotBlank()) {
            WHITELISTED_DOMAINS.add(clean)
        }
    }

    fun removeWhitelist(domain: String) {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
        WHITELISTED_DOMAINS.remove(clean)
    }

    fun toggleWhitelist(domain: String): Boolean {
        val clean = domain.trim().lowercase().removePrefix("https://").removePrefix("http://").trimEnd('/')
        return if (WHITELISTED_DOMAINS.contains(clean)) {
            WHITELISTED_DOMAINS.remove(clean)
            false
        } else {
            WHITELISTED_DOMAINS.add(clean)
            true
        }
    }

    fun getWhitelistedDomains(): Set<String> {
        return WHITELISTED_DOMAINS.toSet()
    }

    /**
     * Checks if a destination host is an authorized AnimeKai domain or streaming video host.
     */
    fun isAuthorizedDomain(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        val lowerHost = host.lowercase()

        // 1. AnimeKai domains & subdomains
        if (lowerHost == "animekai.be" || lowerHost.endsWith(".animekai.be") ||
            lowerHost == "animekai.to" || lowerHost.endsWith(".animekai.to") ||
            lowerHost == "animekai.la" || lowerHost.endsWith(".animekai.la") ||
            lowerHost == "animekai.is" || lowerHost.endsWith(".animekai.is") ||
            lowerHost == "animekai.tv" || lowerHost.endsWith(".animekai.tv") ||
            lowerHost == "animekai.nz" || lowerHost.endsWith(".animekai.nz")) {
            return true
        }

        // 2. Genuine video streaming CDN hosts and player iframe domains
        val allowedVideoCdns = listOf(
            "megacloud.tv", "megacloud.club", "rapid-cloud.co", "rapidcloud.co",
            "vidstreaming.io", "vidcloud9.com", "filelions.to", "filelions.com",
            "streamwish.to", "streamwish.com", "mp4upload.com", "doodstream.com",
            "dood.to", "mixdrop.co", "mixdrop.to", "streamtape.com", "streamtape.net",
            "cloudflare.com", "googleapis.com", "gstatic.com", "jsdelivr.net",
            "cdnjs.cloudflare.com", "disqus.com"
        )
        for (allowed in allowedVideoCdns) {
            if (lowerHost == allowed || lowerHost.endsWith(".$allowed")) {
                return true
            }
        }

        // 3. User whitelisted domains
        for (w in WHITELISTED_DOMAINS) {
            if (lowerHost == w || lowerHost.endsWith(".$w")) {
                return true
            }
        }

        return false
    }

    /**
     * Determines whether a navigation request from a user click or page script should be permitted.
     * Blocks unsolicited redirects to third-party ad networks, scam portals, casinos, and fake landing pages.
     */
    fun shouldBlockNavigation(targetUrl: String?, currentUrl: String?): Boolean {
        if (targetUrl.isNullOrBlank()) return false
        val lowerTarget = targetUrl.lowercase()

        // 1. Always allow offline cached files and internal app intents
        if (lowerTarget.startsWith("file://") || 
            lowerTarget.startsWith("animekai://") || 
            lowerTarget.startsWith("about:blank") ||
            lowerTarget.startsWith("data:")) {
            return false
        }

        // 2. Block suspicious app scheme triggers (e.g. market://, intent://)
        if (isSuspiciousScheme(lowerTarget)) {
            return true
        }

        // 3. Block known ad/tracker URLs
        if (isAd(lowerTarget)) {
            return true
        }

        // 4. Check target domain against authorized domain boundaries
        try {
            val uri = Uri.parse(lowerTarget)
            val host = uri.host ?: ""

            if (host.isNotBlank()) {
                // If destination is a known authorized domain or explicitly whitelisted, permit it
                if (isAuthorizedDomain(host)) {
                    return false
                }

                // If user is currently browsing AnimeKai and a script or tap attempts to navigate
                // to a completely unrelated foreign domain, block the rogue redirect!
                val currentHost = try { Uri.parse(currentUrl ?: "").host ?: "" } catch (e: Exception) { "" }
                if (currentHost.isNotBlank() && isAuthorizedDomain(currentHost) && !isAuthorizedDomain(host)) {
                    Log.w(TAG, "Blocked rogue navigation redirect from $currentHost to unauthorized $host")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking navigation URL: $targetUrl", e)
        }

        return false
    }

    /**
     * Checks if a resource URL or redirect target is an ad, tracker, popup, or malicious destination.
     */
    fun isAd(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowercaseUrl = url.lowercase()

        // Check if destination is explicitly whitelisted
        if (isWhitelisted(lowercaseUrl)) {
            return false
        }

        try {
            val uri = Uri.parse(lowercaseUrl)
            val host = uri.host ?: ""
            val path = uri.path ?: ""

            // Whitelist genuine AnimeKai app domains, assets, and standard web fonts/CDNs
            if ((host == "animekai.be" || host.endsWith(".animekai.be") || host == "animekai.to" || host == "animekai.la") 
                && !lowercaseUrl.contains("/ad") && !lowercaseUrl.contains("popunder")) {
                return false
            }

            // Whitelist verified video streaming CDN hosts unless path specifically contains ad indicators
            if (isAuthorizedDomain(host)
                && !lowercaseUrl.contains("/ad") && !lowercaseUrl.contains("pop") && !lowercaseUrl.contains("banner")
                && !lowercaseUrl.contains("direct_link") && !lowercaseUrl.contains("clck")) {
                return false
            }

            // Check blocked hostnames and their subdomains
            for (domain in BLOCKED_DOMAINS) {
                if (host == domain || host.endsWith(".$domain") || host.contains(domain)) {
                    Log.d(TAG, "Blocked ad host: $host for $url")
                    return true
                }
            }

            // Check blocked path / query keywords
            for (keyword in BLOCKED_KEYWORDS) {
                if (lowercaseUrl.contains(keyword)) {
                    Log.d(TAG, "Blocked by keyword pattern: $keyword in $url")
                    return true
                }
            }

            // Detect typical ad patterns in path/query (e.g. /direct_link.php?id=...)
            if (path.contains("pop") || path.contains("banner") || path.contains("click.php") || path.contains("out.php")) {
                return true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking URL: $url", e)
        }

        return false
    }

    /**
     * Identifies suspicious external scheme redirections (e.g. market://, intent://, tg://)
     * often triggered by video ad scripts.
     */
    fun isSuspiciousScheme(url: String): Boolean {
        val lower = url.lowercase()
        return lower.startsWith("market://") ||
                lower.startsWith("intent://") ||
                lower.startsWith("itms-apps://") ||
                lower.startsWith("itms://") ||
                lower.startsWith("tg://") ||
                lower.startsWith("whatsapp://") ||
                lower.startsWith("viber://") ||
                lower.startsWith("facetime://")
    }

    // CSS injected into all pages to hide ad containers, popups, banner ads, and transparent overlay traps
    const val AD_HIDING_CSS = """
        (function() {
            var css = `
                div[id*="ad_"], div[class*="ad-"], div[class*="banner"], 
                iframe[src*="ads"], iframe[id*="ad"], iframe[class*="ad"],
                iframe[src*="pop"], iframe[src*="banner"], iframe[src*="track"],
                a[href*="bet365"], a[href*="1xbet"], a[href*="casino"],
                a[href*="popads"], a[href*="popcash"], a[href*="onclick"],
                div[class*="popup"], div[id*="popup"], .ad-box, .advertisement,
                .ad-container, .adsbygoogle, .banner-ad, [class*="sponsored"],
                #disqus_thread, #ad-bottom, .sticky-ad, .floating-banner,
                .popunder, #popunder, .jw-ad, .vjs-ad, .video-ad,
                #player-ads, #ad-overlay, .overlay-ad, .ad-interstitial,
                div[style*="z-index: 2147483647"], div[style*="z-index: 999999"],
                div[style*="z-index: 99999"],
                div[style*="position: fixed"][style*="opacity: 0"],
                div[style*="position: absolute"][style*="opacity: 0"],
                a[style*="position: fixed"][style*="opacity: 0"],
                a[style*="position: absolute"][style*="opacity: 0"] {
                    display: none !important;
                    height: 0 !important;
                    width: 0 !important;
                    visibility: hidden !important;
                    pointer-events: none !important;
                    opacity: 0 !important;
                }
            `;
            var head = document.head || document.getElementsByTagName('head')[0];
            if (head) {
                var style = document.getElementById('animekai-adshield-css');
                if (!style) {
                    style = document.createElement('style');
                    style.id = 'animekai-adshield-css';
                    style.type = 'text/css';
                    style.appendChild(document.createTextNode(css));
                    head.appendChild(style);
                }
            }
        })();
    """

    // High-level Anti-Ad, Anti-Popup, and Anti-Redirect JS Shield injected into the WebView
    const val AD_SHIELD_JS = """
        (function() {
            if (window.__animeKaiShieldInjected) return;
            window.__animeKaiShieldInjected = true;

            // 1. Completely neutralize window.open, parent.open, top.open
            var noopWindow = {
                closed: true,
                focus: function() {},
                close: function() {},
                postMessage: function() {}
            };
            window.open = function(url, target, features) {
                console.log("[AdShield] Blocked window.open -> " + url);
                return noopWindow;
            };
            try {
                if (window.top && window.top !== window) window.top.open = window.open;
                if (window.parent && window.parent !== window) window.parent.open = window.open;
            } catch(e) {}

            // 2. Kill alert/confirm/prompt dialog spam
            window.alert = function(msg) { console.log("[AdShield] Neutralized alert: " + msg); };
            window.confirm = function(msg) { console.log("[AdShield] Neutralized confirm: " + msg); return false; };
            window.prompt = function(msg, def) { console.log("[AdShield] Neutralized prompt: " + msg); return null; };
            window.onbeforeunload = null;

            // 3. Fake adblocker bypass variables so anti-adblock scripts don't freeze the player
            window.canRunAds = true;
            window.isAdBlockActive = false;
            window.adblock = false;
            window.FuckAdBlock = function() {
                this.on = function(detected, fn) { if (!detected && typeof fn === 'function') fn(); };
                this.check = function() {};
                this.clearEvent = function() {};
            };
            window.fuckAdBlock = new window.FuckAdBlock();
            window.BlockAdBlock = window.FuckAdBlock;
            window.blockAdBlock = window.fuckAdBlock;
            window.Snack = { isAdBlockActive: false };

            // 4. Capture & neutralize click-anywhere redirect traps in capture phase
            function isRogueAdUrl(url) {
                if (!url) return false;
                var u = (url + '').toLowerCase();
                if (u.indexOf('animekai') !== -1 || u.indexOf('megacloud') !== -1 || u.indexOf('rapid-cloud') !== -1) return false;
                if (u.indexOf('pop') !== -1 || u.indexOf('click') !== -1 || u.indexOf('ad') !== -1 || 
                    u.indexOf('banner') !== -1 || u.indexOf('bet') !== -1 || u.indexOf('casino') !== -1 ||
                    u.indexOf('redirect') !== -1 || u.indexOf('track') !== -1 || u.indexOf('jump') !== -1) {
                    return true;
                }
                return false;
            }

            // Intercept programmatic click on anchor tags
            var origAnchorClick = HTMLAnchorElement.prototype.click;
            HTMLAnchorElement.prototype.click = function() {
                var href = this.getAttribute('href') || this.href || '';
                var target = this.getAttribute('target') || '';
                if (isRogueAdUrl(href) || target === '_blank') {
                    console.log("[AdShield] Prevented rogue anchor click -> " + href);
                    return;
                }
                return origAnchorClick.apply(this, arguments);
            };

            // Global capture-phase click listener to intercept full-screen click traps
            window.addEventListener('click', function(e) {
                var target = e.target;
                if (!target) return;

                // Check if target is a full-screen transparent overlay trap
                var rect = target.getBoundingClientRect ? target.getBoundingClientRect() : null;
                var style = window.getComputedStyle ? window.getComputedStyle(target) : null;
                
                if (rect && style && (style.position === 'fixed' || style.position === 'absolute')) {
                    var isFullScreen = (rect.width >= window.innerWidth * 0.85 && rect.height >= window.innerHeight * 0.85);
                    var isTransparent = (style.opacity === '0' || style.backgroundColor === 'rgba(0, 0, 0, 0)' || style.backgroundColor === 'transparent');
                    var isHugeZIndex = parseInt(style.zIndex, 10) > 1000;

                    // If it's a floating transparent full-screen layer not containing the actual video, eliminate it
                    if (isFullScreen && (isTransparent || isHugeZIndex) && !target.querySelector('video') && target.tagName !== 'VIDEO') {
                        console.log("[AdShield] Removed transparent full-screen click trap element");
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        target.remove();
                        return false;
                    }
                }

                // Check if target or parent is an ad anchor
                var anchor = target.closest ? target.closest('a') : null;
                if (anchor) {
                    var href = (anchor.getAttribute('href') || anchor.href || '').toLowerCase();
                    if (isRogueAdUrl(href)) {
                        console.log("[AdShield] Blocked rogue ad anchor navigation: " + href);
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        return false;
                    }
                    if (anchor.getAttribute('target') === '_blank') {
                        anchor.removeAttribute('target');
                    }
                }
            }, true); // Use capture phase to intercept BEFORE website scripts

            // 5. Clean transparent clickjacking overlays from video players
            function purgeAdOverlays() {
                var badSelectors = [
                    'div[style*="z-index: 2147483647"]',
                    'div[style*="z-index: 999999"]',
                    'div[style*="z-index: 99999"]',
                    'div[id*="ad_"]', 'div[class*="ad-"]', 'div[class*="banner"]',
                    'iframe[src*="ads"]', 'iframe[id*="ad"]', 'iframe[class*="ad"]',
                    'iframe[src*="pop"]', 'iframe[src*="banner"]',
                    'a[href*="bet365"]', 'a[href*="1xbet"]', 'a[href*="casino"]',
                    'a[href*="popads"]', 'a[href*="popcash"]',
                    '.popunder', '#popunder', '.jw-ad', '.video-ad', '#ad-overlay'
                ];

                badSelectors.forEach(function(sel) {
                    try {
                        var elements = document.querySelectorAll(sel);
                        for (var i = 0; i < elements.length; i++) {
                            var el = elements[i];
                            // Don't remove real video element or essential wrapper
                            if (!el.querySelector('video') && el.tagName !== 'VIDEO') {
                                el.remove();
                            }
                        }
                    } catch(e) {}
                });

                // Neutralize suspicious popup links & remove target="_blank" from non-anime links
                var links = document.querySelectorAll('a[target="_blank"]');
                for (var j = 0; j < links.length; j++) {
                    var a = links[j];
                    var href = (a.getAttribute('href') || '').toLowerCase();
                    if (isRogueAdUrl(href)) {
                        a.removeAttribute('href');
                        a.onclick = function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            return false;
                        };
                    } else {
                        a.removeAttribute('target');
                    }
                }
            }

            // Run overlay purge on DOM events & mutation
            purgeAdOverlays();
            if (window.MutationObserver) {
                var observer = new MutationObserver(function() {
                    purgeAdOverlays();
                });
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        if (document.body) observer.observe(document.body, { childList: true, subtree: true });
                    });
                }
            }
            setInterval(purgeAdOverlays, 800);
        })();
    """
}
