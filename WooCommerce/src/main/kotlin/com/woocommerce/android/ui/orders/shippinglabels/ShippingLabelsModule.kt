package com.woocommerce.android.ui.orders.shippinglabels

import com.woocommerce.android.di.FragmentScope
import com.woocommerce.android.ui.orders.shippinglabels.ShippingLabelsModule.CreateShippingLabelFragmentModule
import com.woocommerce.android.ui.orders.shippinglabels.ShippingLabelsModule.EditShippingLabelAddressFragmentModule
import com.woocommerce.android.ui.orders.shippinglabels.ShippingLabelsModule.PrintShippingLabelFragmentModule
import com.woocommerce.android.ui.orders.shippinglabels.ShippingLabelsModule.ShippingLabelRefundFragmentModule
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelFragment
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelModule
import com.woocommerce.android.ui.orders.shippinglabels.creation.EditShippingLabelAddressFragment
import com.woocommerce.android.ui.orders.shippinglabels.creation.EditShippingLabelAddressModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    ShippingLabelRefundFragmentModule::class,
    PrintShippingLabelFragmentModule::class,
    CreateShippingLabelFragmentModule::class,
    EditShippingLabelAddressFragmentModule::class
])
object ShippingLabelsModule {
    @Module
    abstract class ShippingLabelRefundFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [ShippingLabelRefundModule::class])
        abstract fun shippingLabelRefundFragment(): ShippingLabelRefundFragment
    }
    @Module
    abstract class PrintShippingLabelFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [PrintShippingLabelModule::class])
        abstract fun printShippingLabelFragment(): PrintShippingLabelFragment
    }
    @Module
    abstract class CreateShippingLabelFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [CreateShippingLabelModule::class])
        abstract fun createShippingLabelFragment(): CreateShippingLabelFragment
    }
    @Module
    abstract class EditShippingLabelAddressFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [EditShippingLabelAddressModule::class])
        abstract fun editShippingLabelAddressFragment(): EditShippingLabelAddressFragment
    }
}
