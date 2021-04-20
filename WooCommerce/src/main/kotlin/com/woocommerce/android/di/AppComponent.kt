//package com.woocommerce.android.di
//
//import android.app.Application
//import com.woocommerce.android.WooCommerce
//import com.woocommerce.android.media.ProductImagesServiceModule
//import com.woocommerce.android.push.FCMServiceModule
//import com.woocommerce.android.ui.login.LoginAnalyticsModule
//import dagger.BindsInstance
//import dagger.Component
//import dagger.android.AndroidInjectionModule
//import dagger.android.AndroidInjector
//import org.wordpress.android.fluxc.module.ReleaseBaseModule
//import org.wordpress.android.fluxc.module.ReleaseNetworkModule
//import org.wordpress.android.fluxc.module.ReleaseOkHttpClientModule
//import org.wordpress.android.fluxc.module.ReleaseWCNetworkModule
//import org.wordpress.android.login.di.LoginServiceModule
//import javax.inject.Singleton
//
//@Singleton
//@Component(modules = [
//        OrderFetcherModule::class
//])
//interface AppComponent {
//    fun inject(app: WooCommerce)
//
//    // Allows us to inject the application without having to instantiate any modules, and provides the Application
//    // in the app graph
//    @Component.Builder
//    interface Builder {
//        @BindsInstance
//        fun application(application: Application): Builder
//
//        fun build(): AppComponent
//    }
//}
