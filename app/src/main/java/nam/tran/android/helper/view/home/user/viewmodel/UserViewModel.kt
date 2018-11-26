package nam.tran.android.helper.view.home.user.viewmodel;

import android.app.Application
import androidx.lifecycle.MutableLiveData
import nam.tran.domain.entity.state.Resource
import tran.nam.core.viewmodel.BaseFragmentViewModel
import tran.nam.core.viewmodel.IProgressViewModel
import javax.inject.Inject

class UserViewModel @Inject internal constructor(application: Application) : BaseFragmentViewModel(application),
    IProgressViewModel {

    private val results = MutableLiveData<Resource<*>>()

    override fun resource(): Resource<*>? {
        return results.value
    }


}
