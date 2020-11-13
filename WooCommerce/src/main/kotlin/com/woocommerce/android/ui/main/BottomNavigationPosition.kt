package com.woocommerce.android.ui.main

import com.woocommerce.android.R
import com.woocommerce.android.ui.base.TopLevelFragment
import com.woocommerce.android.ui.mystore.MyStoreFragment
import com.woocommerce.android.ui.products.ProductListFragment
import com.woocommerce.android.ui.reviews.ReviewListFragment
import com.woocommerce.android.ui.orders.list.OrderListFragment

enum class BottomNavigationPosition(val position: Int, val id: Int) {
    MY_STORE(0, R.id.dashboard),
    ORDERS(1, R.id.orders),
    PRODUCTS(2, R.id.products),
    REVIEWS(3, R.id.reviews)
}

fun findNavigationPositionById(id: Int): BottomNavigationPosition = when (id) {
    BottomNavigationPosition.MY_STORE.id -> BottomNavigationPosition.MY_STORE
    BottomNavigationPosition.ORDERS.id -> BottomNavigationPosition.ORDERS
    BottomNavigationPosition.PRODUCTS.id -> BottomNavigationPosition.PRODUCTS
    BottomNavigationPosition.REVIEWS.id -> BottomNavigationPosition.REVIEWS
    else -> BottomNavigationPosition.MY_STORE
}

fun BottomNavigationPosition.getTag(): String = when (this) {
    BottomNavigationPosition.MY_STORE -> MyStoreFragment.TAG
    BottomNavigationPosition.ORDERS -> OrderListFragment.TAG
    BottomNavigationPosition.PRODUCTS -> ProductListFragment.TAG
    BottomNavigationPosition.REVIEWS -> ReviewListFragment.TAG
}

fun BottomNavigationPosition.createFragment(): TopLevelFragment = when (this) {
    BottomNavigationPosition.MY_STORE -> MyStoreFragment.newInstance()
    BottomNavigationPosition.ORDERS -> OrderListFragment.newInstance()
    BottomNavigationPosition.PRODUCTS -> ProductListFragment.newInstance()
    BottomNavigationPosition.REVIEWS -> ReviewListFragment.newInstance()
}
