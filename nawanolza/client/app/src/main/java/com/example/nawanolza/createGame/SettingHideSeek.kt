package com.example.nawanolza.createGame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.nawanolza.*
import com.example.nawanolza.R
import com.example.nawanolza.databinding.ActivitySettingHideSeekBinding
import com.example.nawanolza.hideandseek.MessageSenderService
import com.example.nawanolza.login.LoginUtil
import com.example.nawanolza.retrofit.RetrofitConnection
import com.example.nawanolza.retrofit.createroom.CreateRoomHideResponse
import com.example.nawanolza.retrofit.createroom.CreateRoomHideService
import com.example.nawanolza.retrofit.createroom.CreateRoomRequest
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_setting_hide_seek.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingHideSeek : OnMapReadyCallback, AppCompatActivity() {
    val permission_request = 99
    var check: Boolean = false
    var circle = CircleOverlay()
    var lat: Double? = null
    var lng: Double? = null

    private lateinit var naverMap: NaverMap
    private lateinit var createRoomRequest: CreateRoomRequest
    private lateinit var locationSource: FusedLocationSource

    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding = ActivitySettingHideSeekBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var time = 60
        var gameTime = 300
        var range = 100

        gameTime = setGameTime(gameTime, binding)
        range = setRange(range, binding)

        val retrofitAPI = RetrofitConnection.getInstance().create(
            CreateRoomHideService::class.java
        )
        postGameInfo(gameTime, time, range, retrofitAPI)

        //?????? ??????
        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }
    }

    private fun postGameInfo(
        gameTime: Int,
        time: Int,
        range: Int,
        retrofitAPI: CreateRoomHideService
    ) {
        btnCreateRoom.setOnClickListener {
            if (check) {
                val hostId = LoginUtil.getMember(this@SettingHideSeek)!!.id
                createRoomRequest = CreateRoomRequest(lat, lng, gameTime, time, range, hostId)
                retrofitAPI.postCreateRoomHide(createRoomRequest)
                    .enqueue(object : Callback<CreateRoomHideResponse> {
                        override fun onResponse(
                            call: Call<CreateRoomHideResponse>,
                            response: Response<CreateRoomHideResponse>
                        ) {
                            val intent = Intent(this@SettingHideSeek, Waiting::class.java)
                            intent.putExtra("entryCode", "${response.body()?.entryCode}")
                            intent.putExtra("data", GsonBuilder().create().toJson(response.body()))

                            when (response.code()) {
                                200 -> {
                                    startActivity(intent)
                                    MessageSenderService.sendMessageToWearable(
                                        "/message_path",
                                        "w",
                                        this@SettingHideSeek
                                    )
                                }
                                else -> Toast.makeText(
                                    this@SettingHideSeek,
                                    "????????? ???????????????.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<CreateRoomHideResponse>, t: Throwable) {
                            println(call)
                            println(t)
                        }
                    })
            } else {
                Toast.makeText(this, "????????? ??????????????????", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setRange(
        range: Int,
        binding: ActivitySettingHideSeekBinding
    ): Int {
        var range1 = range
        rangeMinus.setOnClickListener {
            if (range1 <= 50) {
            } else {
                range1 -= 50
                binding.rangeText.text = range1.toString()
            }
        }
        rangePlus.setOnClickListener {
            if (range1 <= 150.0) {
                range1 += 50
                binding.rangeText.text = range1.toString()
            }
        }
        return range1
    }

    private fun setGameTime(
        gameTime: Int,
        binding: ActivitySettingHideSeekBinding
    ): Int {
        var gameTime1 = gameTime
        gamePlus.setOnClickListener {
            gameTime1 += 30
            var gameMinute = if (gameTime1 % 60 == 0) "00" else gameTime1 % 60
            binding.gameTime.text = "${gameTime1 / 60}:${gameMinute}"
        }
        gameMinus.setOnClickListener {
            if (gameTime1 > 30) {
                gameTime1 -= 30
                var gameMinute = if (gameTime1 % 60 == 0) "00" else gameTime1 % 60
                binding.gameTime.text = "${gameTime1 / 60}:${gameMinute}"
            } else {
                binding.gameTime.text = "0:30"
            }
        }
        return gameTime1
    }

    fun isPermitted(): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }//????????? ?????? ????????????

    fun startProcess(){
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            } //??????
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, 1000) // ?????? ???????????? ?????? ????????? ??????
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap){
        this.naverMap = naverMap

//        val cameraPosition = CameraPosition(
//            LatLng(36.1071562, 128.4164185),  // ?????? ??????
//            16.0 // ??? ??????
//        )
//        naverMap.cameraPosition = cameraPosition

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true //?????? ?????? ?????????

        naverMap.locationSource = locationSource // ?????? ???????????? ?????? ????????? ??????


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) //gps ???????????? ????????????
        setUpdateLocationListener() //???????????? ???????????? ??????

        naverMap.setOnMapClickListener { point, coord ->
            val latLng = LatLng(coord.latitude, coord.longitude)
            lat = coord.latitude
            lng = coord.longitude
            setPolyline(latLng)
        }
    }
    //??? ????????? ???????????? ??????
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //???????????? gps?????? ????????????.
    lateinit var locationCallback: LocationCallback //gps?????? ?????? ????????????.

    //lateinit: ????????? ????????? ??????????????? ??????
    @SuppressLint("MissingPermission")
    fun setUpdateLocationListener() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //?????? ?????????
//            interval = 1000 //1?????? ????????? GPS ??????
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    setLastLocation(location)
                }
            }
        }

        //location ?????? ?????? ?????? (locationRequest, locationCallback)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }//???????????? ??????????????? ??????

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 16.0
    }

    private fun setPolyline(latLng:LatLng){
        if(!check){
            circle.center = latLng
            val color = Color.parseColor("#80ce93d8")
            val outlineColor = Color.parseColor("#8e24aa")
            circle.outlineColor = outlineColor
            circle.outlineWidth = 2
            circle.color = color
            circle.radius = rangeText.text.toString().toDouble()
            circle.map = naverMap
            check = true
        }else{
            circle.map = null
            check = false
        }
    }
}

