package br.com.soulskyye.marvel.ui.feature.main.fragment.base

import br.com.soulskyye.marvel.data.model.Character

interface BaseCharacterContract {

    interface View {
        fun startDetailActivity(character: Character?, imageView: android.view.View)
    }

    interface Presenter {
        fun onListItemClick(character: Character?, imageView: android.view.View)
        fun onFavoriteClick(character: Character)
        fun refresh()
    }

}