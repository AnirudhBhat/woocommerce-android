package com.woocommerce.android.ui.products

import com.woocommerce.android.R
import com.woocommerce.android.R.drawable
import com.woocommerce.android.R.string
import com.woocommerce.android.analytics.AnalyticsTracker.Stat
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_INVENTORY_SETTINGS_TAPPED
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
import com.woocommerce.android.analytics.AnalyticsTracker.Stat.PRODUCT_DETAIL_VIEW_PRODUCT_VARIANTS_TAPPED
import com.woocommerce.android.extensions.addIfNotEmpty
import com.woocommerce.android.extensions.fastStripHtml
import com.woocommerce.android.extensions.filterNotEmpty
import com.woocommerce.android.extensions.isSet
import com.woocommerce.android.model.Product
import com.woocommerce.android.ui.products.ProductInventoryViewModel.InventoryData
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewGroupedProducts
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewLinkedProducts
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductCategories
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductDescriptionEditor
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductDownloads
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductExternalLink
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductInventory
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductPricing
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductReviews
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductShipping
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductShortDescriptionEditor
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductTags
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductTypes
import com.woocommerce.android.ui.products.ProductNavigationTarget.ViewProductVariations
import com.woocommerce.android.ui.products.ProductPricingViewModel.PricingData
import com.woocommerce.android.ui.products.ProductShippingViewModel.ShippingData
import com.woocommerce.android.ui.products.ProductType.EXTERNAL
import com.woocommerce.android.ui.products.ProductType.GROUPED
import com.woocommerce.android.ui.products.ProductType.OTHER
import com.woocommerce.android.ui.products.ProductType.SIMPLE
import com.woocommerce.android.ui.products.ProductType.VARIABLE
import com.woocommerce.android.ui.products.models.ProductProperty
import com.woocommerce.android.ui.products.models.ProductProperty.ComplexProperty
import com.woocommerce.android.ui.products.models.ProductProperty.Editable
import com.woocommerce.android.ui.products.models.ProductProperty.PropertyGroup
import com.woocommerce.android.ui.products.models.ProductProperty.RatingBar
import com.woocommerce.android.ui.products.models.ProductPropertyCard
import com.woocommerce.android.ui.products.models.ProductPropertyCard.Type.PRIMARY
import com.woocommerce.android.ui.products.models.ProductPropertyCard.Type.SECONDARY
import com.woocommerce.android.ui.products.models.SiteParameters
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.util.FeatureFlag.ADD_EDIT_VARIATIONS
import com.woocommerce.android.util.PriceUtils
import com.woocommerce.android.util.StringUtils
import com.woocommerce.android.viewmodel.ResourceProvider

