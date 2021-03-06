package com.example.amaptest

import android.content.Context
import com.google.gson.Gson
import com.polestar.repository.data.charging.StationDetail
import java.lang.Exception
import com.google.gson.reflect.TypeToken
import com.robolectric.PolicyBean
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type


object AssetsReadUtils {

    fun readBytes(context: Context, fileName: String): ByteArray? {
        val stream = context.assets.open(fileName)
        return input2OutputStream(stream)?.toByteArray()
    }

    fun readJson(context: Context, fileName: String): String? {
        return try {
            val stream = context.assets.open(fileName)
            val inputAsString = stream.bufferedReader().use { it.readText() }
            inputAsString
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun jsonToStations(json: String): List<StationDetail> {
        val listType: Type = object : TypeToken<ArrayList<StationDetail>>() {}.type
        val yourClassList: List<StationDetail> = Gson().fromJson(json, listType)
        return yourClassList
    }

    fun mockStation(context: Context, fileName: String): List<StationDetail>? {
        readJson(context, fileName)?.let {
            return jsonToStations(it)
        }
        return null
    }

    fun mockPolicy(context: Context, fileName: String?=null): PolicyBean? {
        readJson(context, "policy.json")?.let {
            val listType: Type = object : TypeToken<PolicyBean>() {}.type
            val data: PolicyBean = Gson().fromJson(it, listType)
            return data
        }
        return null
    }

    fun input2OutputStream(`is`: InputStream?): ByteArrayOutputStream? {
        return if (`is` == null) null else try {
            val os = ByteArrayOutputStream()
            val b = ByteArray(8192)
            var len: Int
            while (`is`.read(b, 0, 8192).also { len = it } != -1) {
                os.write(b, 0, len)
            }
            os
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}