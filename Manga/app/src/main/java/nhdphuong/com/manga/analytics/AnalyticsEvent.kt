package nhdphuong.com.manga.analytics

import android.os.Bundle

class AnalyticsEvent(
    private val analyticsPusher: AnalyticsPusher,
    private val name: String,
    private val params: Bundle?
) {
    fun push() {
        if (params == null) {
            analyticsPusher.logEvent(name)
        } else {
            analyticsPusher.logEvent(name, params)
        }
    }

    companion object {
        class Builder(private val analyticsPusher: AnalyticsPusher) {
            private var eventName = ""
            private val paramMap = HashMap<String, String>()

            fun setEventName(eventName: String): Builder {
                this.eventName = eventName
                return this
            }

            fun putParam(paramName: String, paramValue: String): Builder {
                paramMap[paramName] = paramValue
                return this
            }

            fun putParam(vararg analyticsParams: AnalyticsParam): Builder {
                analyticsParams.forEach {
                    paramMap[it.paramName] = it.paramValue
                }
                return this
            }

            fun build(): AnalyticsEvent {
                val params = Bundle()
                paramMap.entries.forEach {
                    params.putString(it.key, it.value)
                }
                return AnalyticsEvent(analyticsPusher, eventName, params)
            }
        }
    }
}