class ProductDetailCardBuilder(
    private val viewModel: ProductDetailViewModel,
    private val resources: ResourceProvider,
    private val currencyFormatter: CurrencyFormatter,
    private val parameters: SiteParameters
) {
    private fun isSimple(product: Product) = product.productType == SIMPLE

    private lateinit var originalSku: String

    fun buildPropertyCards(product: Product, originalSku: String): List<ProductPropertyCard> {
        this.originalSku = originalSku

        val cards = mutableListOf<ProductPropertyCard>()
        cards.addIfNotEmpty(getPrimaryCard(product))

        when (product.productType) {
            SIMPLE -> cards.addIfNotEmpty(getSimpleProductCard(product))
            VARIABLE -> cards.addIfNotEmpty(getVariableProductCard(product))
            GROUPED -> cards.addIfNotEmpty(getGroupedProductCard(product))
            EXTERNAL -> cards.addIfNotEmpty(getExternalProductCard(product))
            OTHER -> cards.addIfNotEmpty(getOtherProductCard(product))
        }

        return cards
    }

    private fun getPrimaryCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = PRIMARY,
            properties = listOf(
                product.title(),
                product.description()
            ).filterNotEmpty()
        )
    }

    private fun getSimpleProductCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                product.price(),
                product.productReviews(),
                product.inventory(SIMPLE),
                product.shipping(),
                product.categories(),
                product.tags(),
                product.shortDescription(),
                product.linkedProducts(),
                product.productType(),
                product.downloads()
            ).filterNotEmpty()
        )
    }

    private fun getGroupedProductCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                product.groupedProducts(),
                product.productReviews(),
                product.inventory(GROUPED),
                product.categories(),
                product.tags(),
                product.shortDescription(),
                product.linkedProducts(),
                product.productType()
            ).filterNotEmpty()
        )
    }

    private fun getExternalProductCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                product.price(),
                product.productReviews(),
                product.externalLink(),
                product.inventory(EXTERNAL),
                product.categories(),
                product.tags(),
                product.shortDescription(),
                product.linkedProducts(),
                product.productType()
            ).filterNotEmpty()
        )
    }

    private fun getVariableProductCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                product.variations(),
                product.productReviews(),
                product.inventory(VARIABLE),
                product.shipping(),
                product.categories(),
                product.tags(),
                product.shortDescription(),
                product.linkedProducts(),
                product.productType()
            ).filterNotEmpty()
        )
    }

    /**
     * Used for product types the app doesn't support yet (ex: subscriptions), uses a subset
     * of properties since we can't be sure pricing, shipping, etc., are applicable
     */
    private fun getOtherProductCard(product: Product): ProductPropertyCard {
        return ProductPropertyCard(
            type = SECONDARY,
            properties = listOf(
                product.productReviews(),
                product.categories(),
                product.tags(),
                product.shortDescription(),
                product.linkedProducts(),
                product.productType()
            ).filterNotEmpty()
        )
    }

    private fun Product.downloads(): ProductProperty? {
        if (!this.isDownloadable || this.downloads.isEmpty()) return null
        return ComplexProperty(
            title = R.string.product_downloadable_files,
            value = StringUtils.getQuantityString(
                resourceProvider = resources,
                quantity = this.downloads.size,
                default = R.string.product_downloadable_files_value_multiple,
                one = R.string.product_downloadable_files_value_single
            ),
            icon = R.drawable.ic_gridicons_cloud,
            onClick = {
                viewModel.onEditProductCardClicked(
                    ViewProductDownloads,
                    Stat.PRODUCT_DETAIL_VIEW_DOWNLOADABLE_FILES_TAPPED
                )
            }
        )
    }

    private fun Product.downloadsLegacy(): ProductProperty? {
        return if (this.isDownloadable) {
            val limit = if (this.downloadLimit > 0) String.format(
                resources.getString(R.string.product_download_limit_count),
                this.downloadLimit
            ) else ""
            val expiry = if (this.downloadExpiry > 0) String.format(
                resources.getString(R.string.product_download_expiry_days),
                this.downloadExpiry
            ) else ""

            val downloadGroup = mapOf(
                Pair(resources.getString(R.string.product_downloadable_files), this.downloads.size.toString()),
                Pair(resources.getString(R.string.product_download_limit), limit),
                Pair(resources.getString(R.string.product_download_expiry), expiry)
            )
            PropertyGroup(
                R.string.product_downloads,
                downloadGroup
            )
        } else {
            null
        }
    }

    // shipping group is part of the secondary card if edit product is enabled
    private fun Product.readOnlyShipping(): ProductProperty? {
        return if (!isSimple(this)) {
            val shippingGroup = mapOf(
                Pair(resources.getString(R.string.product_weight), this.getWeightWithUnits(parameters.weightUnit)),
                Pair(resources.getString(R.string.product_size), this.getSizeWithUnits(parameters.dimensionUnit)),
                Pair(resources.getString(R.string.product_shipping_class), this.shippingClass)
            )
            PropertyGroup(
                R.string.product_shipping,
                shippingGroup
            )
        } else {
            null
        }
    }

    // show stock properties as a group if stock management is enabled, otherwise show sku separately
    private fun Product.readOnlyInventory(): ProductProperty {
        return if (this.isStockManaged) {
            val group = mapOf(
                Pair(
                    resources.getString(R.string.product_stock_status),
                    ProductStockStatus.stockStatusToDisplayString(resources, this.stockStatus)
                ),
                Pair(
                    resources.getString(R.string.product_backorders),
                    ProductBackorderStatus.backordersToDisplayString(resources, this.backorderStatus)
                ),
                Pair(
                    resources.getString(R.string.product_stock_quantity),
                    StringUtils.formatCountDecimal(this.stockQuantity)
                ),
                Pair(resources.getString(R.string.product_sku), this.sku)
            )
            PropertyGroup(
                R.string.product_inventory,
                group
            )
        } else {
            ComplexProperty(
                R.string.product_sku,
                this.sku,
                R.drawable.ic_gridicons_list_checkmark
            )
        }
    }

    private fun Product.readOnlyPrice(): ProductProperty? {
        val hasPricingInfo = this.regularPrice != null || this.salePrice != null
        return if (hasPricingInfo) {
            // when there's a sale price show price & sales price as a group, otherwise show price separately
            return if (this.salePrice.isSet()) {
                val group = mapOf(
                    resources.getString(R.string.product_regular_price)
                        to PriceUtils.formatCurrency(this.regularPrice, parameters.currencyCode, currencyFormatter),
                    resources.getString(R.string.product_sale_price)
                        to PriceUtils.formatCurrency(this.salePrice, parameters.currencyCode, currencyFormatter)
                )
                PropertyGroup(R.string.product_price, group)
            } else {
                ComplexProperty(
                    R.string.product_price,
                    PriceUtils.formatCurrency(this.regularPrice, parameters.currencyCode, currencyFormatter)
                )
            }
        } else {
            null
        }
    }

    private fun Product.shortDescription(): ProductProperty? {
        return if (hasShortDescription) {
            ComplexProperty(
                R.string.product_short_description,
                shortDescription,
                R.drawable.ic_gridicons_align_left
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductShortDescriptionEditor(
                        shortDescription,
                        resources.getString(R.string.product_short_description)
                    ),
                    Stat.PRODUCT_DETAIL_VIEW_SHORT_DESCRIPTION_TAPPED
                )
            }
        } else {
            null
        }
    }

    // show stock properties as a group if stock management is enabled and if the product type is [SIMPLE],
    // otherwise show sku separately
    private fun Product.inventory(productType: ProductType): ProductProperty {
        val inventory = mutableMapOf<String, String>()

        if (this.sku.isNotEmpty()) {
            inventory[resources.getString(R.string.product_sku)] = this.sku
        }

        if (productType == SIMPLE || productType == VARIABLE) {
            if (this.isStockManaged) {
                inventory[resources.getString(R.string.product_stock_quantity)] =
                    StringUtils.formatCountDecimal(this.stockQuantity)
                inventory[resources.getString(R.string.product_backorders)] =
                    ProductBackorderStatus.backordersToDisplayString(resources, this.backorderStatus)
            } else if (productType == SIMPLE) {
                inventory[resources.getString(R.string.product_stock_status)] =
                    ProductStockStatus.stockStatusToDisplayString(resources, this.stockStatus)
            }
        }

        if (inventory.isEmpty()) {
            inventory[""] = resources.getString(R.string.product_inventory_empty)
        }

        return PropertyGroup(
            R.string.product_inventory,
            inventory,
            R.drawable.ic_gridicons_list_checkmark,
            true
        ) {
            viewModel.onEditProductCardClicked(
                ViewProductInventory(
                    InventoryData(
                        sku = this.sku,
                        isStockManaged = this.isStockManaged,
                        stockStatus = this.stockStatus,
                        stockQuantity = this.stockQuantity,
                        backorderStatus = this.backorderStatus,
                        isSoldIndividually = this.isSoldIndividually
                    ),
                    originalSku,
                    productType
                ),
                PRODUCT_DETAIL_VIEW_INVENTORY_SETTINGS_TAPPED
            )
        }
    }

    private fun Product.shipping(): ProductProperty? {
        return if (!this.isVirtual && hasShipping) {
            val weightWithUnits = this.getWeightWithUnits(parameters.weightUnit)
            val sizeWithUnits = this.getSizeWithUnits(parameters.dimensionUnit)
            val shippingGroup = mapOf(
                Pair(resources.getString(R.string.product_weight), weightWithUnits),
                Pair(resources.getString(R.string.product_dimensions), sizeWithUnits),
                Pair(
                    resources.getString(R.string.product_shipping_class),
                    viewModel.getShippingClassByRemoteShippingClassId(this.shippingClassId)
                )
            )

            PropertyGroup(
                R.string.product_shipping,
                shippingGroup,
                R.drawable.ic_gridicons_shipping
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductShipping(
                        ShippingData(
                            weight,
                            length,
                            width,
                            height,
                            shippingClass,
                            shippingClassId
                        )
                    ),
                    Stat.PRODUCT_DETAIL_VIEW_SHIPPING_SETTINGS_TAPPED
                )
            }
        } else {
            null
        }
    }

    // enable editing external product link
    private fun Product.externalLink(): ProductProperty? {
        return if (this.productType == EXTERNAL) {
            val hasExternalLink = this.externalUrl.isNotEmpty()
            val externalGroup = if (hasExternalLink) {
                mapOf(Pair("", this.externalUrl))
            } else {
                mapOf(Pair("", resources.getString(R.string.product_external_empty_link)))
            }

            PropertyGroup(
                R.string.product_external_link,
                externalGroup,
                R.drawable.ic_gridicons_link,
                hasExternalLink
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductExternalLink(this.remoteId),
                    Stat.PRODUCT_DETAIL_VIEW_EXTERNAL_PRODUCT_LINK_TAPPED
                )
            }
        } else {
            null
        }
    }

    private fun Product.price(): ProductProperty {
        // If we have pricing info, show price & sales price as a group,
        // otherwise provide option to add pricing info for the product
        val pricingGroup = PriceUtils.getPriceGroup(
            parameters,
            resources,
            currencyFormatter,
            regularPrice,
            salePrice,
            isSaleScheduled,
            saleStartDateGmt,
            saleEndDateGmt
        )

        return PropertyGroup(
            R.string.product_price,
            pricingGroup,
            R.drawable.ic_gridicons_money,
            showTitle = this.regularPrice.isSet()
        ) {
            viewModel.onEditProductCardClicked(
                ViewProductPricing(PricingData(
                    taxClass,
                    taxStatus,
                    isSaleScheduled,
                    saleStartDateGmt,
                    saleEndDateGmt,
                    regularPrice,
                    salePrice
                )),
                Stat.PRODUCT_DETAIL_VIEW_PRICE_SETTINGS_TAPPED
            )
        }
    }

    private fun Product.productTypeDisplayName(): String {
        return when (productType) {
            SIMPLE -> {
                when {
                    this.isDownloadable -> resources.getString(R.string.product_type_downloadable)
                    this.isVirtual -> resources.getString(R.string.product_type_virtual)
                    else -> resources.getString(R.string.product_type_physical)
                }
            }
            VARIABLE -> resources.getString(R.string.product_type_variable)
            GROUPED -> resources.getString(R.string.product_type_grouped)
            EXTERNAL -> resources.getString(R.string.product_type_external)
            OTHER -> this.type.capitalize() // show the actual product type string for unsupported products
        }
    }

    private fun Product.productType(): ProductProperty {
        val onClickHandler = {
            viewModel.onEditProductCardClicked(
                ViewProductTypes(false),
                Stat.PRODUCT_DETAIL_VIEW_PRODUCT_TYPE_TAPPED
            )
        }

        return ComplexProperty(
            R.string.product_type,
            resources.getString(R.string.product_detail_product_type_hint, productTypeDisplayName()),
            R.drawable.ic_gridicons_product,
            onClick = if (remoteId != 0L && productType != OTHER) onClickHandler else null
        )
    }

    private fun Product.productReviews(): ProductProperty? {
        return if (this.reviewsAllowed) {
            val value = when (this.ratingCount) {
                0 -> resources.getString(R.string.product_ratings_count_zero)
                1 -> resources.getString(R.string.product_ratings_count_one)
                else -> resources.getString(R.string.product_ratings_count, this.ratingCount)
            }
            RatingBar(
                R.string.product_reviews,
                value,
                this.averageRating,
                R.drawable.ic_reviews
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductReviews(this.remoteId),
                    Stat.PRODUCT_DETAIL_VIEW_PRODUCT_REVIEWS_TAPPED
                )
            }
        } else {
            null
        }
    }

    private fun Product.groupedProducts(): ProductProperty {
        val groupedProductsSize = this.groupedProductIds.size
        val showTitle = groupedProductsSize > 0

        val groupedProductsDesc = if (showTitle) {
            StringUtils.getPluralString(resources, groupedProductsSize, R.plurals.product_count)
        } else {
            resources.getString(R.string.grouped_product_empty)
        }

        return ComplexProperty(
            R.string.grouped_products,
            groupedProductsDesc,
            R.drawable.ic_widgets,
            showTitle = showTitle
        ) {
            viewModel.onEditProductCardClicked(
                ViewGroupedProducts(this.remoteId, this.groupedProductIds),
                Stat.PRODUCT_DETAIL_VIEW_GROUPED_PRODUCTS_TAPPED
            )
        }
    }

    private fun Product.linkedProducts(): ProductProperty? {
        if (!hasLinkedProducts()) return null

        val upsellDesc = StringUtils.getPluralString(
            resources,
            this.upsellProductIds.size,
            R.plurals.upsell_product_count
        )
        val crossSellDesc = StringUtils.getPluralString(
            resources,
            this.crossSellProductIds.size,
            R.plurals.cross_sell_product_count
        )

        return ComplexProperty(
            R.string.product_detail_linked_products,
            "$upsellDesc<br>$crossSellDesc",
            R.drawable.ic_gridicons_reblog,
            maxLines = 2
        ) {
            viewModel.onEditProductCardClicked(
                ViewLinkedProducts(this.remoteId),
                Stat.PRODUCT_DETAIL_VIEW_LINKED_PRODUCTS_TAPPED
            )
        }
    }

    private fun Product.title(): ProductProperty {
        val name = this.name.fastStripHtml()
        return Editable(
            R.string.product_detail_title_hint,
            name,
            onTextChanged = viewModel::onProductTitleChanged
        )
    }

    private fun Product.description(): ProductProperty {
        val productDescription = this.description
        val showTitle = productDescription.isNotEmpty()
        val description = if (productDescription.isEmpty()) {
            resources.getString(R.string.product_description_empty)
        } else {
            productDescription
        }

        return ComplexProperty(
            R.string.product_description,
            description,
            showTitle = showTitle
        ) {
            viewModel.onEditProductCardClicked(
                ViewProductDescriptionEditor(
                    productDescription, resources.getString(R.string.product_description)
                ),
                PRODUCT_DETAIL_VIEW_PRODUCT_DESCRIPTION_TAPPED
            )
        }
    }

    // show product variations only if product type is variable and if there are variations for the product
    private fun Product.variations(): ProductProperty {
        return if (this.numVariations > 0) {
            val properties = mutableMapOf<String, String>()
            for (attribute in this.attributes) {
                properties[attribute.name] = attribute.terms.size.toString()
            }

            PropertyGroup(
                R.string.product_variations,
                properties,
                R.drawable.ic_gridicons_types,
                propertyFormat = R.string.product_variation_options
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductVariations(this.remoteId),
                    Stat.PRODUCT_DETAIL_VIEW_PRODUCT_VARIANTS_TAPPED
                )
            }
        } else {
            emptyVariations()
        }
    }

    private fun Product.emptyVariations(): ComplexProperty {
        return if (ADD_EDIT_VARIATIONS.isEnabled()) {
            ComplexProperty(
                value = resources.getString(string.product_detail_add_variations),
                icon = drawable.ic_gridicons_types,
                showTitle = false,
                onClick = {
                    // TODO call variation creation view sequence
                }
            )
        } else {
            ComplexProperty(
                title = string.product_variations,
                value = resources.getString(string.product_detail_no_variations),
                icon = drawable.ic_gridicons_types,
                onClick = {
                    viewModel.onEditProductCardClicked(
                        ViewProductVariations(this.remoteId),
                        PRODUCT_DETAIL_VIEW_PRODUCT_VARIANTS_TAPPED
                    )
                }
            )
        }
    }

    private fun Product.categories(): ProductProperty? {
        return if (hasCategories) {
            val categories = categories.joinToString(transform = { it.name })

            ComplexProperty(
                R.string.product_categories,
                categories,
                R.drawable.ic_gridicons_folder,
                maxLines = 5
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductCategories(this.remoteId),
                    Stat.PRODUCT_DETAIL_VIEW_CATEGORIES_TAPPED
                )
            }
        } else {
            null
        }
    }

    private fun Product.tags(): ProductProperty? {
        return if (hasTags) {
            val tags = this.tags.joinToString(transform = { it.name })

            ComplexProperty(
                R.string.product_tags,
                tags,
                R.drawable.ic_gridicons_tag,
                maxLines = 5
            ) {
                viewModel.onEditProductCardClicked(
                    ViewProductTags(this.remoteId),
                    Stat.PRODUCT_DETAIL_VIEW_TAGS_TAPPED
                )
            }
        } else {
            null
        }
    }
}
