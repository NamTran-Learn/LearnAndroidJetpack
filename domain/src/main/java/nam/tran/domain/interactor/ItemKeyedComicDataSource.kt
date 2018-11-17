package nam.tran.domain.interactor

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import nam.tran.domain.entity.BaseItemKey
import nam.tran.domain.entity.ComicEntity
import nam.tran.domain.entity.state.Loading
import nam.tran.domain.entity.state.Resource
import nam.tran.domain.mapper.DataEntityMapper
import nam.tran.flatform.IApi
import nam.tran.flatform.model.response.ComicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tran.nam.util.Logger
import java.util.concurrent.Executor

class ItemKeyedComicDataSource(
    private val iApi: IApi,
    private val dataEntityMapper: DataEntityMapper,
    private val retryExecutor: Executor,
    private val convert: (List<ComicEntity>) -> List<BaseItemKey>
) : ItemKeyedDataSource<Int, BaseItemKey>() {

    // keep a function reference for the retry event
//    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<Resource<BaseItemKey>>()

//    fun retryAllFailed() {
//        val prevRetry = retry
//        retry = null
//        prevRetry?.let {
//            retryExecutor.execute {
//                it.invoke()
//            }
//        }
//    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<BaseItemKey>) {
        Logger.debug("Paging Learn ItemKeyed", "loadInitial")
        networkState.postValue(Resource.loading(null, Loading.LOADING_NORMAL))
        iApi.getComicPaging2(limit = params.requestedLoadSize).enqueue(object : Callback<ComicResponse> {
            override fun onFailure(call: Call<ComicResponse>, t: Throwable) {
                networkState.postValue(
                    Resource.error(t.message ?: "unknown err",null,Loading.LOADING_NORMAL,retry = {
                        loadInitial(params, callback)
                    })
                )
            }

            override fun onResponse(
                call: Call<ComicResponse>,
                response: Response<ComicResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.success!!) {
                        val data = result.result
//                        retry = null
                        networkState.postValue(Resource.success(null, Loading.LOADING_NORMAL))
                        callback.onResult(convert(dataEntityMapper.comicEntityMapper.transform(data)))
                    } else {
                        networkState.postValue(
                            Resource.error("error code: ${result.message}",null,Loading.LOADING_NORMAL,retry = {
                                loadInitial(params, callback)
                            })
                        )
                    }
                } else {
                    networkState.postValue(
                        Resource.error("error code: ${response.code()}",null,Loading.LOADING_NORMAL,retry = {
                            loadInitial(params, callback)
                        })
                    )
                }
            }

        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<BaseItemKey>) {
        Logger.debug("Paging Learn ItemKeyed", "loadAfter")
        networkState.postValue(Resource.loadingPaging(null, Loading.LOADING_NORMAL))
        iApi.getComicPaging2(params.key, params.requestedLoadSize).enqueue(object : Callback<ComicResponse> {
            override fun onFailure(call: Call<ComicResponse>, t: Throwable) {
                networkState.postValue(Resource.errorPaging(t.message ?: "unknown err",null,Loading.LOADING_NORMAL,retry = {
                    loadAfter(params, callback)
                }))
            }

            override fun onResponse(
                call: Call<ComicResponse>,
                response: Response<ComicResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.success!!) {
                        val data = result.result
//                        retry = null
                        networkState.postValue(Resource.successPaging(null, Loading.LOADING_NORMAL))
                        callback.onResult(convert(dataEntityMapper.comicEntityMapper.transform(data)))
                    } else {
                        networkState.postValue(
                            Resource.errorPaging("error code: ${result.message}",null,Loading.LOADING_NORMAL,retry = {
                                loadAfter(params, callback)
                            })
                        )
                    }
                } else {
                    networkState.postValue(
                        Resource.errorPaging("error code: ${response.code()}",null,Loading.LOADING_NORMAL,retry = {
                            loadAfter(params, callback)
                        })
                    )
                }
            }

        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<BaseItemKey>) {
        // ignored, since we only ever append to our initial load
        Logger.debug("Paging Learn ItemKeyed", "loadBefore")
    }

    override fun getKey(item: BaseItemKey): Int {
        return item.idKey
    }
}