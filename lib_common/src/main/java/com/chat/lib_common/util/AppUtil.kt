package com.chat.lib_common.util

import android.R.attr.type
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorSpace.match
import android.graphics.PointF
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.NOTIFICATION_SERVICE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ToastUtils
import com.chat.lib_common.R
import com.chat.lib_common.app.BaseApplication
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hjq.shape.view.ShapeTextView
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.apply
import kotlin.collections.all
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.map
import kotlin.jvm.java
import kotlin.let
import kotlin.text.isBlank
import kotlin.text.isEmpty
import kotlin.text.isNotBlank
import kotlin.text.split
import kotlin.text.toBigDecimal
import kotlin.text.trim
import kotlin.to
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ColorUtils
import java.util.concurrent.TimeUnit
import kotlin.ranges.contains
import kotlin.text.replace


val gson: Gson = GsonBuilder().create()

val videoMimeTypes = arrayOf(
    "video/mp4",       // MP4
    "video/quicktime", // MOV
    "video/webm",      // WEBM
    "video/3gpp",      // 3GP
    "video/x-msvideo"  // AVI
)

val imageMimeTypes = arrayOf(
    "image/jpeg", // JPEG / JPG
    "image/png",  // PNG
    "image/webp", // WEBP
    "image/gif",  // GIF
    "image/bmp"   // BMP
)


fun Date.dateFormat(
    format: String = "yyyy.MM.dd HH:mm:ss",
    locale: Locale = Locale.getDefault(),
    timeZone: TimeZone? = null
): String {
    return SimpleDateFormat(format, locale).apply {

        if (null != timeZone) {
            this.timeZone = timeZone
        }

    }.format(this)
}

fun String.dateFormat(format: String = "yyyy.MM.dd HH:mm:ss",locale: Locale = Locale.getDefault(), timeZone: TimeZone? = null): Date? {

    try {
        return SimpleDateFormat(format, locale).apply {

            if (null != timeZone) {
                this.timeZone = timeZone
            }

        }.parse(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null

}


fun Date?.toZodiac(context: Context): String {
    if (this == null) return ""
    val cal = Calendar.getInstance()

    cal.time = this
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)

    return when (month) {
        1 -> context.getString(if (day < 20) R.string.capricorn else R.string.aquarius)
        2 -> context.getString(if (day < 19) R.string.aquarius else R.string.pisces)
        3 -> context.getString(if (day < 21) R.string.pisces else R.string.aries)
        4 -> context.getString(if (day < 20) R.string.aries else R.string.taurus)
        5 -> context.getString(if (day < 21) R.string.taurus else R.string.gemini)
        6 -> context.getString(if (day < 21) R.string.gemini else R.string.cancer)
        7 -> context.getString(if (day < 23) R.string.cancer else R.string.leo)
        8 -> context.getString(if (day < 24) R.string.leo else R.string.virgo)
        9 -> context.getString(if (day < 24) R.string.virgo else R.string.libra)
        10 -> context.getString(if (day < 24) R.string.libra else R.string.scorpio)
        11 -> context.getString(if (day < 23) R.string.scorpio else R.string.sagittarius)
        12 -> context.getString(if (day < 22) R.string.sagittarius else R.string.capricorn)
        else -> ""
    }
}


fun View.click(duration: Long = 1000, onClick: (v: View?) -> Unit) {

    ClickUtils.applySingleDebouncing(this, duration) { v -> onClick(v) }

}


fun showShort(text: Any) {

    if (text.toString().isBlank()) return

    ToastUtils.showShort(text.toString())
}


fun getScreenPx(context: Context): IntArray {
    val resources = context.resources
    val displayMetrics = resources.displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val heightPixels = displayMetrics.heightPixels
    return intArrayOf(widthPixels, heightPixels)
}

/**
 * @return toJson
 */
fun Any.toJson(): String {
    return gson.toJson(this)
}

/**
 * @return format Json
 */
inline fun <reified T> String.fromJson(typeOfT: Type = T::class.java): T {

    return gson.fromJson(this, typeOfT)
}

inline fun <reified T> String.formatListJson(): MutableList<T> {

    try {
        return gson.fromJson(this, object : TypeToken<MutableList<T>>() {}.type)
    } catch (e: Exception) {

        e.printStackTrace()

        return mutableListOf()
    }

}

/**
 * @return
 */
fun Float.dip2px(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this * scale + 0.5f).toInt()
}

/**
 * @return
 */
