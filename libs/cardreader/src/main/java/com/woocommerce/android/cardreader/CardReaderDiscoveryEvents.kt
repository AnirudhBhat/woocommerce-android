package com.woocommerce.android.cardreader

sealed class CardReaderDiscoveryEvents {
    object NotStarted : CardReaderDiscoveryEvents()
    object Started : CardReaderDiscoveryEvents()
    data class ReadersFound(val list: List<String>) : CardReaderDiscoveryEvents()
    data class Failed(val msg: String) : CardReaderDiscoveryEvents()
}
