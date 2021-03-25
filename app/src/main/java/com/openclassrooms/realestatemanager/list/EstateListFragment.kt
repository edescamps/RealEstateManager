package com.openclassrooms.realestatemanager.list

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.database.Estate
import com.openclassrooms.realestatemanager.databinding.FragmentListBinding
import com.openclassrooms.realestatemanager.detail.DetailFragment

class EstateListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding
    private lateinit var navController: NavController
    private lateinit var estateListViewModel: EstateListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true);

        // Inflate view and obtain an instance of the binding class
        binding = FragmentListBinding.inflate(layoutInflater)

        // Handle estate item by adapter
        val estateListAdapter = EstateListAdapter { estate -> adapterOnClick(estate) }
        binding.recyclerviewEstateList.adapter = estateListAdapter
        binding.recyclerviewEstateList.layoutManager = LinearLayoutManager(context)

        // Get the viewModel
        estateListViewModel = ViewModelProvider(this).get(EstateListViewModel::class.java)

        // Observe data modification in the VM
        estateListViewModel.allEstates.observe(viewLifecycleOwner, {
            it?.let {
                estateListAdapter.submitList(it as MutableList<Estate>)
            }
        })

        return binding.root
    }

    private fun adapterOnClick(estate: Estate) {
        // TODO() Provide the estate to the DetailFragment (arguments in Navigation ?)
        if (activity?.resources?.getBoolean(R.bool.is_landscape) == true) {
            // TODO() Handle dpi AND orientation
            // no navigation

        } else {
            NavHostFragment
                    .findNavController(this)
                    .navigate(R.id.action_listFragment_to_detailFragment)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_list, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handle item clicks of menu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id to handle item clicks
        when (item.itemId) {

            R.id.add_estate -> {
                //Open CreationFragment
                Log.i("EstateListFragment", "Click on create a new estate")
                NavHostFragment.findNavController(this).navigate(R.id.action_listFragment_to_creationFragment)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(
                requireActivity(),
                R.id.nav_host_fragment
        )
    }

    override fun onResume() {
        super.onResume()
        // true only in landscape
        if (binding.detailFragmentContainer != null) {
            childFragmentManager.beginTransaction()
                    .replace(binding.detailFragmentContainer!!.id, DetailFragment())
                    .commit()
        }
    }


}