fun Float.px2dip(context: Context): Int {
    val scale = context.resources.displayMetrics.density
    return (this / scale + 0.5f).toInt()
}


fun View.edgeToEdgeAll(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }
}

fun View.edgeToEdgeTop(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
        insets
    }
}

fun View.edgeToEdgeBottom(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
        insets
    }
}



fun View.edgeToEdgeBottomAndTop(type: Int = WindowInsetsCompat.Type.systemBars()) {

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(type)
        v.setPadding(systemBars.left, 0, systemBars.right, 0)
        insets
    }
}



fun <T> Context.createIntent(clazz: Class<T>): Intent {

    return Intent(this, clazz)
}

fun <T> Fragment.createIntent(clazz: Class<T>): Intent {

    return Intent(requireContext(), clazz)
}


fun Intent.startActivity(context: Context) {

    context.startActivity(this)
}

fun Intent.startActivity(activity: Activity, finish: Boolean = false) {

    activity.startActivity(this)

    if (finish) {

        activity.finish()
    }

}



/**
 * 关闭键盘
 * @param context
 * @param window
 */
fun View.hideSoftInput(context: Context, show: Boolean = false) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (show) {
        this.requestFocus()
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    } else {
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun viewVisibility(visibility: Int, vararg array: View) {

    array.forEach {
        it.visibility = visibility
    }
}




fun getTimeSecond(second: Int, needH: Boolean = false): String {
    var h = 0
    var d = 0
    var s = 0
    val temp = second % 3600
    if (second > 3600) {
        h = second / 3600
        if (temp != 0) {
            if (temp > 60) {
                d = temp / 60
                if (temp % 60 != 0) {
                    s = temp % 60
                }
            } else {
                s = temp
            }
        }
    } else {
        d = second / 60
        if (second % 60 != 0) {
            s = second % 60
        }
    }

    if (needH) {

    } else {
        if (h == 0) {
            return needZero(d) + ":" + needZero(s)
        }
    }
    return needZero(h) + ":" + needZero(d) + ":" + needZero(s)
}

fun needZero(time: Int): String {
    if (time < 10) {
        return "0$time"
    }
    return time.toString() + ""
}


fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {

    return permissions.all { permission ->

        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

fun shouldShowPermissionRationale(activity: Activity, array: Array<String>): Boolean {

    return array.map {

        val shouldShowRequestPermissionRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)

        Log.i("TAG", "shouldShowPermissionRationale: $shouldShowRequestPermissionRationale")
        shouldShowRequestPermissionRationale
    }.all { it }


}

fun Context.hasNotificationPermission(): Boolean {

    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    return notificationManager.areNotificationsEnabled()
}

//fun Context.openNoticePermissionDetail() {
//    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//        }
//
//    } else {
//        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//            Intent.setData = "package:${packageName}".toUri()
//        }
//    }
//    startActivity(intent)
//}
//
//fun Context.openPermissionDetail() {
//
//  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//        Intent.setData = "package:${packageName}".toUri()
//    }
//    startActivity(intent)
//}

//fun Date?.toZodiac(context: Context): String {
//    if (this == null) return ""
//    val cal = Calendar.getInstance()
//
//    cal.time = this
//    val month = cal.get(Calendar.MONTH) + 1
//    val day = cal.get(Calendar.DAY_OF_MONTH)
//
//    return when (month) {
//        1 -> context.getString(if (day < 20) R.string.capricorn else R.string.aquarius)
//        2 -> context.getString(if (day < 19) R.string.aquarius else R.string.pisces)
//        3 -> context.getString(if (day < 21) R.string.pisces else R.string.aries)
//        4 -> context.getString(if (day < 20) R.string.aries else R.string.taurus)
//        5 -> context.getString(if (day < 21) R.string.taurus else R.string.gemini)
//        6 -> context.getString(if (day < 21) R.string.gemini else R.string.cancer)
//        7 -> context.getString(if (day < 23) R.string.cancer else R.string.leo)
//        8 -> context.getString(if (day < 24) R.string.leo else R.string.virgo)
//        9 -> context.getString(if (day < 24) R.string.virgo else R.string.libra)
//        10 -> context.getString(if (day < 24) R.string.libra else R.string.scorpio)
//        11 -> context.getString(if (day < 23) R.string.scorpio else R.string.sagittarius)
//        12 -> context.getString(if (day < 22) R.string.sagittarius else R.string.capricorn)
//        else -> ""
//    }
//}


fun ViewPager2.setCurrentItemWithDuration(targetItem: Int, duration: Long = 300) {
    val recyclerView = getChildAt(0) as RecyclerView
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager

    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return layoutManager.computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return duration.toFloat() / recyclerView.width
        }
    }

    smoothScroller.targetPosition = targetItem
    layoutManager.startSmoothScroll(smoothScroller)
}

