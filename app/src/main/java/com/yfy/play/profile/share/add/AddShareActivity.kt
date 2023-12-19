package com.yfy.play.profile.share.add

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.yfy.core.Play
import com.yfy.core.util.isHttpUrl
import com.yfy.core.util.showToast
import com.yfy.core.view.base.BaseActivity
import com.yfy.play.R
import com.yfy.play.base.util.ActivityRouter
import com.yfy.play.databinding.ActivityAddShareBinding
import com.yfy.play.profile.share.ShareRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddShareActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAddShareBinding

    @Inject
    lateinit var shareRepository: ShareRepository

    override fun getLayoutView(): View {
        binding = ActivityAddShareBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        binding.addShareBtnAdd.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.addShareBtnAdd -> {
                addShare()
            }
        }
    }

    private fun addShare() {
        lifecycleScope.launch {
            Play.isLogin().first {
                if (!it) {
                    showToast(getString(R.string.not_currently_logged_in))
                    ActivityRouter.showLoginActivity(this@AddShareActivity)
//                    LoginActivity.actionStart(this@AddShareActivity)
                }
                it
            }
        }
        val title = binding.addShareEtTitle.text.toString().trim()
        if (TextUtils.isEmpty(title) || title == "") {
            binding.addShareEtTitle.error = getString(R.string.title_cannot_empty)
            return
        }
        val link = binding.addShareEtLink.text.toString().trim()
        if (TextUtils.isEmpty(link) || link == "") {
            binding.addShareEtLink.error = getString(R.string.link_cannot_empty)
            return
        }
        if (!link.isHttpUrl()) {
            binding.addShareEtLink.error = getString(R.string.link_format_error)
            return
        }
        shareRepository.shareArticle(title, link).observe(this) {
            if (it.isSuccess) {
                showToast(getString(R.string.share_success))
                finish()
            }
        }
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, AddShareActivity::class.java)
            context.startActivity(intent)
        }
    }

}