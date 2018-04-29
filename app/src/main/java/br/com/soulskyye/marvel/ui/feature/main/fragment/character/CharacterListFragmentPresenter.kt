package br.com.soulskyye.marvel.ui.feature.main.fragment.character

import br.com.soulskyye.marvel.data.DataManager
import br.com.soulskyye.marvel.data.model.Character
import br.com.soulskyye.marvel.data.network.RetrofitException
import br.com.soulskyye.marvel.data.network.model.CharactersResponse
import br.com.soulskyye.marvel.utils.EndlessRecyclerViewScrollListener.Companion.REQUEST_LIMIT
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CharacterListFragmentPresenter(private var view: CharacterListFragmentContract.View?,
                                     private var dataManager: DataManager) : CharacterListFragmentContract.Presenter {


    object Action {
        const val GET_CHARACTERS = 0
        const val REFRESH = 1
    }

    var list: ArrayList<Character>? = null

    private var page = 0
    private var currentAction = Action.GET_CHARACTERS

    private val compositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun start() {
        getCharacters(0)
    }

    override fun onDestroy() {
        view = null
        compositeDisposable.dispose()
    }

    override fun getCharacters(currentPage: Int){
        currentAction = Action.GET_CHARACTERS

        page = currentPage
        if(currentPage == 0) {
            view?.showLoading()
        } else {
            view?.showLoadingFooter()
        }

        val disposable = dataManager.getCharacters(REQUEST_LIMIT, currentPage*REQUEST_LIMIT)!!
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onFetchCharactersSuccess, this::onFetchCharactersError)

        compositeDisposable.add(disposable)
    }

    override fun refresh() {
        compositeDisposable.clear()

        currentAction = Action.REFRESH
        page = 0

        list = ArrayList()
        view?.clearList()

        view?.showLoading()
        val disposable = dataManager.getCharacters(REQUEST_LIMIT, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onFetchRefreshCharactersSuccess, this::onFetchCharactersError)

        compositeDisposable.add(disposable)
    }

    override fun onTryAgainClick() {
        view?.hideTryAgain()

        when (currentAction) {
            Action.GET_CHARACTERS -> {
                getCharacters(page)
            }
            Action.REFRESH -> {
                refresh()
            }
        }
    }


    /*
            Callbacks
         */
    private fun onFetchCharactersSuccess(response: CharactersResponse){
        view?.hideLoading()

        if(response.data?.results?.isNotEmpty()!!){
            if(list == null){
                list = ArrayList()
            }
            list?.addAll(response.data?.results!!)

            view?.addCharacters(response.data?.results!!)
        }
    }

    private fun onFetchCharactersError(error: Throwable){
        val exception = dataManager.asRetrofitException(error)

        if (exception.kind == RetrofitException.Kind.NETWORK && exception.message != "timeout") {
            view?.showInternetError()
        } else {
            view?.showGenericError()
        }

        view?.hideLoading()
    }

    private fun onFetchRefreshCharactersSuccess(response: CharactersResponse){
        view?.hideLoading()

        if(response.data?.results?.isNotEmpty()!!){
            list?.addAll(response.data?.results!!)

            view?.refreshCharacters(response.data?.results!!)
        }
    }

}