//
//fun ShapeTextView.startLoadingAnimation(lastText:String): ValueAnimator {
//
//
//    val loadingAnimator = ValueAnimator.ofInt(0, 4).apply {
//        ValueAnimator.setDuration = 1000
//        repeatCount = ValueAnimator.INFINITE
//
//        addUpdateListener { animator ->
//            val progress = animator.animatedValue as Int
//            val text = when (progress) {
//                0 -> lastText
//                1 -> "${lastText}."
//                2 -> "${lastText}.."
//                else -> "${lastText}..."
//            }
//            this@startLoadingAnimation.text = text
//        }
//
//    }
//
//    this.shapeDrawableBuilder.setSolidColor(this.context.getColor(R.color.color_C5C5C5)).intoBackground()
//
//    loadingAnimator.start()
//
//    return loadingAnimator
//}


fun isVpnConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networks = connectivityManager.allNetworks

    for (network in networks) {
        val caps = connectivityManager.getNetworkCapabilities(network)
        if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return true
        }
    }
    return false
}

fun Context.copyContent(label: String, text: String) {
    val mClipboardManager: ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    mClipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))

    showShort("Successfully copied!")

}

fun isAppInForeground(): Boolean {
    return BaseApplication.isAppForeground
}


fun MutableList<String>.formatWithSymbol(symbol: String = ","): String {
    val stringBuilder = kotlin.text.StringBuilder()
    this.forEachIndexed { index, s ->
        if (index == this.size - 1) {
            stringBuilder.append(s)
        } else {
            stringBuilder.append(s).append(symbol)
        }
    }
    return stringBuilder.toString()
}


fun String.symbolToList(symbol: String = ","): MutableList<String> {

    val mutableListOf = mutableListOf<String>()
    try {
        val split = this.split(symbol)
        mutableListOf.addAll(split)
        return mutableListOf
    } catch (e: Exception) {
        return mutableListOf
    }
}


fun Context.toGoogleStore(packageName: String) {

    try {

        val playStoreUri = "https://play.google.com/store/apps/details?id=$packageName"
        val marketUri = "market://details?id=$packageName"

        val packageManager = packageManager
        val intent = Intent(Intent.ACTION_VIEW)

        val isPlayStoreInstalled = isPackageInstalled("com.android.vending", packageManager)

        if (isPlayStoreInstalled) {
            intent.data = Uri.parse(marketUri)
        } else {
            intent.data = Uri.parse(playStoreUri)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun isNextDay(key: String): Boolean {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val lastDate = getCache(key, "")
    if (lastDate.isEmpty() || currentDate != lastDate) {
        setCache(key, currentDate)
        return true
    } else {
        return false
    }

}

fun parseCurrency(value: String): Pair<String, String>? {

    try {
        val regex = Regex("""^\s*(\D+)?\s*([\d.]+)\s*(\D+)?\s*$""")
        return regex.find(value.trim())?.let {
            val unit = when {
                it.groupValues[1].isNotBlank() -> it.groupValues[1]
                it.groupValues[3].isNotBlank() -> it.groupValues[3]
                else -> ""
            }
            val number = it.groupValues[2]
            unit to number
        }
    } catch (e: Exception) {

        e.printStackTrace()
    }

    return null
}

fun formatProductDayPrice(priceStr: String?, count: Int): String? {

    if (null == priceStr) return null

    val (unit, number) = parseCurrency(priceStr) ?: return null

    val toBigDecimal = number.toBigDecimal()

    return "$unit ${toBigDecimal.divide(BigDecimal(count), 2, RoundingMode.HALF_UP)}"

}


fun cancelNotification(context: Context, id: Int? = null) {

    try {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (null != id) {
            notificationManager.cancel(id)
        } else {
            notificationManager.cancelAll()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

}




fun Lifecycle.isAlive(): Boolean {

    return this.currentState.isAtLeast(Lifecycle.State.STARTED)
}


fun calculateAge(year: Int, month: Int, day: Int): Int {
    val today = Calendar.getInstance()
    val birth = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }

    var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)

    if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
        (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))
    ) {
        age--
    }
    return age
}


fun ShapeTextView.startLoadingAnimation(lastText:String): ValueAnimator {


    val loadingAnimator = ValueAnimator.ofInt(0, 4).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE

        addUpdateListener { animator ->
            val progress = animator.animatedValue as Int
            val text = when (progress) {
                0 -> lastText
                1 -> "${lastText}."
                2 -> "${lastText}.."
                else -> "${lastText}..."
            }
            this@startLoadingAnimation.text = text
        }

    }

    this.shapeDrawableBuilder.setSolidColor("#C5C5C5".toColorInt()).intoBackground()

    loadingAnimator.start()

    return loadingAnimator
}


