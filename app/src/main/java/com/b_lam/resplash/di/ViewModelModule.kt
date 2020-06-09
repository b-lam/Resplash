package com.b_lam.resplash.di

import com.b_lam.resplash.ui.autowallpaper.AutoWallpaperSettingsViewModel
import com.b_lam.resplash.ui.autowallpaper.collections.AutoWallpaperCollectionViewModel
import com.b_lam.resplash.ui.autowallpaper.history.AutoWallpaperHistoryViewModel
import com.b_lam.resplash.ui.collection.add.AddCollectionViewModel
import com.b_lam.resplash.ui.collection.detail.CollectionDetailViewModel
import com.b_lam.resplash.ui.donation.DonationViewModel
import com.b_lam.resplash.ui.login.LoginViewModel
import com.b_lam.resplash.ui.main.MainViewModel
import com.b_lam.resplash.ui.photo.detail.PhotoDetailViewModel
import com.b_lam.resplash.ui.search.SearchViewModel
import com.b_lam.resplash.ui.settings.SettingsViewModel
import com.b_lam.resplash.ui.upgrade.UpgradeViewModel
import com.b_lam.resplash.ui.user.UserViewModel
import com.b_lam.resplash.ui.user.edit.EditProfileViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel { MainViewModel(get(), get(), get(), get()) }

    viewModel { PhotoDetailViewModel(get(), get()) }

    viewModel { CollectionDetailViewModel(get(), get(), get()) }

    viewModel { AddCollectionViewModel(get(), get()) }

    viewModel { SearchViewModel(get(), get(), get()) }

    viewModel { UserViewModel(get(), get(), get(), get()) }

    viewModel { EditProfileViewModel(get()) }

    viewModel { LoginViewModel(get(), get()) }

    viewModel { SettingsViewModel(androidContext()) }

    viewModel { AutoWallpaperSettingsViewModel(get()) }

    viewModel { AutoWallpaperHistoryViewModel(get()) }

    viewModel { AutoWallpaperCollectionViewModel(get(), get(), get()) }

    viewModel { UpgradeViewModel(get(), get()) }

    viewModel { DonationViewModel(get(), get()) }
}