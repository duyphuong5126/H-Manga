package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.Logger
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
    private val logger = Logger("LogAnalyticsEventUseCase")

    override fun execute(eventName: String, vararg eventParams: AnalyticsParam): Completable {
        return Completable.fromCallable {
            AnalyticsEvent.Companion.Builder(analyticsPusher)
                .setEventName(eventName)
                .putParam(*eventParams)
                .build()
                .push()

            val log = if (eventParams.isNotEmpty()) {
                eventParams.joinToString(separator = "\n", transform = {
                    String.format("%s - %s", it.paramName, it.paramValue)
                })
            } else ""
            logger.d("Event: $eventName\n$log")
        }
    }
}
