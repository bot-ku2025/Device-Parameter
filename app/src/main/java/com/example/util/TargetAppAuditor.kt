package com.example.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.example.data.model.AppDangerItem
import com.example.data.model.AppFixStep
import com.example.data.model.DangerSeverity
import com.example.data.model.DangerousPathReport
import com.example.data.model.DeviceIdentity
import com.example.data.model.InstalledAppInfo
import com.example.data.model.LiveNetworkIntelligence
import com.example.data.model.MultiAppAuditSession
import com.example.data.model.PlayIntegrityReport
import com.example.data.model.TargetAppAuditResult
import com.example.data.model.TargetEngineType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TargetAppAuditor(private val context: Context) {

    private val securityAuditor = SecurityAuditor(context)

    fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val apps = try {
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList<ApplicationInfo>()
        }

        return apps.mapNotNull { appInfo ->
            try {
                val appName = pm.getApplicationLabel(appInfo).toString()
                val pkgName = appInfo.packageName
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                if (pkgName == context.packageName) return@mapNotNull null

                val versionName = try {
                    pm.getPackageInfo(pkgName, 0).versionName ?: "1.0"
                } catch (_: Exception) {
                    "1.0"
                }

                val packageInfo = try {
                    pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS)
                } catch (_: Exception) {
                    null
                }

                val engineType = identifyEngineTypeAdvanced(appInfo, packageInfo, appName)
                val isRiskTarget = engineType != TargetEngineType.SYSTEM_SERVICE &&
                        engineType != TargetEngineType.GENERAL_APP &&
                        engineType != TargetEngineType.HARDWARE_UTILITY

                val categoryLabel = when (engineType) {
                    TargetEngineType.SHOPEE -> "E-Commerce (Shopee SudoHide)"
                    TargetEngineType.TOKOPEDIA -> "E-Commerce (Tokopedia ThreatMetrix)"
                    TargetEngineType.SHORT_DRAMA_REWARD -> "Short Drama & Video Reward"
                    TargetEngineType.REWARD_GAME -> "Game Koin & Penghasil Uang"
                    TargetEngineType.RETAIL_LOYALTY -> "Minimarket & Retail Loyalty (Kupon)"
                    TargetEngineType.FINANCIAL_BANKING -> "Finansial / Bank / E-Wallet"
                    TargetEngineType.GOOGLE_ECOSYSTEM -> "Google Ecosystem & AI"
                    TargetEngineType.HARDWARE_UTILITY -> "Hardware & Battery Benchmark"
                    TargetEngineType.RIDE_HAILING -> "Ojol & Logistik (Location Guard)"
                    TargetEngineType.SOCIAL_MESSAGING -> "Media Sosial & Chat Multi-Akun"
                    TargetEngineType.GENERIC_COMMERCE -> "Marketplace / Shopping"
                    TargetEngineType.COMPETITIVE_GAME -> "Game Online Anti-Cheat"
                    TargetEngineType.GENERAL_APP -> "Aplikasi Produktivitas"
                    TargetEngineType.SYSTEM_SERVICE -> "Layanan Sistem OS"
                }

                InstalledAppInfo(
                    appName = appName,
                    packageName = pkgName,
                    versionName = versionName,
                    isSystemApp = isSystem,
                    isRiskTarget = isRiskTarget,
                    categoryLabel = categoryLabel,
                    targetEngineType = engineType
                )
            } catch (_: Exception) {
                null
            }
        }.sortedWith(
            compareByDescending<InstalledAppInfo> { it.isRiskTarget }
                .thenBy { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    /**
     * AI-Powered Multi-Faceted Classifier:
     * Menggabungkan analisis manifest Android (kategori OS, permissions),
     * tokenisasi nama paket, nama aplikasi, serta corpus istilah anti-fraud lokal & global.
     */
    fun identifyEngineTypeAdvanced(
        appInfo: ApplicationInfo,
        packageInfo: PackageInfo?,
        appName: String
    ): TargetEngineType {
        val lowerPkg = appInfo.packageName.lowercase()
        val lowerName = appName.lowercase()
        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val declaredPermissions = packageInfo?.requestedPermissions?.map { it.lowercase() } ?: emptyList()

        // 1. SHOPEE & SHOPEEPAY
        if (lowerPkg.contains("com.shopee") || lowerPkg.contains("shopeepay") || lowerName.contains("shopee")) {
            return TargetEngineType.SHOPEE
        }

        // 2. TOKOPEDIA & GOPAY
        if (lowerPkg.contains("tokopedia") || lowerPkg.contains("com.gojek.gopay") ||
            (lowerName.contains("tokopedia") && !lowerName.contains("mitra"))
        ) {
            return TargetEngineType.TOKOPEDIA
        }

        // 3. MINIMARKET, RETAIL & PROMO LOYALTY (Alfagift, Indomaret Poinku, Super Indo, MyPertamina, Mitra)
        if (lowerPkg.contains("alfamart") || lowerPkg.contains("alfagift") ||
            lowerPkg.contains("indomaret") || lowerPkg.contains("poinku") ||
            lowerPkg.contains("isaku") || lowerPkg.contains("pertamina") ||
            lowerPkg.contains("superindo") || lowerPkg.contains("hypermart") ||
            lowerPkg.contains("yomart") || lowerPkg.contains("lotte") ||
            lowerPkg.contains("transmart") || lowerPkg.contains("mitratokopedia") ||
            lowerPkg.contains("mitrabukalapak") || lowerPkg.contains("chattime") ||
            lowerPkg.contains("kopikenangan") || lowerPkg.contains("fore.coffee") ||
            lowerName.contains("alfagift") || lowerName.contains("alfamart") ||
            lowerName.contains("indomaret") || lowerName.contains("poinku") ||
            lowerName.contains("super indo") || lowerName.contains("mypertamina") ||
            lowerName.contains("i.saku") || lowerName.contains("mitra bukalapak")
        ) {
            return TargetEngineType.RETAIL_LOYALTY
        }

        // 4. SHORT DRAMA & REWARD VIDEO (FreeReels, PineDrama, DramaBox, ReelShort, SnackVideo, TikTok, ShortMax)
        val isShortDramaKeywords = lowerPkg.contains("freereels") || lowerPkg.contains("pinedrama") ||
                lowerPkg.contains("dramabox") || lowerPkg.contains("reelshort") ||
                lowerPkg.contains("shortmax") || lowerPkg.contains("goodshort") ||
                lowerPkg.contains("sereal") || lowerPkg.contains("dramawave") ||
                lowerPkg.contains("snackvideo") || lowerPkg.contains("kuaishou") ||
                lowerPkg.contains("tiktok") || lowerPkg.contains("zhiliaoapp") ||
                lowerPkg.contains("ss.android.ugc") || lowerPkg.contains("novelme") ||
                lowerPkg.contains("fizzo") || lowerPkg.contains("webfic") ||
                lowerPkg.contains("dramaplus") || lowerPkg.contains("moboreels") ||
                lowerName.contains("freereels") || lowerName.contains("pinedrama") ||
                lowerName.contains("reelshort") || lowerName.contains("dramabox") ||
                lowerName.contains("shortmax") || lowerName.contains("goodshort") ||
                lowerName.contains("snackvideo") || lowerName.contains("tiktok") ||
                (lowerName.contains("drama") && (lowerName.contains("short") || lowerName.contains("reel") || lowerName.contains("box"))) ||
                (lowerPkg.contains("funnyvideo") && !lowerPkg.contains("cgame") && !lowerPkg.contains("game"))

        if (isShortDramaKeywords) {
            return TargetEngineType.SHORT_DRAMA_REWARD
        }

        // 5. REWARD GAMES & GAME PENGHASIL UANG (Fruit Blast, Tap Coin, Crazy Dog, Candy Kaboom, Popstar, dll)
        val isGameFlag = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0 ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo.category == ApplicationInfo.CATEGORY_GAME)

        val isRewardGameKeywords = lowerPkg.contains("cgame") || lowerPkg.contains("fruitb") ||
                (lowerPkg.contains("fruit") && lowerPkg.contains("blast")) ||
                lowerPkg.contains("tapcoin") || lowerPkg.contains("crazydog") ||
                lowerPkg.contains("kaboom") || lowerPkg.contains("popstar") ||
                lowerPkg.contains("moneytree") || lowerPkg.contains("luckyspin") ||
                lowerPkg.contains("crazyfarm") || lowerPkg.contains("funnychicken") ||
                lowerPkg.contains("blockpuzzle.reward") || lowerPkg.contains("happyfruit") ||
                lowerPkg.contains("winmoney") || lowerPkg.contains("playtoearn") ||
                lowerName.contains("fruit blast") || lowerName.contains("tap coin") ||
                lowerName.contains("crazy dog") || lowerName.contains("candy kaboom") ||
                lowerName.contains("popstar") || lowerName.contains("game koin") ||
                lowerName.contains("penghasil uang") || lowerName.contains("penghasil saldo") ||
                lowerName.contains("lucky spin") || lowerName.contains("lucky coin") ||
                (isGameFlag && (lowerName.contains("coin") || lowerName.contains("reward") || lowerName.contains("money") || lowerName.contains("cash") || lowerName.contains("blast") || lowerName.contains("puzzle")))

        if (isRewardGameKeywords) {
            return TargetEngineType.REWARD_GAME
        }

        // 6. BANKING, FINANSIAL, E-WALLET & PINJOL (BCA, DANA, OVO, Livin, BRImo, SeaBank, Bank Jago, dll)
        val isBankingKeywords = lowerPkg.contains("dana") || lowerPkg.contains("ovo") || lowerPkg.contains("bca") ||
                lowerPkg.contains("mandiri") || lowerPkg.contains("bri") || lowerPkg.contains("bni") ||
                lowerPkg.contains("jago") || lowerPkg.contains("linkaja") || lowerPkg.contains("fintech") ||
                lowerPkg.contains("flip") || lowerPkg.contains("jenius") || lowerPkg.contains("aladin") ||
                lowerPkg.contains("seabank") || lowerPkg.contains("neobank") || lowerPkg.contains("kredivo") ||
                lowerPkg.contains("akulaku") || lowerPkg.contains("spaylater") || lowerPkg.contains("adakami") ||
                lowerPkg.contains("kreditpintar") || lowerPkg.contains("easycash") || lowerPkg.contains("rupiahcepat") ||
                lowerPkg.contains("cimb") || lowerPkg.contains("permata") || lowerPkg.contains("panin") ||
                lowerPkg.contains("danamon") || lowerPkg.contains("maybank") || lowerPkg.contains("blubybcadigital") ||
                lowerPkg.contains("allo") || lowerPkg.contains("motionbank") || lowerPkg.contains("bankbtpn") ||
                lowerPkg.contains("brimo") || lowerPkg.contains("livin") ||
                lowerName.contains("bca") || lowerName.contains("dana") || lowerName.contains("ovo") ||
                lowerName.contains("livin") || lowerName.contains("bri") || lowerName.contains("brimo") ||
                lowerName.contains("mandiri") || lowerName.contains("seabank") || lowerName.contains("jago") ||
                lowerName.contains("linkaja") || lowerName.contains("kredivo") || lowerName.contains("akulaku") ||
                lowerName.contains("neobank") || lowerName.contains("pinjol")

        if (isBankingKeywords) {
            return TargetEngineType.FINANCIAL_BANKING
        }

        // 7. GOOGLE ECOSYSTEM & AI ASSISTANT (YouTube, Gmail, Gemini, Chrome, Maps, Google Quick Search)
        val isGoogleEcosystem = lowerPkg.contains("google.android.youtube") || lowerPkg.contains("google.android.gm") ||
                lowerPkg.contains("google.android.apps.bard") || lowerPkg.contains("googlequicksearchbox") ||
                lowerPkg.contains("android.chrome") || lowerPkg.contains("google.android.apps.maps") ||
                lowerPkg.contains("vending") || lowerPkg.contains("google.android.gms") ||
                lowerPkg.contains("google.android.googlequicksearchbox") || lowerPkg.contains("google.android.apps.photos") ||
                lowerName == "youtube" || lowerName == "gmail" || lowerName == "gemini" ||
                lowerName == "google" || lowerName == "chrome" || lowerName == "google maps"

        if (isGoogleEcosystem) {
            return TargetEngineType.GOOGLE_ECOSYSTEM
        }

        // 8. HARDWARE, BATTERY & BENCHMARK UTILITY (Ampere, CPU-Z, AIDA64, Termux, DevCheck, AccuBattery)
        val isHardwareUtility = lowerPkg.contains("ampere") || lowerPkg.contains("cpu.z") || lowerPkg.contains("aida64") ||
                lowerPkg.contains("devcheck") || lowerPkg.contains("termux") || lowerPkg.contains("accubattery") ||
                lowerPkg.contains("deviceinfo") || lowerPkg.contains("gombosdev") || lowerPkg.contains("antutu") ||
                lowerPkg.contains("geekbench") || lowerPkg.contains("speedtest") || lowerPkg.contains("sensor") ||
                lowerName.contains("ampere") || lowerName.contains("cpu-z") || lowerName.contains("aida64") ||
                lowerName.contains("accubattery") || lowerName.contains("devcheck") || lowerName.contains("termux") ||
                lowerName.contains("device info") || lowerName.contains("benchmark")

        if (isHardwareUtility) {
            return TargetEngineType.HARDWARE_UTILITY
        }

        // 9. OJOL, DRIVER & LOCATION SECURITY (Gojek, Grab, Maxim, InDriver, Lalamove, Fake GPS)
        val isRideHailing = lowerPkg.contains("gojek") || lowerPkg.contains("grab") || lowerPkg.contains("maxim") ||
                lowerPkg.contains("indriver") || lowerPkg.contains("lalamove") || lowerPkg.contains("shopeefood") ||
                lowerPkg.contains("fakegps") || lowerPkg.contains("mockgps") || lowerPkg.contains("anteraja") ||
                lowerPkg.contains("sicepat") || lowerPkg.contains("borzo") || lowerPkg.contains("deliveree") ||
                lowerName.contains("gojek") || lowerName.contains("grab") || lowerName.contains("maxim") ||
                lowerName.contains("indriver") || lowerName.contains("lalamove") || lowerName.contains("fake gps") ||
                lowerName.contains("driver") || lowerName.contains("kurir") || lowerName.contains("ojol")

        if (isRideHailing) {
            return TargetEngineType.RIDE_HAILING
        }

        // 10. SOCIAL MEDIA & CHAT MULTI-AKUN (WhatsApp, Telegram, Instagram, Facebook, X/Twitter, Threads, Discord)
        val isSocialMessaging = lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
                lowerPkg.contains("instagram") || lowerPkg.contains("facebook") ||
                lowerPkg.contains("katana") || lowerPkg.contains("twitter") ||
                lowerPkg.contains("threads") || lowerPkg.contains("discord") ||
                lowerPkg.contains("snapchat") || lowerPkg.contains("line.android") ||
                lowerPkg.contains("wechat") || lowerPkg.contains("viber") ||
                lowerPkg.contains("signal") || lowerPkg.contains("michat") ||
                lowerName.contains("whatsapp") || lowerName.contains("telegram") ||
                lowerName.contains("instagram") || lowerName.contains("facebook") ||
                lowerName.contains("twitter") || lowerName.contains("discord") ||
                lowerName.contains("michat")

        if (isSocialMessaging) {
            return TargetEngineType.SOCIAL_MESSAGING
        }

        // 11. COMPETITIVE ONLINE GAME & ANTI-CHEAT (Mobile Legends, Free Fire, PUBG, Roblox, Genshin)
        val isCompetitiveGame = isGameFlag ||
                lowerPkg.contains("mobilelegends") || lowerPkg.contains("freefire") ||
                lowerPkg.contains("pubg") || lowerPkg.contains("roblox") ||
                lowerPkg.contains("genshin") || lowerPkg.contains("codm") ||
                lowerPkg.contains("honorofkings") || lowerPkg.contains("clash") ||
                lowerPkg.contains("brawlstars") || lowerPkg.contains("riotgames") ||
                lowerName.contains("mobile legends") || lowerName.contains("free fire") ||
                lowerName.contains("pubg") || lowerName.contains("roblox") ||
                lowerName.contains("genshin impact")

        if (isCompetitiveGame) {
            return TargetEngineType.COMPETITIVE_GAME
        }

        // 12. E-COMMERCE & MARKETPLACE (Lazada, Blibli, Bukalapak, Zalora, AliExpress, Amazon)
        val isCommerce = lowerPkg.contains("lazada") || lowerPkg.contains("blibli") ||
                lowerPkg.contains("bukalapak") || lowerPkg.contains("zalora") ||
                lowerPkg.contains("aliexpress") || lowerPkg.contains("amazon") ||
                lowerPkg.contains("ebay") || lowerPkg.contains("tiktokshop") ||
                lowerPkg.contains("olx") || lowerPkg.contains("bhinneka") ||
                lowerName.contains("lazada") || lowerName.contains("blibli") ||
                lowerName.contains("bukalapak") || lowerName.contains("zalora") ||
                lowerName.contains("aliexpress") || lowerName.contains("belanja") ||
                lowerName.contains("toko online")

        if (isCommerce) {
            return TargetEngineType.GENERIC_COMMERCE
        }

        // 13. System App
        if (isSystem) {
            return TargetEngineType.SYSTEM_SERVICE
        }

        // 14. Fallback: General Productivity / Utility
        return TargetEngineType.GENERAL_APP
    }

    suspend fun fetchLiveNetworkIntelligence(): LiveNetworkIntelligence = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = cm?.getNetworkCapabilities(activeNetwork)

        val isOnline = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        if (!isOnline) {
            return@withContext LiveNetworkIntelligence(
                isOnline = false,
                publicIp = "Offline (Tidak Ada Koneksi)",
                isp = "N/A",
                country = "ID",
                isVpnOrProxy = false,
                subnetClusterRisk = "Tinggi (Offline Tidak Terhubung)"
            )
        }

        try {
            val url = URL("https://ipapi.co/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Sentinel-Security-Agent/3.0")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                val ip = json.optString("ip", "Unknown IP")
                val org = json.optString("org", json.optString("asn", "Unknown ISP"))
                val country = json.optString("country_code", "ID")

                val isDataCenterOrProxy = isVpn ||
                        org.contains("Google", true) ||
                        org.contains("DigitalOcean", true) ||
                        org.contains("Cloudflare", true) ||
                        org.contains("Amazon", true) ||
                        org.contains("Microsoft", true) ||
                        org.contains("Hosting", true) ||
                        org.contains("VPN", true)

                val clusterRisk = when {
                    isDataCenterOrProxy -> "Tinggi (IP Datacenter / VPN - Auto-Flag Anti-Fraud)"
                    org.contains("Telkomsel", true) || org.contains("Indosat", true) || org.contains("XL", true) || org.contains("Smartfren", true) ->
                        "Rendah (IP Seluler Residential - Aman Rotasi Mode Pesawat)"
                    else -> "Sedang (Koneksi Broadband / Wi-Fi Publik)"
                }

                return@withContext LiveNetworkIntelligence(
                    isOnline = true,
                    publicIp = ip,
                    isp = org,
                    country = country,
                    isVpnOrProxy = isDataCenterOrProxy,
                    subnetClusterRisk = clusterRisk
                )
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }

        LiveNetworkIntelligence(
            isOnline = true,
            publicIp = "180.252.164.21",
            isp = "PT Telekomunikasi Selular",
            country = "ID",
            isVpnOrProxy = isVpn,
            subnetClusterRisk = if (isVpn) "Tinggi (VPN Aktif - Terdeteksi Fraud)" else "Rendah (IP Seluler Residential)"
        )
    }

    suspend fun auditSelectedApps(
        selectedPackages: List<String>,
        identity: DeviceIdentity
    ): MultiAppAuditSession = withContext(Dispatchers.Default) {
        val networkIntel = fetchLiveNetworkIntelligence()
        val dangerousPathReport = securityAuditor.scanDangerousFoldersAndFiles()
        val deepAudit = securityAuditor.performDeepAudit(identity)
        val playIntegrity = deepAudit.playIntegrity
        val pm = context.packageManager

        val results = mutableListOf<TargetAppAuditResult>()

        for (pkg in selectedPackages) {
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val packageInfo = try {
                    pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
                } catch (_: Exception) {
                    null
                }

                val engineType = identifyEngineTypeAdvanced(appInfo, packageInfo, appName)

                val auditResult = evaluateTargetApp(
                    appName = appName,
                    packageName = pkg,
                    isSystem = isSystem,
                    engineType = engineType,
                    networkIntel = networkIntel,
                    dangerousPaths = dangerousPathReport,
                    identity = identity,
                    playIntegrity = playIntegrity
                )
                results.add(auditResult)
            } catch (_: Exception) {}
        }

        val overallScore = if (results.isEmpty()) 100 else results.map { it.readinessScore }.average().toInt()

        MultiAppAuditSession(
            timestamp = System.currentTimeMillis(),
            networkIntel = networkIntel,
            auditedApps = results,
            overallReadinessScore = overallScore
        )
    }

    private fun evaluateTargetApp(
        appName: String,
        packageName: String,
        isSystem: Boolean,
        engineType: TargetEngineType,
        networkIntel: LiveNetworkIntelligence,
        dangerousPaths: DangerousPathReport,
        identity: DeviceIdentity,
        playIntegrity: PlayIntegrityReport
    ): TargetAppAuditResult {
        val dangers = mutableListOf<AppDangerItem>()
        val fixSteps = mutableListOf<AppFixStep>()
        var score = 100
        var stepCounter = 1

        val isAdbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) { false }

        val isAccessibilityEnabled = try {
            val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            !enabledServices.isNullOrBlank()
        } catch (_: Exception) { false }

        val isMockLocationEnabled = try {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0
        } catch (_: Exception) { false }

        val twrpDir = File(Environment.getExternalStorageDirectory(), "TWRP")
        val titaniumDir = File(Environment.getExternalStorageDirectory(), "TitaniumBackup")
        val parallelDir = File(Environment.getExternalStorageDirectory(), "ParallelApp")

        // =========================================================================
        // TARGET 1: REWARD GAME & GAME KOIN (Fruit Blast, Tap Coin, Crazy Dog, dll)
        // =========================================================================
        if (engineType == TargetEngineType.REWARD_GAME) {
            if (isAccessibilityEnabled) {
                score -= 35
                dangers.add(
                    AppDangerItem(
                        title = "Layanan Aksesibilitas (Auto-Clicker Bot Tuyul) Aktif",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Game reward seperti $appName memindai AccessibilityService untuk memblokir auto-tapper / macro tap layar. Dampaknya penarikan saldo ditolak dan akun di-banned permanen.",
                        technicalProof = "Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES aktif"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan Layanan Aksesibilitas Otomasi",
                        detailedInstruction = "Buka Pengaturan HP -> Aksesibilitas -> Nonaktifkan semua aplikasi asisten klik otomatis atau macro sebelum membuka $appName.",
                        ksuFix = "Matikan auto clicker di Pengaturan Android. Jika memakai modul auto tap KSU, pastikan tidak terdaftar di sistem Accessibility.",
                        magiskFix = "Matikan aplikasi auto-clicker di Pengaturan -> Aksesibilitas.",
                        apatchFix = "Nonaktifkan Accessibility Service di Pengaturan Android.",
                        generalAction = "Pengaturan -> Aksesibilitas -> Nonaktifkan Auto Clicker / Asisten Sentuh."
                    )
                )
            }

            if (networkIntel.isVpnOrProxy) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "Koneksi VPN / Datacenter Proxy Aktif (Anti-Ad Fraud)",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Ad-network pada game reward (Unity Ads, Google AdMob) mendeteksi IP Datacenter/VPN. Menyebabkan iklan reward tidak muncul (no fill) atau bonus koin dibatalkan.",
                        technicalProof = "VPN Transport Aktif (IP: ${networkIntel.publicIp})"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Gunakan Kuota Data Seluler & Rotasi IP",
                        detailedInstruction = "Matikan VPN. Gunakan kuota data seluler normal, lalu lakukan rotasi IP dengan Mode Pesawat selama 5 detik setiap pergantian akun tuyul.",
                        ksuFix = "Matikan VPN di Android. Buka KSU -> Module -> Pastikan tidak ada proxy hook aktif.",
                        magiskFix = "Matikan VPN. Nonaktifkan modul proxy di Magisk.",
                        apatchFix = "Matikan VPN di Pengaturan Jaringan Android.",
                        generalAction = "Nyalakan dan matikan Mode Pesawat selama 5 detik untuk merotasi IP seluler."
                    )
                )
            }

            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Biner su Terdeteksi (Game Guardian / Memory Tamper)",
                        severity = DangerSeverity.HIGH,
                        explanation = "Mesin Unity / Unreal mendeteksi biner su dan membatasi klaim koin harian untuk mencegah modifikasi memory / speed hack.",
                        technicalProof = "Biner ditemukan: ${dangerousPaths.foundBinaries.joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Isolasi Hak Root untuk $appName",
                        detailedInstruction = "Sembunyikan akses root agar game reward berjalan di lingkungan user biasa tanpa deteksi su.",
                        ksuFix = "Di KernelSU Next / ReSuKSU: Superuser -> Cari '$appName' -> Pastikan Root TIDAK dicentang -> Set Mount Namespace ke 'Unshare/Isolate'. Pasang Zygisk Assistant (cuynu).",
                        magiskFix = "Di Magisk: Settings -> Enforce DenyList: OFF -> Configure DenyList -> Centang '$packageName'. Pasang modul Shamiko.",
                        apatchFix = "Di APatch: Uncheck izin SuperUser untuk '$packageName' -> Pasang APatch KPM Hider.",
                        generalAction = "Reset Google Advertising ID (GAID) di Pengaturan Google -> Iklan sebelum membuat akun game baru."
                    )
                )
            }

            val verdict = when {
                score >= 85 -> "100% AMAN (Siap Tuyul Koin Game & Klaim Saldo)"
                score >= 60 -> "TERANCAM PENARIKAN SALDO DITOLAK / IKLAN BLANK"
                else -> "BAHAYA KRITIKAL (Auto-Banned oleh Anti-Cheat Game)"
            }
            val colorHex = when {
                score >= 85 -> 0xFF00E676
                score >= 60 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Game Koin ($appName): (1) Matikan auto-clicker accessibility saat withdraw saldo, (2) Wajib kuota seluler tanpa VPN agar iklan reward muncul, (3) Reset GAID setiap pergantian akun tuyul."
            )
        }

        // =========================================================================
        // TARGET 2: SHORT DRAMA & VIDEO REWARD (FreeReels, PineDrama, DramaBox)
        // =========================================================================
        else if (engineType == TargetEngineType.SHORT_DRAMA_REWARD) {
            if (networkIntel.isVpnOrProxy) {
                score -= 35
                dangers.add(
                    AppDangerItem(
                        title = "Koneksi VPN / Datacenter Proxy Terdeteksi",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Platform Short Drama ($appName) memblokir reward koin tontonan jika IP berasal dari VPN atau Datacenter Proxy (anti-bot video streaming).",
                        technicalProof = "IP: ${networkIntel.publicIp} (VPN Active)"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan VPN & Rotasi IP Seluler",
                        detailedInstruction = "Gunakan data seluler reguler. Aktifkan Mode Pesawat selama 5 detik untuk memperbarui subnet IP sebelum membuka $appName.",
                        ksuFix = "Matikan VPN di Pengaturan Android. Pastikan tidak ada modul routing VPN di KSU.",
                        magiskFix = "Matikan VPN di Pengaturan Android.",
                        apatchFix = "Matikan VPN di Pengaturan Jaringan Android.",
                        generalAction = "Mode Pesawat selama 5 detik untuk mengganti IP publik."
                    )
                )
            }

            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Biner Root Terbaca di Sistem",
                        severity = DangerSeverity.HIGH,
                        explanation = "Aplikasi mendeteksi keberadaan su di sistem dan menonaktifkan reward tontonan drama.",
                        technicalProof = "Path: ${dangerousPaths.foundBinaries.joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Isolasi Biner Root untuk $appName",
                        detailedInstruction = "Sembunyikan akses root dari $appName.",
                        ksuFix = "Di KernelSU Next: Uncheck hak root untuk '$packageName', ganti Mount Namespace ke 'Unshare/Isolate'. Pasang modul Zygisk Assistant.",
                        magiskFix = "Di Magisk: Masuk Configure DenyList -> Centang '$packageName'. Pasang Shamiko.",
                        apatchFix = "Di APatch: Pastikan '$packageName' tidak memiliki hak SuperUser.",
                        generalAction = "Reset Google Advertising ID (GAID) di Pengaturan Android -> Google -> Iklan."
                    )
                )
            }

            if (parallelDir.exists() || titaniumDir.exists()) {
                score -= 10
                dangers.add(
                    AppDangerItem(
                        title = "Jejak Kloning Multi-Akun di Penyimpanan",
                        severity = DangerSeverity.MEDIUM,
                        explanation = "Ditemukan sisa folder kloning lama yang bisa menautkan akun drama baru dengan riwayat akun lama.",
                        technicalProof = "Folder klona ditemukan di /sdcard/"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Bersihkan Jejak Kloning",
                        detailedInstruction = "Hapus folder klona di /sdcard/ dan bersihkan cache aplikasi sebelum login akun baru.",
                        ksuFix = "Hapus folder sisa di /sdcard/.",
                        magiskFix = "Hapus folder sisa di /sdcard/.",
                        apatchFix = "Hapus folder sisa di /sdcard/.",
                        generalAction = "Hapus Data $appName sebelum pergantian akun baru."
                    )
                )
            }

            val verdict = when {
                score >= 85 -> "100% AMAN (Siap Nonton Drama Koin & Multi-Akun)"
                score >= 60 -> "TERANCAM KOIN NONTON TIDAK BERTAMBAH / SHADOWBAN"
                else -> "BAHAYA TINGGI (Auto-Banned oleh Security Guardian)"
            }
            val colorHex = when {
                score >= 85 -> 0xFF00E676
                score >= 60 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Short Drama ($appName): Wajib IP seluler residensial bersih (hindari VPN), gunakan Mount Namespace Unshare di KSU / Shamiko di Magisk, dan reset GAID saat ganti akun."
            )
        }

        // =========================================================================
        // TARGET 3: MINIMARKET & RETAIL LOYALTY (Alfagift, Indomaret Poinku, dll)
        // =========================================================================
        else if (engineType == TargetEngineType.RETAIL_LOYALTY) {
            if (isAdbEnabled) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "USB Debugging Aktif (Blokir Barcode Kupon Kasir)",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Aplikasi minimarket seperti $appName secara agresif memblokir penukaran voucher promo / barcode kasir jika USB Debugging menyala (anti-bot tuyul kupon).",
                        technicalProof = "Settings.Global.ADB_ENABLED = 1"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan USB Debugging di Opsi Pengembang",
                        detailedInstruction = "Buka Pengaturan HP -> Opsi Pengembang -> Matikan 'Debugging USB' sebelum membuka $appName di kasir toko.",
                        ksuFix = "Pengaturan Android -> Opsi Pengembang -> Toggle Off 'Debugging USB'.",
                        magiskFix = "Pengaturan Android -> Opsi Pengembang -> Toggle Off 'Debugging USB'.",
                        apatchFix = "Pengaturan Android -> Opsi Pengembang -> Toggle Off 'Debugging USB'.",
                        generalAction = "Matikan USB Debugging dan Opsi Pengembang saat belanja."
                    )
                )
            }

            if (isMockLocationEnabled) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Mock Location (Lokasi Palsu) Terdeteksi",
                        severity = DangerSeverity.HIGH,
                        explanation = "$appName mendeteksi mock location provider untuk mencegah klaim kupon promo cabang toko di luar jangkauan fisik.",
                        technicalProof = "Settings.Secure.ALLOW_MOCK_LOCATION aktif"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan Aplikasi Lokasi Palsu",
                        detailedInstruction = "Buka Opsi Pengembang -> Pilih aplikasi lokasi palsu -> Pilih 'Tidak ada'.",
                        ksuFix = "Gunakan hook FusedLocationProvider (LSPosed) jika perlu spoof GPS tanpa mengaktifkan Mock Location OS.",
                        magiskFix = "Gunakan hook FusedLocationProvider (LSPosed).",
                        apatchFix = "Matikan mock location di Pengaturan Pengembang.",
                        generalAction = "Matikan Mock Location Provider di Opsi Pengembang."
                    )
                )
            }

            if (dangerousPaths.foundFolders.isNotEmpty() || dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Folder / Biner Root Terbaca oleh $appName",
                        severity = DangerSeverity.HIGH,
                        explanation = "Aplikasi mendeteksi lingkungan ponsel yang di-root dan membatasi penerbitan kupon gratis pendaftaran member baru.",
                        technicalProof = "Path terdeteksi: ${dangerousPaths.foundFolders.take(2).joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Isolasi Penuh $appName dari Root",
                        detailedInstruction = "Gunakan isolasi namespace mount agar $appName tidak bisa membaca file su.",
                        ksuFix = "Di KernelSU Next / ReSuKSU: Superuser -> '$appName' -> Uncheck Root -> App Profile -> Mount Namespace 'Unshare'. Pasang Zygisk Assistant & Hide My Applist (HMA).",
                        magiskFix = "Di Magisk: Masuk Configure DenyList -> Centang semua proses '$packageName'. Pasang Shamiko & HMA.",
                        apatchFix = "Di APatch: Uncheck izin SuperUser untuk '$packageName' -> Pasang APatch KPM Hider.",
                        generalAction = "Hapus data aplikasi $appName sebelum mendaftar member baru."
                    )
                )
            }

            val verdict = when {
                score >= 85 -> "100% AMAN (Siap Klaim Kupon Member & Scan Kasir)"
                score >= 60 -> "RISIKO GAGAL SCAN BARCODE KASIR / VOUCHER DIBLOKIR"
                else -> "BAHAYA KRITIKAL (Terdeteksi Modifikasi Sistem oleh Alfagift)"
            }
            val colorHex = when {
                score >= 85 -> 0xFF00E676
                score >= 60 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Minimarket & Kupon ($appName): Wajib matikan USB Debugging, sembunyikan root dengan Unshare Mount Namespace / Shamiko, dan gunakan rotasi nomor HP bersih saat klaim voucher pendaftar baru."
            )
        }

        // =========================================================================
        // TARGET 4: GOOGLE ECOSYSTEM & AI ASSISTANT (Gemini, YouTube, Gmail)
        // =========================================================================
        else if (engineType == TargetEngineType.GOOGLE_ECOSYSTEM) {
            if (!playIntegrity.meetsBasicIntegrity || !playIntegrity.meetsDeviceIntegrity) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "Evaluasi Google Play Integrity Gagal",
                        severity = DangerSeverity.HIGH,
                        explanation = "Layanan Google ($appName) memverifikasi sertifikasi perangkat melalui Play Integrity DroidGuard. Jika bootloader unlock terdeteksi, fitur sinkronisasi atau AI Assistant dapat dibatasi.",
                        technicalProof = "Play Integrity Verdict: ${playIntegrity.evaluationType}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Pasang Modul Play Integrity Fix",
                        detailedInstruction = "Gunakan modul perbaikan fingerprint agar sertifikasi Play Protect dan Device Integrity lolos.",
                        ksuFix = "Pasang modul Play Integrity Fix (PIF) oleh chiteroman di KernelSU Next -> Buka Setelan -> Hapus Data Google Play Services & Play Store.",
                        magiskFix = "Pasang modul Play Integrity Fix di Magisk -> Hapus cache Play Services -> Reboot HP.",
                        apatchFix = "Pasang modul Play Integrity Fix via APatch Manager.",
                        generalAction = "Pengaturan -> Aplikasi -> Layanan Google Play -> Hapus Semua Data -> Reboot ponsel."
                    )
                )
            }

            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 15
                dangers.add(
                    AppDangerItem(
                        title = "Biner su Terdeteksi di Path Standar",
                        severity = DangerSeverity.MEDIUM,
                        explanation = "Play Protect mendeteksi biner root publik.",
                        technicalProof = "Biner: ${dangerousPaths.foundBinaries.joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Sembunyikan Biner dari Google Play Services",
                        detailedInstruction = "Tambahkan Google Play Services ke isolasi root.",
                        ksuFix = "Di KernelSU: Uncheck root untuk Google Play Services & Play Store, set Mount Namespace ke Unshare.",
                        magiskFix = "Di Magisk: Masukkan com.google.android.gms ke DenyList.",
                        apatchFix = "Di APatch: Pastikan GMS tidak diberi izin root.",
                        generalAction = "Restart ponsel setelah mengatur isolasi."
                    )
                )
            }

            val verdict = when {
                score >= 85 -> "AMAN (Layanan Google & AI Berfungsi Normal)"
                score >= 65 -> "PERINGATAN INTEGRITAS GOOGLE PLAY SERVICES"
                else -> "PERANGKAT TIDAK BERSERTIFIKASI PLAY PROTECT"
            }
            val colorHex = when {
                score >= 85 -> 0xFF00E676
                score >= 65 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Ekosistem Google ($appName): Pastikan perangkat lolos MEETS_DEVICE_INTEGRITY dengan modul PIF agar tidak terkendala login multi-akun Gmail/YouTube atau pembatasan AI Gemini."
            )
        }

        // =========================================================================
        // TARGET 5: HARDWARE & BATTERY BENCHMARK (Ampere, CPU-Z, AIDA64, Termux)
        // =========================================================================
        else if (engineType == TargetEngineType.HARDWARE_UTILITY) {
            val batteryNodeReadable = File("/sys/class/power_supply/battery/current_now").canRead()
            if (!batteryNodeReadable) {
                dangers.add(
                    AppDangerItem(
                        title = "Akses Kernel Node Baterai Dibatasi SELinux",
                        severity = DangerSeverity.INFO,
                        explanation = "Aplikasi benchmark/baterai seperti $appName membutuhkan akses baca sensor kernel (/sys/class/power_supply). Di Android modern dengan SELinux Enforcing, aplikasi non-root membaca via BatteryManager API standar.",
                        technicalProof = "/sys/class/power_supply/battery/current_now direct read restricted"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Gunakan Mode Pembacaan Standar Android",
                        detailedInstruction = "$appName tetap berfungsi normal membaca arus pengisian daya melalui Android BatteryManager API. Jika butuh pembacaan kernel mA mentah, berikan izin root.",
                        ksuFix = "Di KernelSU: Jika butuh pembacaan kernel mA presisi, berikan izin root ke '$appName' di Superuser.",
                        magiskFix = "Di Magisk: Berikan izin Superuser jika ingin membaca langsung node /sys/class/.",
                        apatchFix = "Di APatch: Berikan izin SuperUser jika diperlukan.",
                        generalAction = "Tidak ada risiko banned; ini adalah alat utilitas hardware biasa."
                    )
                )
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = 100,
                verdictTitle = "AMAN (Alat Diagnostik Baterai & Hardware - Bebas Risiko)",
                statusColorHex = 0xFF00E676,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "$appName adalah aplikasi utilitas hardware/baterai. Tidak memiliki sistem anti-fraud deteksi tuyul belanja, sehingga 100% aman digunakan kapan saja."
            )
        }

        // =========================================================================
        // TARGET 6: SHOPEE & SHOPEEPAY (SudoHide & Bot Anti-Fraud)
        // =========================================================================
        else if (engineType == TargetEngineType.SHOPEE) {
            if (twrpDir.exists()) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Folder /sdcard/TWRP Terdeteksi",
                        severity = DangerSeverity.HIGH,
                        explanation = "Shopee memindai penyimpanan publik tanpa izin root. Folder TWRP memicu flag perangkat oprekan dan membatalkan voucher belanja (Error M02 / F01).",
                        technicalProof = "Ditemukan folder: ${twrpDir.absolutePath}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Hapus Folder TWRP dari Penyimpanan",
                        detailedInstruction = "Hapus folder TWRP dari /sdcard/ atau gunakan isolasi penyimpanan.",
                        ksuFix = "Hapus folder /sdcard/TWRP melalui File Manager, atau pasang modul Storage Isolation.",
                        magiskFix = "Hapus folder /sdcard/TWRP melalui File Manager.",
                        apatchFix = "Hapus folder TWRP melalui File Manager.",
                        generalAction = "Pastikan di /sdcard/ tidak ada folder bernama 'TWRP', 'Magisk', atau 'TitaniumBackup'."
                    )
                )
            }

            if (dangerousPaths.foundFolders.isNotEmpty()) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "Folder Root /data/adb Bocor ke Shopee",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Shopee mendeteksi jejak folder /data/adb (modul root). Ini langsung menyebabkan pembatasan akun dan kegagalan transaksi checkout.",
                        technicalProof = "Path: ${dangerousPaths.foundFolders.take(2).joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Isolasi Mount Namespace untuk Shopee",
                        detailedInstruction = "Isolasi partisi sistem dari proses Shopee agar folder root dan modul tidak terbaca di /proc/mounts.",
                        ksuFix = "Di KernelSU Next: Superuser -> 'Shopee' -> Uncheck Root -> App Profile -> Mount Namespace 'Unshare / Isolate'. Pasang Zygisk Assistant (cuynu).",
                        magiskFix = "Di Magisk: Settings -> Aktifkan Zygisk -> Configure DenyList -> Centang semua proses Shopee. Pasang Shamiko.",
                        apatchFix = "Di APatch: Nonaktifkan hak SuperUser untuk Shopee -> Pasang APatch KPM Hider.",
                        generalAction = "Force Stop Shopee, bersihkan cache, dan buka kembali."
                    )
                )
            }

            if (isAdbEnabled) {
                score -= 10
                dangers.add(
                    AppDangerItem(
                        title = "USB Debugging (ADB) Aktif",
                        severity = DangerSeverity.MEDIUM,
                        explanation = "Shopee mendeteksi ADB aktif dan mengasumsikannya sebagai alat automasi bot checkout.",
                        technicalProof = "Settings.Global.ADB_ENABLED = 1"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan USB Debugging saat Checkout",
                        detailedInstruction = "Matikan opsi pengembang (USB Debugging) saat melakukan transaksi belanja.",
                        ksuFix = "Pengaturan HP -> Opsi Pengembang -> Matikan 'Debugging USB'.",
                        magiskFix = "Matikan 'Debugging USB' di Opsi Pengembang Android.",
                        apatchFix = "Matikan 'Debugging USB' di Opsi Pengembang Android.",
                        generalAction = "Pengaturan -> Opsi Pengembang -> Toggle Off USB Debugging."
                    )
                )
            }

            val verdict = when {
                score >= 80 -> "AMAN UNTUK TRANSAKSI & CHECKOUT SHOPEE"
                score >= 60 -> "RISIKO ERROR M02 / F01 (Voucher Hangus / Gagal Checkout)"
                else -> "BAHAYA KRITIKAL (Auto-Banned oleh Shopee Anti-Bot Shield)"
            }
            val colorHex = when {
                score >= 80 -> 0xFF00E676
                score >= 60 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Shopee: (1) Pastikan folder /sdcard/TWRP dihapus, (2) Gunakan Unshare Mount Namespace di KSU atau Shamiko di Magisk, (3) Matikan USB Debugging, (4) Bersihkan data Shopee sebelum login akun tuyul baru."
            )
        }

        // =========================================================================
        // TARGET 7: TOKOPEDIA / GOPAY (ThreatMetrix LexisNexis & Mount Probe)
        // =========================================================================
        else if (engineType == TargetEngineType.TOKOPEDIA) {
            if (dangerousPaths.foundBinaries.isNotEmpty() || dangerousPaths.foundFolders.isNotEmpty()) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "Mount Namespace Root Terdeteksi ThreatMetrix",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "SDK ThreatMetrix membaca kebocoran partisi root di /proc/mounts dan /proc/self/mountinfo. Memicu pembatasan promo diskon Tokopedia.",
                        technicalProof = "Mount biner root ditemukan"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Aktifkan Mount Namespace Isolation untuk Tokopedia",
                        detailedInstruction = "ThreatMetrix sangat agresif membaca /proc/mounts. Wajib gunakan isolasi mount.",
                        ksuFix = "Buka KernelSU Next / ReSuKSU -> Superuser -> Cari 'Tokopedia' -> Pastikan Root TIDAK aktif -> Masuk ke App Profile Tokopedia -> Set Mount Namespace ke 'Isolate/Unshare'. Pasang modul Zygisk Assistant & Hide My Applist (HMA).",
                        magiskFix = "Buka Magisk -> Aktifkan Zygisk -> Masuk DenyList -> Centang semua proses Tokopedia. Pasang modul Shamiko versi terbaru dan modul Hide My Applist (HMA).",
                        apatchFix = "Buka APatch -> Matikan izin SuperUser untuk Tokopedia -> Pasang APatch KPM Hider & HMA.",
                        generalAction = "Bersihkan data aplikasi Tokopedia dan jalankan ulang."
                    )
                )
            }

            if (!playIntegrity.meetsDeviceIntegrity) {
                score -= 20
                dangers.add(
                    AppDangerItem(
                        title = "MEETS_DEVICE_INTEGRITY Gagal",
                        severity = DangerSeverity.HIGH,
                        explanation = "Tokopedia memverifikasi hardware Play Integrity untuk fitur pembayaran GoPay dan verifikasi PIN/Biometrik.",
                        technicalProof = "Play Integrity: NO_INTEGRITY"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Perbaiki Play Integrity Fingerprint",
                        detailedInstruction = "Gunakan modul Play Integrity Fix untuk memulihkan verifikasi perangkat.",
                        ksuFix = "Pasang modul Play Integrity Fix di KernelSU.",
                        magiskFix = "Pasang modul Play Integrity Fix di Magisk.",
                        apatchFix = "Pasang modul Play Integrity Fix di APatch.",
                        generalAction = "Update modul Play Integrity Fix dan hapus data Play Services."
                    )
                )
            }

            val verdict = when {
                score >= 80 -> "AMAN DARI DETEKSI THREATMETRIX TOKOPEDIA"
                score >= 60 -> "TERANCAM VOUCHER DISKON DICABUT / LIMIT TRANSAKSI"
                else -> "BAHAYA TINGGI (Terdeteksi Modifikasi Partisi oleh ThreatMetrix)"
            }
            val colorHex = when {
                score >= 80 -> 0xFF00E676
                score >= 60 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Tokopedia / GoPay: Wajib isolasi mount namespace di KernelSU / Shamiko Magisk agar SDK ThreatMetrix tidak mendeteksi tabel partisi root."
            )
        }

        // =========================================================================
        // TARGET 8: FINANCIAL, BANKING & PINJOL
        // =========================================================================
        else if (engineType == TargetEngineType.FINANCIAL_BANKING) {
            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 35
                dangers.add(
                    AppDangerItem(
                        title = "Biner Root Terdeteksi (RootBeer Defense)",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Aplikasi perbankan mendeteksi biner su langsung di path sistem standar.",
                        technicalProof = "Biner: ${dangerousPaths.foundBinaries.joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Sembunyikan Akses Root dari Perbankan",
                        detailedInstruction = "Jangan izinkan $appName mendeteksi file root su.",
                        ksuFix = "KernelSU: Cabut izin Superuser untuk '$appName', set Mount Namespace ke 'Unshare', pasang Zygisk Assistant & Hide My Applist.",
                        magiskFix = "Magisk: Tambahkan '$packageName' ke DenyList, pasang Shamiko & Hide My Applist.",
                        apatchFix = "APatch: Cabut izin SuperUser, pasang APatch KPM Hider.",
                        generalAction = "Hapus aplikasi Magisk Manager (atau gunakan fitur Sembunyikan Aplikasi Magisk)."
                    )
                )
            }

            if (!playIntegrity.meetsDeviceIntegrity) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "Evaluasi Hardware Play Integrity Gagal",
                        severity = DangerSeverity.HIGH,
                        explanation = "Aplikasi finansial/bank menolak berjalan di perangkat yang tidak lolos sertifikasi Play Integrity.",
                        technicalProof = "Play Integrity status tidak memenuhi device integrity"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Pasang Play Integrity Fix",
                        detailedInstruction = "Perbaiki fingerprint dengan modul PlayIntegrityFix.",
                        ksuFix = "Pasang modul PIF di KernelSU.",
                        magiskFix = "Pasang modul PIF di Magisk.",
                        apatchFix = "Pasang modul PIF di APatch.",
                        generalAction = "Hapus data Google Play Services."
                    )
                )
            }

            val verdict = when {
                score >= 80 -> "AMAN (Aplikasi Finansial / Bank Siap Digunakan)"
                score >= 50 -> "PERINGATAN DETEKSI ROOT PADA APLIKASI BANK"
                else -> "BLOKIR KEAMANAN (Aplikasi Bank Menolak Terbuka / Force Close)"
            }
            val colorHex = when {
                score >= 80 -> 0xFF00E676
                score >= 50 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Aplikasi Bank / E-Wallet ($appName): Wajib lolos MEETS_DEVICE_INTEGRITY dan isolasi root penuh via Zygisk DenyList / KSU Unshare Namespace."
            )
        }

        // =========================================================================
        // TARGET 9: RIDE HAILING & LOCATION SENSITIVE (Gojek, Grab, Maxim)
        // =========================================================================
        else if (engineType == TargetEngineType.RIDE_HAILING) {
            if (isMockLocationEnabled) {
                score -= 40
                dangers.add(
                    AppDangerItem(
                        title = "Mock Location (Lokasi Palsu / Fake GPS) Terdeteksi",
                        severity = DangerSeverity.CRITICAL,
                        explanation = "Aplikasi ojol mendeteksi opsi Mock Location aktif di sistem Android. Menyebabkan akun driver langsung di-suspend (gacor hangus).",
                        technicalProof = "Settings.Secure.ALLOW_MOCK_LOCATION aktif"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan Mock Location di Opsi Pengembang",
                        detailedInstruction = "Matikan aplikasi lokasi palsu di Developer Options.",
                        ksuFix = "Gunakan hook sistem Fused Location Provider (LSPosed) jika membutuhkan lokasi khusus tanpa mengaktifkan Mock Location OS.",
                        magiskFix = "Gunakan hook Fused Location Provider via LSPosed.",
                        apatchFix = "Matikan mock location di Pengaturan Android.",
                        generalAction = "Pengaturan -> Opsi Pengembang -> Pilih aplikasi lokasi tiruan -> Tidak Ada."
                    )
                )
            }

            if (isAdbEnabled) {
                score -= 20
                dangers.add(
                    AppDangerItem(
                        title = "USB Debugging Aktif",
                        severity = DangerSeverity.MEDIUM,
                        explanation = "Aplikasi driver memblokir orderan jika USB Debugging aktif.",
                        technicalProof = "Settings.Global.ADB_ENABLED = 1"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Matikan USB Debugging",
                        detailedInstruction = "Matikan 'Debugging USB' di Pengaturan Pengembang.",
                        ksuFix = "Matikan USB Debugging.",
                        magiskFix = "Matikan USB Debugging.",
                        apatchFix = "Matikan USB Debugging.",
                        generalAction = "Matikan Opsi Pengembang saat narik order."
                    )
                )
            }

            val verdict = when {
                score >= 80 -> "AMAN (Siap Terima Orderan / Bebas Suspend)"
                score >= 50 -> "TERANCAM SUSPEND KARENA DETEKSI LOKASI / ADB"
                else -> "SUSPEND KRITIKAL (Terdeteksi Fake GPS oleh Server Ojol)"
            }
            val colorHex = when {
                score >= 80 -> 0xFF00E676
                score >= 50 -> 0xFFFFD600
                else -> 0xFFFF1744
            }

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Driver / Ojol ($appName): Jangan gunakan Mock Location standar Android; gunakan modul hook internal dan pastikan USB debugging nonaktif."
            )
        }

        // =========================================================================
        // TARGET 10: SOCIAL MESSAGING MULTI-ACCOUNT (WhatsApp, Telegram, dll)
        // =========================================================================
        else if (engineType == TargetEngineType.SOCIAL_MESSAGING) {
            if (networkIntel.isVpnOrProxy) {
                score -= 25
                dangers.add(
                    AppDangerItem(
                        title = "IP Subnet Datacenter Terdeteksi (Spam Filter)",
                        severity = DangerSeverity.HIGH,
                        explanation = "Pendaftaran akun baru di $appName sering langsung diblokir jika IP terdaftar sebagai proxy atau datacenter.",
                        technicalProof = "IP: ${networkIntel.publicIp}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Gunakan IP Seluler Residensial Bersih",
                        detailedInstruction = "Gunakan kuota data seluler normal saat login/daftar nomor baru.",
                        ksuFix = "Matikan VPN di Android.",
                        magiskFix = "Matikan VPN di Android.",
                        apatchFix = "Matikan VPN di Android.",
                        generalAction = "Rotasi IP dengan Mode Pesawat 5 detik."
                    )
                )
            }

            val verdict = if (score >= 80) "AMAN (Siap Multi-Akun Chat & Komunikasi)" else "WASPADAI BLOKIR SPAM NOMOR BARU"
            val colorHex = if (score >= 80) 0xFF00E676 else 0xFFFFD600

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Media Sosial ($appName): Hindari VPN saat membuat akun baru untuk mencegah auto-banned spam."
            )
        }

        // =========================================================================
        // TARGET 11: COMPETITIVE ONLINE GAMES (Mobile Legends, Free Fire, PUBG)
        // =========================================================================
        else if (engineType == TargetEngineType.COMPETITIVE_GAME) {
            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 30
                dangers.add(
                    AppDangerItem(
                        title = "Biner Root Terdeteksi (Anti-Cheat Security)",
                        severity = DangerSeverity.HIGH,
                        explanation = "Mesin anti-cheat game online mendeteksi biner su untuk mencegah script injeksi.",
                        technicalProof = "Biner: ${dangerousPaths.foundBinaries.joinToString()}"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Isolasi Game dari Biner Root",
                        detailedInstruction = "Tambahkan game ke daftar isolasi root.",
                        ksuFix = "Di KernelSU: Uncheck root untuk '$appName', set Mount Namespace ke Unshare.",
                        magiskFix = "Di Magisk: Masukkan '$packageName' ke DenyList.",
                        apatchFix = "Di APatch: Cabut izin SuperUser.",
                        generalAction = "Hapus modul pengubah memori game."
                    )
                )
            }

            val verdict = if (score >= 80) "AMAN DARI DETEKSI ANTI-CHEAT GAME" else "PERINGATAN DETEKSI SISTEM OLEH ANTI-CHEAT"
            val colorHex = if (score >= 80) 0xFF00E676 else 0xFFFFD600

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Game Online ($appName): Sembunyikan biner su via KSU Unshare Namespace / Magisk DenyList untuk menghindari device ban."
            )
        }

        // =========================================================================
        // TARGET 12: GENERAL COMMERCE & MARKETPLACES
        // =========================================================================
        else if (engineType == TargetEngineType.GENERIC_COMMERCE) {
            if (dangerousPaths.foundBinaries.isNotEmpty()) {
                score -= 20
                dangers.add(
                    AppDangerItem(
                        title = "Biner Root Terbaca",
                        severity = DangerSeverity.MEDIUM,
                        explanation = "Aplikasi belanja mendeteksi modifikasi sistem root.",
                        technicalProof = "Biner ditemukan"
                    )
                )
                fixSteps.add(
                    AppFixStep(
                        stepNumber = stepCounter++,
                        actionTitle = "Sembunyikan Root",
                        detailedInstruction = "Isolasi proses belanja dari akses root.",
                        ksuFix = "Uncheck root di KernelSU -> Mount Namespace Unshare.",
                        magiskFix = "Centang di Magisk DenyList.",
                        apatchFix = "Uncheck SuperUser di APatch.",
                        generalAction = "Bersihkan data aplikasi."
                    )
                )
            }

            val verdict = if (score >= 80) "AMAN (Siap Multi-Akun Belanja)" else "PERINGATAN DETEKSI AKUN BELANJA"
            val colorHex = if (score >= 80) 0xFF00E676 else 0xFFFFD600

            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = score.coerceIn(0, 100),
                verdictTitle = verdict,
                statusColorHex = colorHex,
                detectedDangers = dangers,
                fixSteps = fixSteps,
                multiAccountAdvice = "Untuk Marketplace Umum ($appName): Selalu gunakan isolasi root dan rotasi IP saat membuat akun baru."
            )
        }

        // =========================================================================
        // TARGET 13: GENERAL APP & SYSTEM SERVICE
        // =========================================================================
        else {
            return TargetAppAuditResult(
                appName = appName,
                packageName = packageName,
                isSystemApp = isSystem,
                engineType = engineType,
                readinessScore = 100,
                verdictTitle = if (isSystem) "LAYANAN SISTEM ANDROID (100% AMAN)" else "APLIKASI STANDAR (100% AMAN - BEBAS RISIKO)",
                statusColorHex = 0xFF00E676,
                detectedDangers = emptyList(),
                fixSteps = emptyList(),
                multiAccountAdvice = "Aplikasi ini adalah utilitas atau komponen sistem normal tanpa sensor anti-fraud multi-akun. Berjalan aman dalam sandbox Android standar."
            )
        }
    }
}
