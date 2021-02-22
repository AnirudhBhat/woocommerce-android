package com.woocommerce.android.ui.orders.shippinglabels

import com.woocommerce.android.annotations.OpenClassOnDebug
import com.woocommerce.android.model.ShippingAccountSettings
import com.woocommerce.android.model.ShippingLabel
import com.woocommerce.android.model.ShippingPackage
import com.woocommerce.android.model.toAppModel
import com.woocommerce.android.tools.SelectedSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
import org.wordpress.android.fluxc.store.WCShippingLabelStore
import javax.inject.Inject

@OpenClassOnDebug
class ShippingLabelRepository @Inject constructor(
    private val shippingLabelStore: WCShippingLabelStore,
    private val selectedSite: SelectedSite
) {
    suspend fun refundShippingLabel(orderId: Long, shippingLabelId: Long): WooResult<Boolean> {
        return withContext(Dispatchers.IO) {
            shippingLabelStore.refundShippingLabelForOrder(
                site = selectedSite.get(),
                orderId = orderId,
                remoteShippingLabelId = shippingLabelId
            )
        }
    }

    fun getShippingLabelByOrderIdAndLabelId(
        orderId: Long,
        shippingLabelId: Long
    ): ShippingLabel? {
        return shippingLabelStore.getShippingLabelById(
            selectedSite.get(), orderId, shippingLabelId
        )
            ?.toAppModel()
    }

    suspend fun printShippingLabel(paperSize: String, shippingLabelId: Long): WooResult<String> {
        return withContext(Dispatchers.IO) {
            shippingLabelStore.printShippingLabel(
                site = selectedSite.get(),
                paperSize = paperSize,
                remoteShippingLabelId = shippingLabelId
            )
        }
    }

    suspend fun getShippingPackages(): WooResult<List<ShippingPackage>> {
        return shippingLabelStore.getPackageTypes(selectedSite.get()).let { result ->
            if (result.isError) return@let WooResult(error = result.error)

            val packagesResult = result.model!!
            val list = mutableListOf<ShippingPackage>()
            packagesResult.customPackages.map {
                list.add(it.toAppModel())
            }
            packagesResult.predefinedOptions.forEach { option ->
                list.addAll(option.toAppModel())
            }

            WooResult(list)
        }
    }

    suspend fun getAccountSettings(): WooResult<ShippingAccountSettings> {
        return shippingLabelStore.getAccountSettings(selectedSite.get()).let { result ->
            if (result.isError) return@let WooResult(error = result.error)

            WooResult(result.model!!.toAppModel())
        }
    }
}
