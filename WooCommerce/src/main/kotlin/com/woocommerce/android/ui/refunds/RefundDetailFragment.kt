package com.woocommerce.android.ui.refunds

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.extensions.navigateBackWithResult
import com.woocommerce.android.ui.orders.OrderDetailFragment.Companion.REFUND_REQUEST_CODE
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_refund_detail.*
import kotlinx.android.synthetic.main.fragment_refund_summary.*
import javax.inject.Inject

class RefundDetailFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        return inflater.inflate(R.layout.fragment_refund_detail, container, false)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewModel()
    }

    private fun initializeViewModel() {
        ViewModelProviders.of(requireActivity(), viewModelFactory).get(RefundDetailViewModel::class.java).also {
            setupObservers(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers(viewModel: RefundDetailViewModel) {
        viewModel.screenTitle.observe(this, Observer {
            activity?.title = it
        })

        viewModel.formattedRefundAmount.observe(this, Observer {
            refundDetail_refundAmount.text = it
        })

        viewModel.refundMethod.observe(this, Observer {
            refundDetail_refundMethod.text = it
        })

        viewModel.refundReason.observe(this, Observer {
            refundDetail_refundReason.text = it
        })
    }
}
