package io.nichijou.tujian.ui.h2

import androidx.fragment.app.*
import io.nichijou.tujian.*
import io.nichijou.tujian.base.*
import io.nichijou.tujian.ui.*

class H2Fragment : BaseFragment() {
  override fun getFragmentViewId(): Int = R.layout.fragment_h2
  private val mainViewModel by activityViewModels<MainViewModel>()
  override fun handleOnViewCreated() {

  }

}
