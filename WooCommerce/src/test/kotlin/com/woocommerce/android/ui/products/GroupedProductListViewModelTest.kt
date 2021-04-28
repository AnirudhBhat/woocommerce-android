package com.woocommerce.android.ui.products

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.woocommerce.android.model.Product
import com.woocommerce.android.tools.NetworkStatus
import com.woocommerce.android.ui.products.GroupedProductListViewModel.GroupedProductListViewState
import com.woocommerce.android.util.CoroutineDispatchers
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.woocommerce.android.viewmodel.SavedStateWithArgs
import com.woocommerce.android.viewmodel.test
import kotlinx.coroutines.Dispatchers
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class GroupedProductListViewModelTest : BaseUnitTest() {
    companion object {
        private const val PRODUCT_REMOTE_ID = 1L
        private val GROUPED_PRODUCT_IDS = longArrayOf(2, 3, 4, 5)
    }

    private val networkStatus: NetworkStatus = mock()
    private val productRepository: GroupedProductListRepository = mock()
    private val savedState: SavedStateWithArgs = spy(
        SavedStateWithArgs(
            SavedStateHandle(),
            null,
            GroupedProductListFragmentArgs(
                remoteProductId = PRODUCT_REMOTE_ID,
                productIds = GROUPED_PRODUCT_IDS,
                groupedProductListType = GroupedProductListType.GROUPED
            )
        )
    )

    private val coroutineDispatchers = CoroutineDispatchers(
        Dispatchers.Unconfined, Dispatchers.Unconfined, Dispatchers.Unconfined)
    private val productList = ProductTestUtils.generateProductList()
    private val groupedProductIds = GROUPED_PRODUCT_IDS.toList()

    private lateinit var viewModel: GroupedProductListViewModel

    @Before
    fun setup() {
        doReturn(MutableLiveData(GroupedProductListViewState(groupedProductIds)))
            .whenever(savedState).getLiveData<GroupedProductListViewState>(any(), any())
        doReturn(true).whenever(networkStatus).isConnected()
    }

    private fun createViewModel() {
        viewModel = spy(
            GroupedProductListViewModel(
                savedState,
                coroutineDispatchers,
                networkStatus,
                productRepository
            )
        )
    }

    @Test
    fun `Displays the grouped product list view correctly`() = test {
        doReturn(productList).whenever(productRepository).fetchProductList(groupedProductIds)

        createViewModel()

        val products = ArrayList<Product>()
        viewModel.productList.observeForever {
            it?.let { products.addAll(it) }
        }

        assertThat(products).isEqualTo(productList)
    }

    @Test
    fun `Displays grouped product list view correctly after deletion`() {
        createViewModel()

        assertThat(viewModel.hasChanges).isEqualTo(false)

        var productData: GroupedProductListViewState? = null
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        viewModel.onProductDeleted(productList.last())

        assertThat(groupedProductIds.size - 1).isEqualTo(productData?.selectedProductIds?.size)
        assertThat(viewModel.hasChanges).isEqualTo(true)
    }

    @Test
    fun `Displays grouped product list view correctly after addition`() {
        createViewModel()

        assertThat(viewModel.hasChanges).isEqualTo(false)

        var productData: GroupedProductListViewState? = null
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        val listAdded = listOf<Long>(6, 7, 8)
        viewModel.onProductsAdded(listAdded)

        assertThat(groupedProductIds.size + listAdded.size).isEqualTo(productData?.selectedProductIds?.size)
        assertThat(viewModel.hasChanges).isEqualTo(true)
    }

    @Test
    fun `ExitWithResult event dispatched correctly when back button clicked`() {
        createViewModel()

        var productData: GroupedProductListViewState? = null
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        var event: Event? = null
        viewModel.event.observeForever { new -> event = new }

        viewModel.onProductDeleted(productList.last())
        viewModel.onBackButtonClicked()

        assertThat(event).isInstanceOf(ExitWithResult::class.java)
        assertThat(((event as ExitWithResult<*>).data as List<*>).size).isEqualTo(
            productData?.selectedProductIds?.size
        )
    }

    @Test
    fun `Displays previously selected product list correctly after canceling edit mode`() {

        // Given
        createViewModel()
        val previousSelectedProductIds = listOf(1L, 2L, 3L, 4L, 5L)
        var productData: GroupedProductListViewState? = null
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        // When
        viewModel.restorePreviousProductList(previousSelectedProductIds)

        // Then
        assertThat(previousSelectedProductIds).isEqualTo(productData?.selectedProductIds)
    }

    @Test
    fun `Displays current product list if previously selected list is null after canceling edit mode`() {

        // Given
        createViewModel()
        val previousSelectedProductIds = null
        var productData: GroupedProductListViewState? = null
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        // When
        viewModel.restorePreviousProductList(previousSelectedProductIds)

        // Then
        assertThat(groupedProductIds).isEqualTo(productData?.selectedProductIds)
    }

    @Test
    fun `re-ordering the product via drag-and-drop must reflect properly`() {

        // Given
        createViewModel()
        val reorderedProductList = arrayListOf<Product>()
        reorderedProductList.add(ProductTestUtils.generateProduct(5L))
        reorderedProductList.add(ProductTestUtils.generateProduct(2L))
        reorderedProductList.add(ProductTestUtils.generateProduct(3L))
        reorderedProductList.add(ProductTestUtils.generateProduct(1L))
        reorderedProductList.add(ProductTestUtils.generateProduct(4L))
        val reorderedProductIdsList = reorderedProductList.map { it.remoteId }
        var productData: GroupedProductListViewState? = null
        assertThat(viewModel.hasChanges).isEqualTo(false)
        viewModel.productListViewStateData.observeForever { _, new -> productData = new }

        // When
        viewModel.updateReOrderedProductList(reorderedProductList)

        // Then
        assertThat(viewModel.hasChanges).isEqualTo(true)
        assertThat(reorderedProductIdsList).isEqualTo(productData?.selectedProductIds)
    }
}
