package app.olauncher.data

object Constants {

    object Key {
        const val FLAG = "flag"
        const val RENAME = "rename"
    }

    object Dialog {
        const val ABOUT = "ABOUT"
        const val WALLPAPER = "WALLPAPER"
        const val REVIEW = "REVIEW"
        const val RATE = "RATE"
        const val SHARE = "SHARE"
        const val HIDDEN = "HIDDEN"
        const val KEYBOARD = "KEYBOARD"
        const val DIGITAL_WELLBEING = "DIGITAL_WELLBEING"
        const val PRO_MESSAGE = "PRO_MESSAGE"
        const val SHORTCUT_MENU = "SHORTCUT_MENU"
    }

    object UserState {
        const val START = "START"
        const val WALLPAPER = "WALLPAPER"
        const val REVIEW = "REVIEW"
        const val RATE = "RATE"
        const val SHARE = "SHARE"
    }

    object DateTime {
        const val OFF = 0
        const val ON = 1
        const val DATE_ONLY = 2

        fun isTimeVisible(dateTimeVisibility: Int): Boolean {
            return dateTimeVisibility == ON
        }

        fun isDateVisible(dateTimeVisibility: Int): Boolean {
            return dateTimeVisibility == ON || dateTimeVisibility == DATE_ONLY
        }
    }

    object SwipeDownAction {
        const val SEARCH = 1
        const val NOTIFICATIONS = 2
    }

    object TextSize {
        const val ONE = 0.6f
        const val TWO = 0.75f
        const val THREE = 0.9f
        const val FOUR = 1f
        const val FIVE = 1.1f
        const val SIX = 1.2f
        const val SEVEN = 1.3f
    }

    object CharacterIndicator {
        const val SHOW = 102
        const val HIDE = 101
    }

    val CLOCK_APP_PACKAGES = arrayOf(
        "com.google.android.deskclock", //Google Clock
        "com.sec.android.app.clockpackage", //Samsung Clock
        "com.oneplus.deskclock", //OnePlus Clock
        "com.miui.clock", //Xiaomi Clock
    )

    const val WALL_TYPE_LIGHT = "light"
    const val WALL_TYPE_DARK = "dark"

    const val FLAG_LAUNCH_APP = 100
    const val FLAG_HIDDEN_APPS = 101

    const val FLAG_SET_HOME_APP_1 = 1
    const val FLAG_SET_HOME_APP_2 = 2
    const val FLAG_SET_HOME_APP_3 = 3
    const val FLAG_SET_HOME_APP_4 = 4
    const val FLAG_SET_HOME_APP_5 = 5
    const val FLAG_SET_HOME_APP_6 = 6
    const val FLAG_SET_HOME_APP_7 = 7
    const val FLAG_SET_HOME_APP_8 = 8

    const val FLAG_SET_SWIPE_LEFT_APP = 11
    const val FLAG_SET_SWIPE_RIGHT_APP = 12
    const val FLAG_SET_CLOCK_APP = 13
    const val FLAG_SET_CALENDAR_APP = 14

    const val REQUEST_CODE_ENABLE_ADMIN = 666
    const val REQUEST_CODE_LAUNCHER_SELECTOR = 678

    const val HINT_RATE_US = 15

    const val LONG_PRESS_DELAY_MS = 500L
    const val ONE_DAY_IN_MILLIS = 86400000L
    const val ONE_HOUR_IN_MILLIS = 3600000L
    const val ONE_MINUTE_IN_MILLIS = 60000L

    const val MIN_ANIM_REFRESH_RATE = 10f

    const val URL_ABOUT = "https://www.tinobritty.me"
    const val URL_PRIVACY = "https://www.tinobritty.me"
    const val URL_DOUBLE_TAP = "https://www.tinobritty.me"
    const val URL_GITHUB = "https://github.com/brittytino"
    const val URL_PLAY_STORE = "https://www.tinobritty.me"
    const val URL_PRO = "https://www.tinobritty.me"
    const val URL_DEV = "https://www.tinobritty.me"
    const val URL_WALLPAPERS = "https://gist.githubusercontent.com/tanujnotes/85e2d0343ace71e76615ac346fbff82b/raw"
    const val URL_DUCK_SEARCH = "https://duck.co/?q="
    const val URL_OLAUNCHER_PRO = "https://play.google.com/store/apps/details?id=app.olauncher.pro"
    const val URL_DEFAULT_LIGHT_WALLPAPER = "https://images.unsplash.com/photo-1554147090-e1221a04a025?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&dl=bevis-chen-748958-unsplash.jpg"
    const val URL_DEFAULT_DARK_WALLPAPER = "https://images.unsplash.com/photo-1506318137071-a8e063b4bcc0?ixlib=rb-1.2.1&q=80&fm=jpg&crop=entropy&cs=tinysrgb&dl=felipe-pelaquim-605151-unsplash.jpg"


    const val DIGITAL_WELLBEING_PACKAGE_NAME = "com.google.android.apps.wellbeing"
    const val DIGITAL_WELLBEING_ACTIVITY = "com.google.android.apps.wellbeing.settings.TopLevelSettingsActivity"
    const val DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME = "com.samsung.android.forest"
    const val DIGITAL_WELLBEING_SAMSUNG_ACTIVITY = "com.samsung.android.forest.launcher.LauncherActivity"
    const val WALLPAPER_WORKER_NAME = "WALLPAPER_WORKER_NAME"
}