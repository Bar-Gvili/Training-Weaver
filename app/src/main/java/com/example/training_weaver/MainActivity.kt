package com.training_weaver

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.training_weaver.databinding.ActivityMainBinding
import androidx.navigation.ui.navigateUp


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)

        // NavHost + NavController (this is the correct way in an Activity)
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        // Drawer toggle (hamburger)
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // AppBarConfiguration lets NavigationUI know about the drawer
        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Hook the NavigationView to the NavController
        binding.navView.setupWithNavController(navController)

        // Lock the drawer on the login screen
        navController.addOnDestinationChangedListener { _, dest, _ ->
            val lock = dest.id == R.id.loginFragment
            binding.drawerLayout.setDrawerLockMode(
                if (lock) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )
            if (lock) {
                // remove hamburger/back icon
                binding.toolbar.navigationIcon = null
            } else {
                toggle.syncState()
            }
        }

        // (Optional) If you still want manual item handling instead of menu xml destinations:
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_workout_playlists -> navController.navigate(R.id.workoutListFragment)
                R.id.nav_exercises_dataset -> navController.navigate(R.id.exerciseDatabaseFragment)
                R.id.nav_settings -> navController.navigate(R.id.settingsFragment)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Handle the Up button with the drawer/graph
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }
}
