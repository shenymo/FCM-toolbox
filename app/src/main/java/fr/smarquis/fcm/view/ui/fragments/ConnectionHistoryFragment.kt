package fr.smarquis.fcm.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.databinding.FragmentConnectionHistoryBinding
import fr.smarquis.fcm.view.adapter.ConnectionHistoryAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ConnectionHistoryFragment : Fragment() {

    private var _binding: FragmentConnectionHistoryBinding? = null
    private val binding get() = _binding!!
    
    private val repository: ConnectionHistoryRepository by inject()
    private lateinit var adapter: ConnectionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadConnectionHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = ConnectionHistoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConnectionHistoryFragment.adapter
        }
    }

    private fun loadConnectionHistory() {
        lifecycleScope.launch {
            repository.getRecentHistory(100).collectLatest { historyList ->
                adapter.submitList(historyList)
                binding.emptyView.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
