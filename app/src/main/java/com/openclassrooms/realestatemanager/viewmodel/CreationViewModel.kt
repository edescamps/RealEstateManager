package com.openclassrooms.realestatemanager.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.openclassrooms.realestatemanager.database.EstateDatabase.Companion.getDatabase
import com.openclassrooms.realestatemanager.database.model.*
import com.openclassrooms.realestatemanager.repository.*
import com.openclassrooms.realestatemanager.utils.Source
import com.openclassrooms.realestatemanager.utils.copyImageFromStream
import com.openclassrooms.realestatemanager.utils.generateFilename
import com.openclassrooms.realestatemanager.utils.getImagesFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class CreationViewModel(application: Application) : AndroidViewModel(application) {

    private val estateDao = getDatabase(application, viewModelScope).estateDao()
    private val estateRepository: EstateRepository = EstateRepository(estateDao)

    private val pictureDao = getDatabase(application, viewModelScope).pictureDao()
    private val pictureRepository: PictureRepository = PictureRepository(pictureDao)

    private val typeDao = getDatabase(application, viewModelScope).typeDao()
    private val typeRepository: TypeRepository = TypeRepository(typeDao)

    private val employeeDao = getDatabase(application, viewModelScope).employeeDao()
    private val employeeRepository: EmployeeRepository = EmployeeRepository(employeeDao)

    private val poiDao = getDatabase(application, viewModelScope).poiDao()
    private val poiRepository: PoiRepository = PoiRepository(poiDao)

    private val imagesFolder: File by lazy { getImagesFolder(getApplication()) }
    private val context: Context
        get() = getApplication()


    //------------------------------------------------------//

    fun allTypes(): LiveData<List<Type>> = typeRepository.allTypes
    fun allEmployees(): LiveData<List<Employee>> = employeeRepository.allEmployees
    fun allPois(): LiveData<List<Poi>> = poiRepository.allPois


    //--------------- EDIT MODE FUNCTIONS ------------------//

    fun getEstate(estateKey: Long): LiveData<DetailedEstate> =
        estateRepository.getLiveEstate(estateKey)

    fun getEstatePictures(estateKey: Long): LiveData<List<Picture>> =
        pictureRepository.getEstatePictures(estateKey)

    //--------------- CREATION & EDITION ------------------//

    fun saveEstate(estate: Estate, listPicture: List<Picture>, pois: List<Int>) {
        var estateId = 0L
        viewModelScope.launch(Dispatchers.IO) {
            estateId = estateRepository.insert(estate)
            Log.i(
                "CreationViewModel ",
                "added a new estate with id ${estate.startTime}"
            )
            if (estateId == estate.startTime) onEstateUpdated(estate)
        }
        updatePois(estate, pois)
        savePictures(listPicture)

    }

    private fun updatePois(estate: Estate, pois: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            estateRepository.deleteEstatePoi(estate.startTime)
        }

        for (poi in pois) {
            viewModelScope.launch(Dispatchers.IO) {
                estateRepository.insert(estate.startTime, poi)
            }
        }
    }


    private fun savePictures(listPicture: List<Picture>) {
        for (picture in listPicture) {
            savePicture(picture)
        }
    }

    fun savePicture(picture: Picture) {
        // check if picture is already stored in app folder, otherwise copy file in it
        if (!picture.url.contains(imagesFolder.path, true) &&
            !picture.url.contains("http", true)
        ) // prevent issue with network files from autogenerated database
            picture.url = copyImageFromUriToAppFolder(Uri.parse(picture.url))
        insertPicture(picture)
    }

    private fun insertPicture(picture: Picture) {
        viewModelScope.launch(Dispatchers.IO) { pictureRepository.insert(picture) }
    }


//----------------- MANAGE PICTURES --------------------//


    fun saveImageFromCamera(bitmap: Bitmap): String {
        val imageFile = File(imagesFolder, generateFilename(Source.CAMERA))
        val imageStream = FileOutputStream(imageFile)
        Log.i("TakePicture", "$imageFile")

        compressPicture(bitmap, imageStream)
        return imageFile.path
    }

    private fun compressPicture(bitmap: Bitmap, imageStream: FileOutputStream) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageStream)
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "Error writing bitmap", e)
                }
            }
        }
    }

    private fun copyImageFromUriToAppFolder(uri: Uri): String {
        val imageFile = File(imagesFolder, generateFilename(Source.PICKER))
        val imageStream = FileOutputStream(imageFile)

        context.contentResolver.openInputStream(uri)?.let {
            // TODO() compress picture copied to app folder
//            compressPicture(BitmapFactory.decodeStream(it), imageStream)
            copyImageFromStream(it, imageStream)
        }
        return imageFile.toString()
    }


    fun deletePicture(picture: Picture) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myFile = File(picture.url)
                if (myFile.exists()) myFile.delete()
                pictureRepository.deletePicture(picture)
            }
        }
    }

//------------------ NOTIFICATIONS ---------------------//

    /**
     * Navigation notification
     */
    private val _navigateToEstateDetail = MutableLiveData<DetailedEstate>()
    val navigateToEstateDetail
        get() = _navigateToEstateDetail

    private fun onEstateUpdated(estate: Estate) {
        viewModelScope.launch(Dispatchers.IO) {
            _navigateToEstateDetail.postValue(estateRepository.getEstate(estate.startTime))
        }
    }


}