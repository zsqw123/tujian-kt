package io.nichijou.tujian.ui.about

import android.graphics.*
import androidx.fragment.app.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.facebook.imagepipeline.request.*
import com.google.android.flexbox.*
import com.larvalabs.boo.*
import io.nichijou.oops.*
import io.nichijou.oops.ext.*
import io.nichijou.tujian.R
import io.nichijou.tujian.base.*
import io.nichijou.tujian.common.ext.*
import io.nichijou.tujian.common.fresco.*
import io.nichijou.tujian.ext.*
import io.nichijou.tujian.ui.*
import io.nichijou.utils.*
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.item_osl.view.*
import org.jetbrains.anko.support.v4.browse
import org.koin.androidx.viewmodel.ext.android.*
import kotlinx.android.synthetic.main.item_osl.view.name as oslName
import kotlinx.android.synthetic.main.item_team_core.view.avatar as coreAvatar
import kotlinx.android.synthetic.main.item_team_core.view.job as coreJob
import kotlinx.android.synthetic.main.item_team_core.view.name as coreName
import kotlinx.android.synthetic.main.item_team_leader.view.avatar as leaderAvatar
import kotlinx.android.synthetic.main.item_team_leader.view.job as leaderJob
import kotlinx.android.synthetic.main.item_team_leader.view.name as leaderName
import kotlinx.android.synthetic.main.item_team_member.view.avatar as memberAvatar
import kotlinx.android.synthetic.main.item_team_member.view.job as memberJob
import kotlinx.android.synthetic.main.item_team_member.view.name as memberName

class AboutFragment : BaseFragment() {
  companion object {
    fun newInstance() = AboutFragment()
  }

  override fun getFragmentViewId(): Int = R.layout.fragment_about
  private val mainViewModel by activityViewModels<MainViewModel>()
  private val aboutViewModel by viewModel<AboutViewModel>()

  override fun handleOnViewCreated() {
    mainViewModel.barColor.postValue(Color.TRANSPARENT)
    mainViewModel.enableScreenSaver.postValue(false)
    toolbar.setMarginTopPlusStatusBarHeight()
    toolbar.setNavigationOnClickListener { (target() as MainActivity).drawer.openMenu() }
    aboutViewModel.lastPicture.observe(this, Observer {
      if (it != null) {
        banner?.load(it.local, postprocessor = GaussianBlurPostprocessor(target(), 1f))
        ImageRequest.fromUri(it.local)?.getPalette { p ->
          if (p != null) {
            val dominant = p.dominantSwatch?.rgb ?: p.darkVibrantSwatch?.rgb
            ?: p.darkMutedSwatch?.rgb ?: 0
            addFragment(BooFragment.newInstance(dominant.isColorDark(), isIntro = false, enableFace = false,enableFuckBoo = true, enableBackground = false, creatureNum = 6), R.id.boo_wrapper)
            Oops.immed().collapsingToolbarDominantColorSet(getString(R.string.tag_about_collapsingtoolbarlayout), dominant)
          }
        }
      }
    })
    osl_recycler_view.layoutManager = LinearLayoutManager(target(), RecyclerView.VERTICAL, false)
    osl_recycler_view.with<OSL>(layoutRes = R.layout.item_osl, viewTypeMatching = { _, _ -> true }) { b, _ ->
      oslName.text = b.name
      license.text = b.license
      license.setOnClickListener { v ->
        v.context.openUrl(b.licenseUrl)
      }
      desc.text = b.desc
      setOnClickListener { v ->
        v.context.openUrl(b.website)
      }
    }
    val dp26 = target().dp2px(26f)
    val dp36 = target().dp2px(36f)
    val dp45 = target().dp2px(45f)
    team_recycler_view.layoutManager = FlexboxLayoutManager(target(), FlexDirection.ROW, FlexWrap.WRAP).apply {
      justifyContent = JustifyContent.SPACE_AROUND
    }
    team_recycler_view.with<Team>(layoutRes = R.layout.item_team_leader, viewTypeMatching = { b, _ -> b.type == 0 }) { b, _ ->
      layoutParams = (layoutParams as FlexboxLayoutManager.LayoutParams).apply {
        flexGrow = 3f
      }
      leaderAvatar.load(url = b.avatar.suffixRandom(), radius = dp45)
      leaderName.text = b.name
      leaderJob.text = b.job
      setOnClickListener {
        val url = b.url
        if (url.length > 7) browse(url)
      }
    }.with(layoutRes = R.layout.item_team_core, viewTypeMatching = { b, _ -> b.type == 1 }) { b, _ ->
      layoutParams = (layoutParams as FlexboxLayoutManager.LayoutParams).apply {
        flexGrow = 2f
      }
      coreAvatar.load(url = b.avatar.suffixRandom(), radius = dp36)
      coreName.text = b.name
      coreJob.text = b.job
      setOnClickListener {
        val url = b.url
        if (url.length > 7) browse(url)
      }
    }.with(layoutRes = R.layout.item_team_member, viewTypeMatching = { b, _ -> b.type == 2 }) { b, _ ->
      memberAvatar.load(url = b.avatar.suffixRandom(), radius = dp26)
      memberName.text = b.name
      memberJob.text = b.job
      setOnClickListener {
        val url = b.url
        if (url.length > 7) browse(url)
      }
    }
    aboutViewModel.getTeam().observe(this, Observer {
      if (!it.isNullOrEmpty()) {
        team_recycler_view.addNew(it)
      }
    })
    aboutViewModel.getOSL().observe(this, Observer {
      if (!it.isNullOrEmpty()) {
        osl_recycler_view.addNew(it)
      }
    })
  }
}
