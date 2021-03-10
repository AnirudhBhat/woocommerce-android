package com.woocommerce.android.ui.orders.shippinglabels.creation

import android.os.Parcelable
import androidx.annotation.StringRes
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.woocommerce.android.R
import com.woocommerce.android.di.ViewModelAssistedFactory
import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelEvent.EditSelectedAddress
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelEvent.UseSelectedAddress
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelAddressValidator.AddressType.ORIGIN
import com.woocommerce.android.util.CoroutineDispatchers
import com.woocommerce.android.viewmodel.LiveDataDelegate
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.Exit
import com.woocommerce.android.viewmodel.SavedStateWithArgs
import com.woocommerce.android.viewmodel.ScopedViewModel
import kotlinx.android.parcel.Parcelize

class ShippingLabelAddressSuggestionViewModel @AssistedInject constructor(
    @Assisted savedState: SavedStateWithArgs,
    dispatchers: CoroutineDispatchers
) : ScopedViewModel(savedState, dispatchers) {
    private val arguments: ShippingLabelAddressSuggestionFragmentArgs by savedState.navArgs()

    val viewStateData = LiveDataDelegate(savedState, ViewState(arguments.enteredAddress, arguments.suggestedAddress))
    private var viewState by viewStateData

    init {
        viewState = viewState.copy(
            title = if (arguments.addressType == ORIGIN) {
                R.string.orderdetail_shipping_label_item_shipfrom
            } else {
                R.string.orderdetail_shipping_label_item_shipto
            }
        )
    }

    fun onUseSelectedAddressTapped() {
        viewState.selectedAddress?.let {
            triggerEvent(UseSelectedAddress(it))
        }
    }

    fun onEditSelectedAddressTapped() {
        viewState.selectedAddress?.let {
            triggerEvent(EditSelectedAddress(it))
        }
    }

    fun onSelectedAddressChanged(isSuggestedAddress: Boolean) {
        val address = if (isSuggestedAddress) viewState.suggestedAddress else viewState.enteredAddress
        viewState = viewState.copy(selectedAddress = address)
    }

    fun onExit() {
        triggerEvent(Exit)
    }

    @Parcelize
    data class ViewState(
        val enteredAddress: Address? = null,
        val suggestedAddress: Address? = null,
        val selectedAddress: Address? = null,
        @StringRes val title: Int? = null
    ) : Parcelable {
        val areButtonsEnabled: Boolean = selectedAddress != null
    }

    @AssistedInject.Factory
    interface Factory : ViewModelAssistedFactory<ShippingLabelAddressSuggestionViewModel>
}
