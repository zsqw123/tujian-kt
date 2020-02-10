package io.nichijou.tujian

import io.nichijou.tujian.ui.about.*
import io.nichijou.tujian.ui.archive.*
import io.nichijou.tujian.ui.bing.*
import io.nichijou.tujian.ui.history.*
import io.nichijou.tujian.ui.today.*
import io.nichijou.tujian.ui.upload.*
import org.koin.android.ext.koin.*
import org.koin.androidx.viewmodel.dsl.*
import org.koin.dsl.*

val normalModule = module {
  viewModel { TodayViewModel(androidApplication(), get(), get()) }
  viewModel { UploadViewModel(get(), get()) }
  viewModel { HistoryViewModel(get()) }
  viewModel { AboutViewModel(androidApplication(), get()) }
  viewModel { ArchiveViewModel(get(), get()) }
  viewModel { ListViewModel(get(), get()) }
  viewModel { BingViewModel(get(), get()) }
}
