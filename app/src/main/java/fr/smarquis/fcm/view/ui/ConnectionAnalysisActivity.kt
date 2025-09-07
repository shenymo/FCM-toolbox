package fr.smarquis.fcm.view.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import fr.smarquis.fcm.R
import fr.smarquis.fcm.databinding.ActivityConnectionAnalysisBinding
import fr.smarquis.fcm.view.ui.fragments.ConnectionChartsFragment
import fr.smarquis.fcm.view.ui.fragments.ConnectionHistoryFragment
import fr.smarquis.fcm.view.ui.fragments.ConnectionOverviewFragment

class ConnectionAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        binding = ActivityConnectionAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        setupWindowInsets()
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
