package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.Constants.Companion.EVENT_EXCEPTION
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_EXCEPTION_CANONICAL_CLASS_NAME
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_EXCEPTION_CAUSE
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_EXCEPTION_MESSAGE
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_EXCEPTION_LOCALIZED_MESSAGE
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_EXCEPTION_STACK_TRACE
import nhdphuong.com.manga.analytics.AnalyticsEvent
import nhdphuong.com.manga.analytics.AnalyticsPusher
import javax.inject.Inject

interface LogAnalyticsErrorUseCase {
    fun execute(throwable: Throwable): Completable
}

class LogAnalyticsErrorUseCaseImpl @Inject constructor(
    private val analyticsPusher: AnalyticsPusher
) : LogAnalyticsErrorUseCase {
    override fun execute(throwable: Throwable): Completable {
        return Completable.fromCallable {
            var stackTraceString = ""
            throwable.stackTrace.forEach {
                stackTraceString += "$it\n"
            }
            AnalyticsEvent.Companion.Builder(analyticsPusher)
                .setEventName(EVENT_EXCEPTION)
                .putParam(
                    PARAM_NAME_EXCEPTION_CANONICAL_CLASS_NAME,
                    throwable.javaClass.canonicalName.orEmpty()
                )
                .putParam(PARAM_NAME_EXCEPTION_CAUSE, throwable.cause.toString())
                .putParam(PARAM_NAME_EXCEPTION_MESSAGE, throwable.message.orEmpty())
                .putParam(PARAM_NAME_EXCEPTION_LOCALIZED_MESSAGE, throwable.localizedMessage.orEmpty())
                .putParam(PARAM_NAME_EXCEPTION_STACK_TRACE, stackTraceString)
                .build()
                .push()
        }
    }
}
