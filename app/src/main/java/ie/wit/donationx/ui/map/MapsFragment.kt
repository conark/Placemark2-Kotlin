package ie.wit.donationx.ui.map

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ie.wit.donationx.R
import ie.wit.donationx.models.DonationModel
import ie.wit.donationx.ui.auth.LoggedInViewModel
import ie.wit.donationx.ui.report.ReportViewModel
import ie.wit.donationx.utils.createLoader
import ie.wit.donationx.utils.hideLoader
import ie.wit.donationx.utils.showLoader

class MapsFragment : Fragment() {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()
    private val loggedInViewModel : LoggedInViewModel by activityViewModels()
    lateinit var loader : AlertDialog

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mapsViewModel.map = googleMap
        mapsViewModel.map.isMyLocationEnabled = true
        mapsViewModel.currentLocation.observe(viewLifecycleOwner) {
            val loc = LatLng(
                mapsViewModel.currentLocation.value!!.latitude,
                mapsViewModel.currentLocation.value!!.longitude
            )

            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
            mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
            mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true

            reportViewModel.observableDonationsList.observe(
                viewLifecycleOwner
            ) { donations ->
                donations?.let {
                    render(donations as ArrayList<DonationModel>)
                    hideLoader(loader)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loader = createLoader(requireActivity())
        setupMenu()
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun render(donationsList: ArrayList<DonationModel>) {
        var markerColour: Float
        if (donationsList.isNotEmpty()) {
            mapsViewModel.map.clear()
            donationsList.forEach {
                markerColour = if(it.email.equals(this.reportViewModel.liveFirebaseUser.value!!.email))
                    BitmapDescriptorFactory.HUE_AZURE + 5
                else
                    BitmapDescriptorFactory.HUE_RED

                mapsViewModel.map.addMarker(
                    MarkerOptions().position(LatLng(it.latitude, it.longitude))
                        .title("${it.paymentmethod} €${it.amount}")
                        .snippet(it.message)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColour ))
                )
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_report, menu)

                val item = menu.findItem(R.id.toggleDonations) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleDonations: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleDonations.isChecked = false

                toggleDonations.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) reportViewModel.loadAll()
                    else reportViewModel.load()
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Donations")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) {
                firebaseUser -> if (firebaseUser != null) {
                reportViewModel.liveFirebaseUser.value = firebaseUser
                reportViewModel.load()
            }
        }
    }
}