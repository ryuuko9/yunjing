package com.example.yunjing.ui.merchant.content.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yunjing.ui.merchant.content.repository.MerchantContentRepository

/**
 * 本文件负责为内容库页面创建共享的 MerchantContentViewModel 实例。
 */
class MerchantContentViewModelFactory(
    private val repository: MerchantContentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        /**
         * 这个函数负责按类型创建内容库 ViewModel，并把仓库依赖注入进去。
         */
        if (modelClass.isAssignableFrom(MerchantContentViewModel::class.java)) {
            return MerchantContentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
