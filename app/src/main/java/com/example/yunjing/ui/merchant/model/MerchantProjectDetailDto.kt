package com.example.yunjing.ui.merchant.model

data class MerchantProjectDetailDto(
    val project: MerchantProjectDto,
    val mediaAssets: List<ProjectMediaAssetDto>,
    val modelAssets: List<ProjectModelAssetDto>
)