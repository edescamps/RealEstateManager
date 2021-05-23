package com.openclassrooms.realestatemanager.detail

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import coil.load
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.database.model.DetailedEstate
import com.openclassrooms.realestatemanager.database.model.Picture
import com.openclassrooms.realestatemanager.databinding.FragmentDetailBinding
import com.openclassrooms.realestatemanager.utils.StaticMapBuilder.Companion.buildUrl
import com.openclassrooms.realestatemanager.utils.Utils
import com.openclassrooms.realestatemanager.utils.Utils.getFormattedDateFromMillis
import com.openclassrooms.realestatemanager.viewmodel.ListDetailViewModel
import java.util.*

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: ListDetailViewModel by activityViewModels()
    private lateinit var detailedEstate: DetailedEstate
    private val pictureListAdapter = DetailPictureListAdapter(DetailPictureListener { picture ->
        // TODO() viewModel.onPictureClicked(picture)
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initBindings()
        getEstate()
        setBackNav()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // If TabletMode, Menu managed by EstateList
        if (!requireContext().resources.getBoolean(R.bool.isTablet)) {
            inflater.inflate(R.menu.menu_fragment_detail, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id to handle item clicks
        when (item.itemId) {
            R.id.edit_estate -> {
                //Open CreationFragment for Edition
                Log.i("DetailFragment", "Click on edit an estate")
                if (this@DetailFragment.findNavController().currentDestination?.id == R.id.detailFragment) {
                    NavHostFragment.findNavController(this)
                        .navigate(
                            DetailFragmentDirections
                                .actionDetailFragmentToCreationFragment(detailedEstate.estate!!.startTime)
                        )
                }
            }

            R.id.convert_price -> {
                with(binding.detailPriceContent.text) {
                    if (this.contains("$")) { // convert to euro
                        item.icon = getDrawable(requireContext(), R.drawable.ic_baseline_dollar_24)
                        binding.detailPriceImage.setImageDrawable(
                            getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_euro_24
                            )
                        )

                        binding.detailPriceContent.text = getString(
                            R.string.detail_estate_price_euro,
                            detailedEstate.estate?.estatePrice?.let { Utils.convertDollarToEuro(it) }
                                .toString()
                        )
                    } else { // convert to dollar
                        item.icon = getDrawable(requireContext(), R.drawable.ic_baseline_euro_24)
                        binding.detailPriceImage.setImageDrawable(
                            getDrawable(
                                requireContext(),
                                R.drawable.ic_baseline_dollar_24
                            )
                        )
                        binding.detailPriceContent.text = getString(
                            R.string.detail_estate_price_dollar,
                            detailedEstate.estate?.estatePrice.toString()
                        )
                    }
                }
            }

            android.R.id.home -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                return true;
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initBindings() {
        binding = FragmentDetailBinding.inflate(layoutInflater)

        binding.detailRecyclerviewPictures.adapter = pictureListAdapter
        val mLayoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        binding.detailRecyclerviewPictures.layoutManager = mLayoutManager
    }

    private fun getEstate() {
        if (viewModel.navigateToEstateDetail.value != null) {
            detailedEstate = viewModel.navigateToEstateDetail.value!!
            bindEstate()
        } else {
            viewModel.getEstate(args.estateKey).observe(viewLifecycleOwner, {
                detailedEstate = it
                bindEstate()
                //save estate in VM
                viewModel.onEstateClicked(it)
            })
        }
    }

    private fun bindEstate() {
        binding.detailedEstate = this.detailedEstate
        binding.detailEstateScrollview.visibility = View.VISIBLE
        pictureListAdapter.submitList(detailedEstate.pictures as MutableList<Picture>)
        bindDates()
        bindPois()
        bindMapView()
        binding.executePendingBindings()
    }

    private fun bindMapView() {
        val staticUrl = buildUrl(detailedEstate)
        binding.detailMapView.load(staticUrl)
    }

    private fun bindDates() {
        // Start Time
        binding.detailDateStart.text = getString(
            R.string.detail_date_start,
            getFormattedDateFromMillis(detailedEstate.estate?.startTime!!)
        )

        if (detailedEstate.estate!!.endTime != null) {
            binding.detailDateSold.text = getString(
                R.string.detail_date_sold,
                getFormattedDateFromMillis(detailedEstate.estate?.endTime!!)
            )
            binding.detailDateSold.visibility = View.VISIBLE
        }
    }

    private fun bindPois() {
        val estatePoisIdList = detailedEstate.estate?.estatePois!!.split("|")
        val poisStr = StringBuilder()
        viewModel.allPois().observe(viewLifecycleOwner, {
            for (poi in it) {
                if (estatePoisIdList.contains(poi.poiId.toString())) {
                    if (poisStr.isEmpty()) {
                        poisStr.append(poi.poiName)
                    } else {
                        poisStr.append(", " + poi.poiName)
                    }
                }
            }
            binding.detailPoisContent.text = poisStr
        })

    }

    private fun setBackNav() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (this@DetailFragment.findNavController().currentDestination?.id == R.id.detailFragment) {
                NavHostFragment.findNavController(requireParentFragment())
                    .navigate(R.id.action_detailFragment_to_listFragment)
                viewModel.navigateToEstateDetail.value = null
            }
        }
    }

    override fun onResume() {
        if (this@DetailFragment.findNavController().currentDestination?.id == R.id.listFragment) {
            // do nothing
        } else {
            if (requireContext().resources.getBoolean(R.bool.isTablet)) {
                NavHostFragment.findNavController(requireParentFragment())
                    .navigate(
                        DetailFragmentDirections
                            .actionDetailFragmentToListFragment(detailedEstate.estate?.startTime!!)
                    )
            }
        }
        super.onResume()
    }
}