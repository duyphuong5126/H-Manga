package nhdphuong.com.manga.supports

import android.text.TextUtils
import com.google.gson.GsonBuilder
import nhdphuong.com.manga.gson.GsonUTCDateAdapter
import nhdphuong.com.manga.gson.SerializationExclusionStrategy
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

/*
 * Created by nhdphuong on 3/24/18.
 */
object ServiceGenerator {
    private val mRetrofitBuilder = Retrofit.Builder().addConverterFactory(
        GsonConverterFactory.create(
            GsonBuilder()
                .addSerializationExclusionStrategy(SerializationExclusionStrategy())
                .registerTypeAdapter(Date::class.java, GsonUTCDateAdapter())
                .create()
        )
    )

    private var mRetrofit: Retrofit? = null
    private val mLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val mHttpClientBuilder = OkHttpClient.Builder().addInterceptor(mLoggingInterceptor)
        .connectTimeout(30000, TimeUnit.MILLISECONDS)
        .readTimeout(30000, TimeUnit.MILLISECONDS)
        .writeTimeout(30000, TimeUnit.MILLISECONDS)
    private var mBaseUrl: String? = null

    fun setBaseUrl(baseUrl: String) {
        if (!TextUtils.equals(mBaseUrl, baseUrl)) {
            mBaseUrl = baseUrl
            mRetrofitBuilder.baseUrl(mBaseUrl!!)
            mRetrofit = mRetrofitBuilder.build()
        }
    }

    fun setInterceptor(interceptor: Interceptor?) {
        mHttpClientBuilder.interceptors().clear()
        mHttpClientBuilder.addInterceptor(mLoggingInterceptor)
        if (interceptor != null && !mHttpClientBuilder.interceptors().contains(interceptor)) {
            mHttpClientBuilder.addInterceptor(interceptor)
        }
        mRetrofitBuilder.client(mHttpClientBuilder.build())
        mRetrofit = mRetrofitBuilder.build()
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return mRetrofit!!.create(serviceClass)
    }
}