fun Fragment.getColor(color: Int): Int{

    return ColorUtils.getColor(color)
}


fun getLocalFormatTime(date: Date): String {

    val now = Calendar.getInstance()
    val input = Calendar.getInstance().apply { time = date }

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val yesterdayStart = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val timeFormat = if (DateFormat.is24HourFormat(BaseApplication.mApplication)) "HH:mm" else "hh:mm a"
    val timeStr = date.dateFormat(timeFormat,Locale.US)


    return when {
        // current day
        input.after(todayStart) -> {
            timeStr
        }

        // last day
        input.after(yesterdayStart) -> {
            "yesterday ${timeStr}"
        }

        // 7 day
        TimeUnit.MILLISECONDS.toDays(now.timeInMillis - input.timeInMillis) in 2..6 -> {
            val weekDay = input.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            weekDay ?: "" // fallback
        }

        // in 1 year
        input.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
            date.dateFormat("dd/MM",Locale.US)
        }

        // out 1 year
        else -> {
            date.dateFormat("dd/MM/yyyy",Locale.US)
        }
    }
}


fun getFormatMessageTime(date: Date): String {
    val now = Calendar.getInstance()
    val input = Calendar.getInstance().apply { time = date }

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val yesterdayStart = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val timeFormat = if (DateFormat.is24HourFormat(BaseApplication.mApplication)) "HH:mm" else "hh:mm a"
    val timeStr = date.dateFormat(timeFormat,Locale.US)

    return when {
        // current day
        input.after(todayStart) -> {
            timeStr
        }

        // last day
        input.after(yesterdayStart) -> {
            "yesterday ${timeStr}"
        }

        // 7 day
        TimeUnit.MILLISECONDS.toDays(now.timeInMillis - input.timeInMillis) in 2..6 -> {
            val weekDay = input.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            "$weekDay ${date.dateFormat(timeFormat,Locale.US)}" ?: "" // fallback
        }

        // in 1 year
        input.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
            date.dateFormat("dd/MM $timeFormat",Locale.US)
        }

        // out 1 year
        else -> {
            date.dateFormat("dd/MM/yyyy $timeFormat",Locale.US)
        }
    }
}


fun Context.openNoticePermissionDetail() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }

    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${packageName}".toUri()
        }
    }
    startActivity(intent)
}

fun Context.openPermissionDetail() {

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:${packageName}".toUri()
    }
    startActivity(intent)
}


fun <T> List<T>.findNextIndexLoop(
    startIndex: Int,
    predicate: (T) -> Boolean
): Int {
    if (isEmpty()) return -1
    val size = this.size

//    for (i in (startIndex + 1) until size) {
//        if (predicate(this[i])) return i
//    }
//
//    for (i in 0 until startIndex) {
//        if (predicate(this[i])) return i
//    }

    // 第一段：从 startIndex 往前找
    for (i in (startIndex - 1) downTo 0) {
        if (predicate(this[i])) return i
    }

    // 第二段：从列表末尾倒序查到 startIndex+1
    for (i in (size - 1) downTo (startIndex + 1)) {
        if (predicate(this[i])) return i
    }

    return -1
}

fun String?.replaceEmoji(): String{

    try {
        if (this.isNullOrEmpty()) return ""

        return this.replace(Regex("[^a-zA-Z0-9\\s\\p{P}]"),"")
    }catch (e: Exception){

        e.printStackTrace()
        return ""
    }


}


fun highlightContacts(textView: TextView, text: String, textColor: Int) {

    val spannable = SpannableString(text)

    val regex = Regex(
        "service@jolt-chat.com")

        val matches = regex.findAll(text)

        for (match in matches) {

            val matchedText = match.value

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {

                    textView.context.copyContent("Jolt",matchedText)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = textColor
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(
                clickableSpan,
                match.range.first,
                match.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    textView.text = spannable
    textView.movementMethod = LinkMovementMethod.getInstance()
    textView.highlightColor = Color.TRANSPARENT
}