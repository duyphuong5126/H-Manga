package nhdphuong.com.manga.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nhdphuong.com.manga.views.uimodel.RoutingModel

class ExternalRoutingViewModel : ViewModel() {
    private val _routingModel = MutableLiveData<RoutingModel>()

    val routingModel: LiveData<RoutingModel> = _routingModel

    fun navigate(routingModel: RoutingModel) {
        _routingModel.postValue(routingModel)
    }
}