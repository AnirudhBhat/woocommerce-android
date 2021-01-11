package com.woocommerce.android.ui.orders.shippinglabels.creation

import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelViewModel.Step
import com.woocommerce.android.ui.orders.shippinglabels.creation.CreateShippingLabelViewModel.ViewState
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.Data
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.Event
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.Event.OriginAddressValidationStarted
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.FlowStep.ORIGIN_ADDRESS
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelsStateMachine.SideEffect
import com.woocommerce.android.util.CoroutineTestRule
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.SavedStateWithArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateShippingLabelViewModelTest : BaseUnitTest() {
    companion object {
        private const val ORDER_ID = "123"
    }

    private val repository: OrderDetailRepository = mock()
    private val stateMachine: ShippingLabelsStateMachine = mock()
    private val addressValidator: ShippingAddressValidator = mock()
    private lateinit var stateFlow: MutableStateFlow<SideEffect>

    private val originAddress = CreateShippingLabelTestUtils.generateAddress()
    private val originAddressValidated = originAddress.copy(city = "DONE")
    private val shippingAddress = originAddress.copy(company = "McDonald's")
    private val shippingAddressValidated = shippingAddress.copy(city = "DONE")

    private val data = Data(
        originAddress = originAddress,
        shippingAddress = shippingAddress,
        flowSteps = setOf(ORIGIN_ADDRESS)
    )

    private val originAddressCurrent = Step(
        details = originAddress.toString(),
        isEnabled = true,
        isContinueButtonVisible = true,
        isEditButtonVisible = false,
        isHighlighted = true
    )

    private val originAddressDone = originAddressCurrent.copy(
        details = originAddressValidated.toString(),
        isContinueButtonVisible = false,
        isEditButtonVisible = true,
        isHighlighted = false
    )

    private val shippingAddressNotDone = Step(
        details = shippingAddress.toString(),
        isEnabled = false,
        isContinueButtonVisible = false,
        isEditButtonVisible = false,
        isHighlighted = false
    )

    private val shippingAddressCurrent = shippingAddressNotDone.copy(
        isEnabled = true,
        isContinueButtonVisible = true,
        isEditButtonVisible = false,
        isHighlighted = true
    )

    private val shippingAddressDone = shippingAddressCurrent.copy(
        details = shippingAddressValidated.toString(),
        isContinueButtonVisible = false,
        isEditButtonVisible = true,
        isHighlighted = false
    )

    private val otherNotDone = Step(
        isEnabled = false,
        isContinueButtonVisible = false,
        isEditButtonVisible = false,
        isHighlighted = false
    )

    private val otherCurrent = Step(
        isEnabled = true,
        isContinueButtonVisible = true,
        isEditButtonVisible = false,
        isHighlighted = true
    )

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()
    private val savedState: SavedStateWithArgs = spy(
        SavedStateWithArgs(
            SavedStateHandle(),
            null,
            CreateShippingLabelFragmentArgs(ORDER_ID)
        )
    )

    private lateinit var viewModel: CreateShippingLabelViewModel

    @Before
    fun setup() {
        stateFlow = MutableStateFlow(SideEffect.NoOp)
        whenever(stateMachine.effects).thenReturn(stateFlow)

        viewModel = spy(
            CreateShippingLabelViewModel(
                savedState,
                coroutinesTestRule.testDispatchers,
                repository,
                stateMachine,
                addressValidator
            )
        )

        clearInvocations(
            viewModel,
            savedState,
            repository,
            stateMachine
        )
    }

    @Test
    fun `Displays create shipping label view correctly`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        var viewState: ViewState? = null
        viewModel.viewStateData.observeForever { _, new -> viewState = new }

        assertThat(viewState).isEqualTo(ViewState())
    }

    @Test
    fun `Displays data-loaded state correctly`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        var viewState: ViewState? = null
        viewModel.viewStateData.observeForever { _, new -> viewState = new }

        val expectedViewState = ViewState(
            originAddressStep = originAddressCurrent,
            shippingAddressStep = shippingAddressNotDone,
            packagingDetailsStep = otherNotDone,
            customsStep = otherNotDone,
            carrierStep = otherNotDone,
            paymentStep = otherNotDone
        )

        stateFlow.value = SideEffect.UpdateViewState(data)

        assertThat(viewState).isEqualTo(expectedViewState)
    }

    @Test
    fun `Displays origin-address validated state correctly`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        var viewState: ViewState? = null
        viewModel.viewStateData.observeForever { _, new -> viewState = new }

        val expectedViewState = ViewState(
            originAddressStep = originAddressDone,
            shippingAddressStep = shippingAddressCurrent,
            packagingDetailsStep = otherNotDone,
            customsStep = otherNotDone,
            carrierStep = otherNotDone,
            paymentStep = otherNotDone
        )

        val newData = data.copy(
            originAddress = originAddressValidated,
            flowSteps = data.flowSteps + FlowStep.SHIPPING_ADDRESS
        )
        stateFlow.value = SideEffect.UpdateViewState(newData)

        assertThat(viewState).isEqualTo(expectedViewState)
    }

    @Test
    fun `Displays shipping-address validated state correctly`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        var viewState: ViewState? = null
        viewModel.viewStateData.observeForever { _, new -> viewState = new }

        val expectedViewState = ViewState(
            originAddressStep = originAddressDone,
            shippingAddressStep = shippingAddressDone,
            packagingDetailsStep = otherCurrent,
            customsStep = otherNotDone,
            carrierStep = otherNotDone,
            paymentStep = otherNotDone
        )

        val newData = data.copy(
            originAddress = originAddressValidated,
            shippingAddress = shippingAddressValidated,
            flowSteps = data.flowSteps + FlowStep.SHIPPING_ADDRESS + FlowStep.PACKAGING
        )
        stateFlow.value = SideEffect.UpdateViewState(newData)

        assertThat(viewState).isEqualTo(expectedViewState)
    }

    @Test
    fun `Continue click in origin address triggers validation`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        stateFlow.value = SideEffect.UpdateViewState(data)

        viewModel.onContinueButtonTapped(ORIGIN_ADDRESS)

        verify(stateMachine).handleEvent(OriginAddressValidationStarted)

        stateFlow.value = SideEffect.ValidateAddress(originAddress)

        verify(addressValidator).validateAddress(originAddress)
    }

    @Test
    fun `Edit click in origin address triggers validation`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val newData = data.copy(
            originAddress = originAddressValidated,
            flowSteps = data.flowSteps + FlowStep.SHIPPING_ADDRESS
        )
        stateFlow.value = SideEffect.UpdateViewState(newData)

        viewModel.onEditButtonTapped(ORIGIN_ADDRESS)

        verify(stateMachine).handleEvent(Event.EditOriginAddressRequested)

        stateFlow.value = SideEffect.OpenAddressEditor(originAddress)

        verify(stateMachine).handleEvent(Event.AddressEditFinished(originAddress))
    }
}
