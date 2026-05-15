package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nammasanthe.data.repository.LedgerRepository

class ViewModelFactory(private val repository: LedgerRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(CustomerViewModel::class.java) ->
                CustomerViewModel(repository) as T
            modelClass.isAssignableFrom(CustomerProfileViewModel::class.java) ->
                CustomerProfileViewModel(repository) as T
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(repository) as T
            modelClass.isAssignableFrom(OverdueViewModel::class.java) ->
                OverdueViewModel(repository) as T
            // ✅ Added HistoryViewModel
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
