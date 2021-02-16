package com.woocommerce.android.ui.products.downloads

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.databinding.FragmentProductDownloadsListBinding
import com.woocommerce.android.extensions.takeIfNotEqualTo
import com.woocommerce.android.ui.products.BaseProductEditorFragment2
import com.woocommerce.android.ui.products.ProductDetailViewModel.ProductExitEvent.ExitProductDownloads
import com.woocommerce.android.ui.products.ProductEditorViewModelFactory
import com.woocommerce.android.widgets.CustomProgressDialog
import com.woocommerce.android.widgets.DraggableItemTouchHelper
import dagger.Lazy
import javax.inject.Inject

class ProductDownloadsFragment : BaseProductEditorFragment2(R.layout.fragment_product_downloads_list) {
    private val itemTouchHelper by lazy {
        DraggableItemTouchHelper(
            dragDirs = UP or DOWN,
            onMove = { from, to ->
                viewModel.swapDownloadableFiles(from, to)
                updateFilesFromProductDraft()
            }
        )
    }

    @Inject lateinit var viewModelFactory: Lazy<ProductEditorViewModelFactory>
    private val viewModel: ProductDownloadsViewModel by viewModels { viewModelFactory.get() }

    private var _binding: FragmentProductDownloadsListBinding? = null
    private val binding get() = _binding!!

    private val productDownloadsAdapter: ProductDownloadsAdapter by lazy {
        ProductDownloadsAdapter(
            viewModel::onProductDownloadClicked,
            itemTouchHelper
        )
    }

    private var progressDialog: CustomProgressDialog? = null

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductDownloadsListBinding.bind(view)

        setHasOptionsMenu(true)
        setupObservers()
        setupResultHandlers()

        with(binding.productDownloadsRecycler) {
            adapter = productDownloadsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_product_downloads_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                viewModel.onDoneButtonClicked(ExitProductDownloads(shouldShowDiscardDialog = false))
                true
            }
            R.id.menu_product_downloads_settings -> {
                viewModel.onDownloadsSettingsClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupResultHandlers() {
//        handleResult<List<Image>>(WPMediaPickerFragment.KEY_WP_IMAGE_PICKER_RESULT) {
//            viewModel.showAddProductDownload(it.first().source)
//            changesMade()
//        }
    }

    override fun getFragmentTitle(): String = getString(R.string.product_downloadable_files)

    fun setupObservers() {
        viewModel.productDownloadsViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.isUploadingDownloadableFile?.takeIfNotEqualTo(old?.isUploadingDownloadableFile) {
                if (it) {
                    showUploadingProgressDialog()
                } else {
                    hideUploadingProgressDialog()
                }
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ExitProductDownloads -> findNavController().navigateUp()
                else -> event.isHandled = false
            }
        }

        binding.addProductDownloadsView.initView { viewModel.onAddDownloadableFileClicked() }

        updateFilesFromProductDraft()
    }

    private fun showUploadingProgressDialog() {
        hideUploadingProgressDialog()
        progressDialog = CustomProgressDialog.show(
            getString(R.string.product_downloadable_files_upload_dialog_title),
            getString(R.string.product_downloadable_files_upload_dialog_message)
        ).also { it.show(parentFragmentManager, CustomProgressDialog.TAG) }
        progressDialog?.isCancelable = false
    }

    private fun hideUploadingProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun updateFilesFromProductDraft() {
        val product = requireNotNull(viewModel.getProduct().productDraft)
        productDownloadsAdapter.filesList = product.downloads
        // changesMade()
    }

//    override fun hasChanges(): Boolean {
//        return viewModel.hasDownloadsChanges() || viewModel.hasDownloadsSettingsChanges()
//    }
//
//    override fun onRequestAllowBackPress(): Boolean {
//        return viewModel.onBackButtonClicked(ExitProductDownloads())
//    }
}
