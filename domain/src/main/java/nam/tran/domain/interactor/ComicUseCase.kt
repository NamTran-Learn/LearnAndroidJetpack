package nam.tran.domain.interactor

import android.arch.lifecycle.LiveData
import nam.tran.domain.IRepository
import nam.tran.domain.entity.ComicEntity
import nam.tran.domain.entity.state.Resource
import javax.inject.Inject

class ComicUseCase @Inject internal constructor(val iRepository: IRepository) {

    fun getComic(offset : Int = 0,typeLoading : Int): LiveData<Resource<List<ComicEntity>>>{
        return iRepository.getComic(offset,typeLoading = typeLoading)
    }

}