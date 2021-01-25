package com.woocommerce.android.ui.orders.shippinglabels.creation

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.R
import com.woocommerce.android.databinding.FragmentCreateShippingLabelBinding
import com.woocommerce.android.extensions.handleNotice
import com.woocommerce.android.extensions.handleResult
import com.woocommerce.android.extensions.navigateSafely
import com.woocommerce.android.extensions.takeIfNotEqualTo
import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.base.UIMessageResolver
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelEvent.ShowAddressEditor
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelEvent.ShowSuggestedAddress
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelViewModel.Step
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.CARRIER
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.CUSTOMS
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.ORIGIN_ADDRESS
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.PACKAGING
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.PAYMENT
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.SHIPPING_ADDRESS
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.woocommerce.android.viewmodel.ViewModelFactory
import com.woocommerce.android.widgets.CustomProgressDialog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CreateShippingLabelFragment : BaseFragment(R.layout.fragment_create_shipping_label) {
    companion object {
        const val EDIT_ADDRESS_RESULT = "key_edit_address_dialog_result"
        const val EDIT_ADDRESS_CLOSED = "key_edit_address_dialog_closed"
    }

    private var progressDialog: CustomProgressDialog? = null

    @Inject lateinit var uiMessageResolver: UIMessageResolver
    @Inject lateinit var viewModelFactory: ViewModelFactory

    val viewModel: CreateShippingLabelViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentCreateShippingLabelBinding? = null
    private val binding get() = _binding!!

    override fun getFragmentTitle() = getString(R.string.shipping_label_create_title)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCreateShippingLabelBinding.bind(view)

        initializeViewModel()
        initializeViews()
    }

    override fun onPause() {
        super.onPause()
        progressDialog?.dismiss()
    }

    private fun initializeViewModel() {
        subscribeObservers()
        setupResultHandlers()
    }

    private fun setupResultHandlers() {
        handleResult<Address>(EDIT_ADDRESS_RESULT) {
            viewModel.onAddressEditConfirmed(it)
        }
        handleNotice(EDIT_ADDRESS_CLOSED) {
            viewModel.onAddressEditCanceled()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun subscribeObservers() {
        viewModel.viewStateData.observe(viewLifecycleOwner) { old, new ->
            new.originAddressStep?.takeIfNotEqualTo(old?.originAddressStep) {
                binding.originStep.update(it)
            }
            new.shippingAddressStep?.takeIfNotEqualTo(old?.shippingAddressStep) {
                binding.shippingStep.update(it)
            }
            new.packagingDetailsStep?.takeIfNotEqualTo(old?.packagingDetailsStep) {
                binding.packagingStep.update(it)
            }
            new.customsStep?.takeIfNotEqualTo(old?.customsStep) {
                binding.customsStep.update(it)
            }
            new.carrierStep?.takeIfNotEqualTo(old?.carrierStep) {
                binding.carrierStep.update(it)
            }
            new.paymentStep?.takeIfNotEqualTo(old?.paymentStep) {
                binding.paymentStep.update(it)
            }
            new.isProgressDialogVisible?.takeIfNotEqualTo(old?.isProgressDialogVisible) { isVisible ->
                if (isVisible) {
                    showProgressDialog(
                        R.string.shipping_label_edit_address_validation_progress_title,
                        R.string.shipping_label_edit_address_validation_progress_message
                    )
                } else {
                    hideProgressDialog()
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowAddressEditor -> {
                    val action = CreateShippingLabelFragmentDirections
                        .actionCreateShippingLabelFragmentToEditShippingLabelAddressFragment(
                            event.address,
                            event.type,
                            event.validationResult
                        )
                    findNavController().navigateSafely(action)
                }
                is ShowSuggestedAddress -> {}
                else -> event.isHandled = false
            }
        })
    }

    private fun showProgressDialog(@StringRes title: Int, @StringRes message: Int) {
        hideProgressDialog()
        progressDialog = CustomProgressDialog.show(
            getString(title),
            getString(message)
        ).also { it.show(parentFragmentManager, CustomProgressDialog.TAG) }
        progressDialog?.isCancelable = false
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun initializeViews() {
        binding.originStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(ORIGIN_ADDRESS) }
        binding.shippingStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(SHIPPING_ADDRESS) }
        binding.packagingStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(PACKAGING) }
        binding.customsStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(CUSTOMS) }
        binding.carrierStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(CARRIER) }
        binding.paymentStep.continueButtonClickListener = { viewModel.onContinueButtonTapped(PAYMENT) }

        binding.originStep.editButtonClickListener = { viewModel.onEditButtonTapped(ORIGIN_ADDRESS) }
        binding.shippingStep.editButtonClickListener = { viewModel.onEditButtonTapped(SHIPPING_ADDRESS) }
        binding.packagingStep.editButtonClickListener = { viewModel.onEditButtonTapped(PACKAGING) }
        binding.customsStep.editButtonClickListener = { viewModel.onEditButtonTapped(CUSTOMS) }
        binding.carrierStep.editButtonClickListener = { viewModel.onEditButtonTapped(CARRIER) }
        binding.paymentStep.editButtonClickListener = { viewModel.onEditButtonTapped(PAYMENT) }
    }

    private fun ShippingLabelCreationStepView.update(data: Step) {
        data.details?.let { details = it }
        data.isEnabled?.let { isViewEnabled = it }
        data.isContinueButtonVisible?.let { isContinueButtonVisible = it }
        data.isEditButtonVisible?.let { isEditButtonVisible = it }
        data.isHighlighted?.let { isHighlighted = it }
    }
}
