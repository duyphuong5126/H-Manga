package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.analytics.AnalyticsEvent
import nhdphuong.com.manga.analytics.AnalyticsParam
import nhdphuong.com.manga.analytics.AnalyticsPusher
import javax.inject.Inject

interface LogAnalyticsEventUseCase {
    fun execute(eventName: String, vararg eventParams: AnalyticsParam): Completable
}

class LogAnalyticsEventUseCaseImpl @Inject constructor(
    private val analyticsPusher: AnalyticsPusher
) : LogAnalyticsEventUseCase {
    override fun execute(eventName: String, vararg eventParams: AnalyticsParam): Completable {
        return Completable.fromCallable {
            AnalyticsEvent.Companion.Builder(analyticsPusher)
                .setEventName(eventName)
                .putParam(*eventParams)
                .build()
                .push()
        }
    }
}
