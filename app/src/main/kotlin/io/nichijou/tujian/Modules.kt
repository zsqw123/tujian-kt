package io.nichijou.tujian

import io.nichijou.tujian.ui.about.AboutViewModel
import io.nichijou.tujian.ui.about.OSLViewModel
import io.nichijou.tujian.ui.archive.ArchiveViewModel
import io.nichijou.tujian.ui.archive.ListViewModel
import io.nichijou.tujian.ui.bing.BingViewModel
import io.nichijou.tujian.ui.history.HistoryViewModel
import io.nichijou.tujian.ui.today.TodayViewModel
import io.nichijou.tujian.ui.upload.UploadViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val normalModule = module {
  viewModel { TodayViewModel(androidApplication(), get(), get()) }
  viewModel { UploadViewModel(get()) }
  viewModel { HistoryViewModel(get()) }
  viewModel { AboutViewModel(androidApplication(), get()) }
  viewModel { ArchiveViewModel(get(), get()) }
  viewModel { ListViewModel(get(), get()) }
  viewModel { BingViewModel(get(), get()) }
  viewModel { OSLViewModel(androidApplication()) }
}
