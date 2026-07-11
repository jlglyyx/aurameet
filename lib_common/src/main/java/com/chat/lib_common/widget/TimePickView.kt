package com.chat.lib_common.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.graphics.toColorInt
import com.bigkoo.pickerview.adapter.ArrayWheelAdapter
import com.chat.lib_common.databinding.ViewTimePickBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimePickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    private val TAG = "TimePickView"

    private var cameraDistance = 20f

    private val mViewTimePickBinding by lazy {

        ViewTimePickBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -100) }

    private val maxDate = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }

    private var mYearData = mutableListOf<Int>()

    private var mMonthData = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private var mDayData = mutableListOf<Int>()

    private lateinit var mYearAdapter: ArrayWheelAdapter<Int>

    private lateinit var mMonthAdapter: ArrayWheelAdapter<String>

    private lateinit var mDayAdapter: ArrayWheelAdapter<Int>

    private var mCurrentYear = 0

    private var mCurrentMonth = 0

    private var mCurrentDay = 0


    var onTimeChange: (Date, Int) -> Unit = { _, _ -> }

    private val mTextColorCenter = "#ffffff".toColorInt()

    private val mTextColorOut = "#666666".toColorInt()


    init {


        createYearData()
        createMonthData()
        createDayData()


        mViewTimePickBinding.wheelYear.apply {
            adapter = mYearAdapter
            setCyclic(false)
            cameraDistance = this@TimePickView.cameraDistance
            setDividerColor(Color.TRANSPARENT)
            setTextColorCenter(mTextColorCenter)
            setTextColorOut(mTextColorOut)
            currentItem = mCurrentYear
            setOnItemSelectedListener { yearIndex ->

                mCurrentYear = yearIndex

                createDayData()

                try {

                    if (mCurrentYear >= mYearData.size || mCurrentDay >= mDayData.size) return@setOnItemSelectedListener

                    calculateAge(mYearData[mCurrentYear], mCurrentMonth + 1, mDayData[mCurrentDay])
                } catch (e: Exception) {
                    e.printStackTrace()
                }


            }
        }


        mViewTimePickBinding.wheelMonth.apply {
            adapter = mMonthAdapter
            setCyclic(false)
            cameraDistance = this@TimePickView.cameraDistance
            setDividerColor(Color.TRANSPARENT)
            setTextColorCenter(mTextColorCenter)
            setTextColorOut(mTextColorOut)
            currentItem = mCurrentMonth
            setOnItemSelectedListener { monthIndex ->
                mCurrentMonth = monthIndex

                createDayData()

                try {

                    if (mCurrentYear >= mYearData.size || mCurrentDay >= mDayData.size) return@setOnItemSelectedListener

                    calculateAge(mYearData[mCurrentYear], mCurrentMonth + 1, mDayData[mCurrentDay])
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }




        mViewTimePickBinding.wheelDay.apply {
            setCyclic(false)
            cameraDistance = this@TimePickView.cameraDistance
            setDividerColor(Color.TRANSPARENT)
            setTextColorCenter(mTextColorCenter)
            setTextColorOut(mTextColorOut)
            currentItem = mCurrentDay
            setOnItemSelectedListener { dayIndex ->

                mCurrentDay = dayIndex
                try {
                    if (mCurrentYear >= mYearData.size || mCurrentDay >= mDayData.size) return@setOnItemSelectedListener

                    calculateAge(mYearData[mCurrentYear], mCurrentMonth + 1, mDayData[mCurrentDay])
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }


    private fun createYearData() {

        mYearData = (minDate.get(Calendar.YEAR)..maxDate.get(Calendar.YEAR)).toMutableList()

        mYearAdapter = ArrayWheelAdapter(mYearData)

    }

    private fun createMonthData() {

        mMonthAdapter = ArrayWheelAdapter(mMonthData)

    }

    private fun createDayData() {

        val maxDay = getMaxDay(mYearData[mCurrentYear], mCurrentMonth + 1)

        mDayData = (1..maxDay).toMutableList()

        mDayAdapter = ArrayWheelAdapter(mDayData)

        mViewTimePickBinding.wheelDay.adapter = mDayAdapter

        try {
            mViewTimePickBinding.wheelDay.currentItem = mViewTimePickBinding.wheelDay.currentItem
        } catch (e: Exception) {
            e.printStackTrace()

            mViewTimePickBinding.wheelDay.currentItem = 0
        }

    }

    private fun getMaxDay(year: Int, month: Int): Int {

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        val actualMaximum = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        Log.i(TAG, "getMaxDay: $year  ==== $month  === $actualMaximum")

        return actualMaximum
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
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

        onTimeChange(birth.time, age)

        return age
    }


    fun setCurrentTime(mCurrentTime: String) {

        try {
            val data = formatInputTime(mCurrentTime)

            val split = data.split("/")

            if (split.size == 3) {
                mCurrentMonth = (split[0].toInt() - 1).coerceAtLeast(0)
                mCurrentDay = (split[1].toInt() - 1).coerceAtLeast(0)
                mCurrentYear = mYearData.indexOfFirst { index -> index.toString() == (split[2]) }
                    .coerceAtLeast(0)
                createMonthData()
                createDayData()
                mViewTimePickBinding.wheelYear.currentItem = mCurrentYear
                mViewTimePickBinding.wheelMonth.currentItem = mCurrentMonth
                mViewTimePickBinding.wheelDay.currentItem = mCurrentDay
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    fun getCurrentAge(): Int {
        try {


            return calculateAge(mYearData[mCurrentYear], mCurrentMonth + 1, mDayData[mCurrentDay])
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return -1
    }

    fun getCurrentDate(): Date? {

        val birth = Calendar.getInstance().apply {
            set(Calendar.YEAR, mYearData[mCurrentYear])
            set(Calendar.MONTH, mCurrentMonth)
            set(Calendar.DAY_OF_MONTH, mDayData[mCurrentDay])
        }

        return birth.time
    }


    private fun formatInputTime(originalDate: String): String {

        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

        val outputFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())

        try {
            val date = inputFormat.parse(originalDate)

            date?.let {
                val convertedDate: String = outputFormat.format(date)

                return convertedDate
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return ""
    }

}