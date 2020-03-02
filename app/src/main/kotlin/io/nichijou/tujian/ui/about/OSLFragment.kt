package io.nichijou.tujian.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.nichijou.tujian.R
import io.nichijou.tujian.common.ext.addNew
import io.nichijou.tujian.common.ext.openUrl
import io.nichijou.tujian.common.ext.with
import io.nichijou.tujian.ext.target
import kotlinx.android.synthetic.main.fragment_osl.*
import kotlinx.android.synthetic.main.item_osl.view.*
import me.yokeyword.fragmentation.SupportFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class OSLFragment : SupportFragment() {
  private val oslViewModel by viewModel<OSLViewModel>()
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_osl, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    osl_recycler_view.layoutManager = LinearLayoutManager(target(), RecyclerView.VERTICAL, false)
    osl_recycler_view.with<OSL>(layoutRes = R.layout.item_osl, viewTypeMatching = { _, _ -> true }) { b, _ ->
      name.text = b.name
      license.text = b.license
      license.setOnClickListener { v ->
        v.context.openUrl(b.licenseUrl)
      }
      desc.text = b.desc
      setOnClickListener { v ->
        v.context.openUrl(b.website)
      }
    }
    oslViewModel.getOSL().observe(viewLifecycleOwner, Observer {
      if (!it.isNullOrEmpty()){
        osl_recycler_view.addNew(it)
      }
    })
  }

  override fun onBackPressedSupport(): Boolean {
    pop()
    return true
  }
}


