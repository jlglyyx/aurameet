package com.chat.lib_common.util

import android.net.Uri
import android.util.Log
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.blankj.utilcode.util.UriUtils
import com.chat.lib_common.app.BaseApplication
import com.chat.lib_common.data.OssData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OSSUtil {


    private const val TAG = "OSSUtil"

    private val endpoint = "oss-us-east-1.aliyuncs.com"

    private val bucket = "julan-as"

    const val AVATAR = "headPic"

    const val COVER = "cover"

    const val ALBUM = "album"

    const val SYSTEM = "system"

    const val VIDEO = "vio"

    const val PICTURE = "pic"



    const val UPLOAD_STATUS_NORMAL = 0

    const val UPLOAD_STATUS_LOADING = 1

    const val UPLOAD_STATUS_SUCCESS = 2

    const val UPLOAD_STATUS_ERROR = 3





    fun uploadPicture(
        mOssData: OssData?,
        type: String? = COVER,
        mFileUri: Uri?,
        onSuccess: (String?, String?) -> Unit = { _, _ -> },
        onError: (String?) -> Unit = {}
    ) {

        if (null == mOssData || null == mFileUri) {

            onError(null)

            return
        }

        val credentialProvider = OSSStsTokenCredentialProvider(
            mOssData.AccessKeyId,
            mOssData.AccessKeySecret,
            mOssData.SecurityToken
        )


        val mOSSClient =
            OSSClient.Builder().context(BaseApplication.mApplication).endpoint(endpoint)
                .credentialsProvider(credentialProvider).build()

        mOSSClient.asyncPutObject(
            PutObjectRequest(
                bucket,
                "$type/${System.currentTimeMillis()}${(Math.random() * 1000).toInt()}.jpg",
                CompressUtil.compressImage(mFileUri, BaseApplication.mApplication)
            ), object :
                OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {

                    onSuccess(request?.objectKey, mOssData.uploadId)

                    Log.i(TAG, "onSuccess: ${request?.toJson()} \n  ${result?.toJson()}")

                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientException: ClientException?,
                    serviceException: ServiceException?
                ) {

                    onError(mOssData.uploadId)
                    Log.i(TAG, "onFailure: ${request?.toJson()}")
                }

            })

    }


    fun uploadVideo(
        mOssData: OssData?,
        type: String,
        mFileUri: Uri?,
        onSuccess: (String?, String?) -> Unit = { _, _ -> },
        onError: (String?) -> Unit = {}
    ) {

        if (null == mOssData || null == mFileUri) {

            onError(null)

            return
        }

        val credentialProvider = OSSStsTokenCredentialProvider(
            mOssData.AccessKeyId,
            mOssData.AccessKeySecret,
            mOssData.SecurityToken
        )


        val mOSSClient =
            OSSClient.Builder().context(BaseApplication.mApplication).endpoint(endpoint)
                .credentialsProvider(credentialProvider).build()

        mOSSClient.asyncPutObject(
            PutObjectRequest(
                bucket,
                "$type/${System.currentTimeMillis()}${(Math.random() * 1000).toInt()}.mp4",
                mFileUri
            ), object :
                OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {

                    onSuccess(request?.objectKey, mOssData.uploadId)

                    Log.i(TAG, "onSuccess: ${request?.toJson()} \n  ${result?.toJson()}")

                }

                override fun onFailure(
                    request: PutObjectRequest?,
                    clientException: ClientException?,
                    serviceException: ServiceException?
                ) {

                    onError(mOssData.uploadId)
                    Log.i(TAG, "onFailure: ${request?.toJson()}")
                }

            })

    }




    fun uploadVideoWithCover(
        mOssData: OssData?,
        mFileUri: Uri?,
        onSuccess: (String?,String?, String?,Long) -> Unit = { _, _,_,_ -> },
        onError: (String?) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val videoDeferred = async {
                    uploadVideoAndImage(mOssData, VIDEO, mFileUri)

                }

                val coverDeferred = async {
                    uploadVideoAndImage(mOssData, ALBUM, mFileUri)
                }
                val videoResult = videoDeferred.await()

                val coverResult = coverDeferred.await()


                onSuccess(videoResult,coverResult,mOssData?.uploadId, CompressUtil.getVideoDuration(BaseApplication.mApplication,mFileUri))

                Log.i(TAG, "uploadVideoWithCover: $videoResult  $coverResult")

            } catch (e: Exception) {
                e.printStackTrace()

                onError(mOssData?.uploadId)
            }
        }
    }



    private suspend fun uploadVideoAndImage(
        mOssData: OssData?,
        type: String,
        mFileUri: Uri?,
    ): String? {

        if (null == mOssData || null == mFileUri) {

            return null
        }

        val credentialProvider = OSSStsTokenCredentialProvider(
            mOssData.AccessKeyId,
            mOssData.AccessKeySecret,
            mOssData.SecurityToken
        )


        val mOSSClient =
            OSSClient.Builder().context(BaseApplication.mApplication).endpoint(endpoint)
                .credentialsProvider(credentialProvider).build()

        val tt = if (type == VIDEO){
            "mp4"
        }else{
            "jpg"
        }

        val file = if (type == VIDEO){
            UriUtils.uri2Bytes(mFileUri)
        }else{

            if (null == CompressUtil.getVideoThumbnail(BaseApplication.mApplication,mFileUri)) UriUtils.uri2Bytes(mFileUri) else  CompressUtil.compressImage(CompressUtil.getVideoThumbnail(BaseApplication.mApplication,mFileUri)!!)

        }

        return suspendCancellableCoroutine { continuation ->

            mOSSClient.asyncPutObject(
                PutObjectRequest(
                    bucket,
                    "$type/${System.currentTimeMillis()}${(Math.random() * 1000).toInt()}.${tt}",
                    file
                ), object :
                    OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {

                        Log.i(TAG, "onSuccess: ${request?.toJson()} \n  ${result?.toJson()}")

                        continuation.resume(request?.objectKey)

                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientException: ClientException?,
                        serviceException: ServiceException?
                    ) {

                        Log.i(TAG, "onFailure: ${request?.toJson()}")
                        continuation.resume(null)
                    }

                })

        }

    }

}