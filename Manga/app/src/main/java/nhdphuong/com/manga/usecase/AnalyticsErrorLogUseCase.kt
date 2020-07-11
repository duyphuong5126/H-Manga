package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.Constants.Companion.EVENT_EXCEPTION
import nhdphuong.com.manga.Constants.Companion.EXCEPTION_CANONICAL_CLASS_NAME
import nhdphuong.com.manga.Constants.Companion.EXCEPTION_CAUSE
import nhdphuong.com.manga.Constants.Companion.EXCEPTION_MESSAGE
import nhdphuong.com.manga.Constants.Companion.EXCEPTION_LOCALIZED_MESSAGE
import nhdphuong.com.manga.Constants.Companion.EXCEPTION_STACK_TRACE
import nhdphuong.com.manga.analytics.AnalyticsEvent
import nhdphuong.com.manga.analytics.AnalyticsPusher
import javax.inject.Inject

interface AnalyticsErrorLogUseCase {
    fun execute(throwable: Throwable): Completable
}

class AnalyticsErrorLogUseCaseImpl @Inject constructor(
    private val analyticsPusher: AnalyticsPusher
) : AnalyticsErrorLogUseCase {
    override fun execute(throwable: Throwable): Completable {
        return Completable.fromCallable {
            var stackTraceString = ""
            throwable.stackTrace.forEach {
                stackTraceString += "$it\n"
            }
            AnalyticsEvent.Companion.Builder(analyticsPusher)
                .setEventName(EVENT_EXCEPTION)
                .putParam(
                    EXCEPTION_CANONICAL_CLASS_NAME,
                    throwable.javaClass.canonicalName.orEmpty()
                )
                .putParam(EXCEPTION_CAUSE, throwable.cause.toString())
                .putParam(EXCEPTION_MESSAGE, throwable.message.orEmpty())
                .putParam(EXCEPTION_LOCALIZED_MESSAGE, throwable.localizedMessage.orEmpty())
                .putParam(EXCEPTION_STACK_TRACE, stackTraceString)
                .build()
                .push()
        }
    }
}
