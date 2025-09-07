package fr.smarquis.fcm.view.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import fr.smarquis.fcm.R
import fr.smarquis.fcm.data.model.ConnectionHistory
import fr.smarquis.fcm.data.model.ConnectionStatus
import fr.smarquis.fcm.data.model.NetworkType
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.databinding.ActivityConnectionAnalysisBinding
import fr.smarquis.fcm.view.ui.fragments.ConnectionChartsFragment
import fr.smarquis.fcm.view.ui.fragments.ConnectionHistoryFragment
import fr.smarquis.fcm.view.ui.fragments.ConnectionOverviewFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ConnectionAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionAnalysisBinding
    private val repository: ConnectionHistoryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        binding = ActivityConnectionAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        setupWindowInsets()
        setupTestButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.connection_analysis_title)
        }
    }

    private fun setupViewPager() {
        val adapter = ConnectionAnalysisPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_overview)
                1 -> getString(R.string.tab_history)
                2 -> getString(R.string.tab_charts)
                else -> ""
            }
        }.attach()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupTestButton() {
        binding.fabTest.setOnClickListener {
            insertTestData()
        }
    }

    private fun insertTestData() {
        lifecycleScope.launch {
            try {
                Log.d("ConnectionAnalysis", "Inserting test data...")

                val currentTime = System.currentTimeMillis()
                val testData = listOf(
                    // 今天的连接记录
                    ConnectionHistory(
                        timestamp = currentTime - 7200000, // 2小时前
                        status = ConnectionStatus.CONNECTED,
                        networkType = NetworkType.WIFI,
                        deviceInfo = "Test Device"
                    ),
                    ConnectionHistory(
                        timestamp = currentTime - 3600000, // 1小时前
                        status = ConnectionStatus.DISCONNECTED,
                        networkType = NetworkType.WIFI,
                        duration = 3600000, // 1小时连接时长
                        deviceInfo = "Test Device"
                    ),
                    // 另一次连接
                    ConnectionHistory(
                        timestamp = currentTime - 1800000, // 30分钟前
                        status = ConnectionStatus.CONNECTED,
                        networkType = NetworkType.MOBILE,
                        deviceInfo = "Test Device"
                    ),
                    ConnectionHistory(
                        timestamp = currentTime - 900000, // 15分钟前
                        status = ConnectionStatus.DISCONNECTED,
                        networkType = NetworkType.MOBILE,
                        duration = 900000, // 15分钟连接时长
                        deviceInfo = "Test Device"
                    )
                )

                repository.insertAll(testData)
                Log.d("ConnectionAnalysis", "Test data inserted successfully")

                // 刷新当前显示的Fragment
                recreate()

            } catch (e: Exception) {
                Log.e("ConnectionAnalysis", "Error inserting test data", e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private class ConnectionAnalysisPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ConnectionOverviewFragment()
                1 -> ConnectionHistoryFragment()
                2 -> ConnectionChartsFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }
}
