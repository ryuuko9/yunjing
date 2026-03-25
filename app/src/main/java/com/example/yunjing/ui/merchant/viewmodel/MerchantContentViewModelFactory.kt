package com.example.yunjing.ui.merchant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yunjing.ui.merchant.repository.MerchantContentRepository

class MerchantContentViewModelFactory(
    private val repository: MerchantContentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MerchantContentViewModel::class.java)) {
            return MerchantContentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}