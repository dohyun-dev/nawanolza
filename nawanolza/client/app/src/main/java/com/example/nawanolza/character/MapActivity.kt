package com.example.nawanolza.character

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.nawanolza.R
import com.example.nawanolza.hideandseek.MainHideSeek
import com.example.nawanolza.login.LoginUtil
import com.example.nawanolza.minigame.CalcGameActivity
import com.example.nawanolza.minigame.CardGameActivity
import com.example.nawanolza.minigame.GameBoomActivity
import com.example.nawanolza.minigame.NumberPuzzleGameActivity
import com.example.nawanolza.quest.QuestService
import com.example.nawanolza.quest.QuizActivity
import com.example.nawanolza.retrofit.CharacterLocationResponse
import com.example.nawanolza.retrofit.CharacterLocationResponseItem
import com.example.nawanolza.retrofit.MemberResponse
import com.example.nawanolza.retrofit.QuestResponse
import com.example.nawanolza.stomp.MarkerDTO
import com.example.nawanolza.stomp.waitingstomp.WaitingStompClient
import com.google.android.gms.location.*
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.dialog_success.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class MapActivity :OnMapReadyCallback, AppCompatActivity() {
    val permission_request = 99
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    var quizInfo = QuestResponse()

    val markerMap: HashMap<Long, Marker> = HashMap()


    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

    var characterInfo = CharacterLocationResponse()

    var memberInfo: MemberResponse? = null

    var currentMarker by Delegates.notNull<Long>()

    lateinit var clickMarkerInfo: Marker

    lateinit var activityResult: ActivityResultLauncher<Intent>

    lateinit var curMyLocation: Location

    //????????? ????????????
    var retrofit = Retrofit.Builder()
        .baseUrl("https://k7d103.p.ssafy.io/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_map)
        WaitingStompClient.connect()
        messageSubscribe()

        memberInfo = LoginUtil.getMemberInfo(this)
        val url = memberInfo!!.member.image

        if (isPermitted()) {
            startProcess()
        } else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }//?????? ??????

        Glide.with(this)
            .load(url) // ????????? ????????? url
            .circleCrop() // ???????????? ?????????
            .into(CircleImageView) // ???????????? ?????? ???



        var service = retrofit.create(GetCharacterService::class.java)



        service.GetCharacter().enqueue(object:Callback<CharacterLocationResponse> {

            override fun onResponse(
                call: Call<CharacterLocationResponse>,
                response: Response<CharacterLocationResponse>
            ) {
                characterInfo = response.body() ?: CharacterLocationResponse()

                updateMapMarkers(characterInfo)
            }

            override fun onFailure(call: Call<CharacterLocationResponse>, t: Throwable) {
                println(call)
                println(t)
            }

        })

        CircleImageView.setOnClickListener{
            val intent = Intent(this@MapActivity, CharacterActivity::class.java)
            intent.apply {
                putExtra("memberInfo", memberInfo)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }

        activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            println("????????? ?????? ??????")
            val booleanExtra = it.data?.getBooleanExtra("result", false)
            println(booleanExtra)
            if (it.resultCode == RESULT_OK && booleanExtra == true) {

                val dialog = AlertDialog.Builder(this@MapActivity).apply {

                }.create()



                dialog.window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                dialog.apply {

                    val dialogView = layoutInflater.inflate(R.layout.dialog_success,null)
                    dialogView.character.setImageResource(MarkerImageUtil.getImage(currentMarker))
                    setView(dialogView)

                    show()
                }



            }
            else{
                val dialog = AlertDialog.Builder(this@MapActivity).apply {

                }.create()


                dialog.window?.apply {
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                dialog.apply {
                    setView(layoutInflater.inflate(R.layout.dialog_fail,null))

                    show()
                }
            }
        }
    }

    private fun updateMapMarkers(characterInfo: CharacterLocationResponse) {
        if(characterInfo != null  && characterInfo.size>0){
            for(current in characterInfo){
                val marker = Marker()
                marker.position= LatLng(current.lat, current.lng)
                marker.width  = 250
                marker.height = 250


                marker.icon = OverlayImage.fromResource(MarkerImageUtil.getImage(current.characterId) as Int)
                marker.map = naverMap
                marker.tag = current

                markerMap[current.markerId] = marker


                val function: (Overlay) -> Boolean = { o ->

                    currentMarker = (marker.tag as CharacterLocationResponseItem).characterId
                    clickMarkerInfo = marker


                    var service = retrofit.create(QuestService::class.java)

                    var markerInfo = marker.tag as CharacterLocationResponseItem


                    service.GetQuiz(mapOf("markerId" to markerInfo.markerId.toString(), "questType" to markerInfo.questType.toString())).enqueue(object: Callback<QuestResponse> {

                        override fun onResponse(
                            call: Call<QuestResponse>,
                            response: Response<QuestResponse>
                        ) {

                            quizInfo = response.body() ?: QuestResponse()

                            checkQuestType()
                        }

                        private fun checkQuestType() {

                            if (!quizInfo.accessible) {
                                val dialog = AlertDialog.Builder(this@MapActivity).apply {

                                }.create()


                                dialog.window?.apply {
                                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                }

                                dialog.apply {
                                    setView(layoutInflater.inflate(R.layout.dialog_play, null))

                                    show()
                                }
                            } else {
                                if (markerInfo.questType == 0) {
                                    val intent = Intent(this@MapActivity, QuizActivity::class.java)

                                    intent.putExtra(
                                        "markerId",
                                        (marker.tag as CharacterLocationResponseItem).markerId
                                    )

                                    intent.putExtra(
                                        "characterId",
                                        (marker.tag as CharacterLocationResponseItem).characterId
                                    )
                                    intent.putExtra(
                                        "quizInfo",
                                        GsonBuilder().create().toJson(quizInfo)
                                    )



                                    activityResult.launch(intent)
                                } else if (markerInfo.questType == 1) {
                                    val intent =
                                        Intent(this@MapActivity, GameBoomActivity::class.java)

                                    intent.putExtra(
                                        "markerId",
                                        (marker.tag as CharacterLocationResponseItem).markerId
                                    )

                                    intent.putExtra(
                                        "characterId",
                                        (marker.tag as CharacterLocationResponseItem).characterId
                                    )


                                    activityResult.launch(intent)
                                } else if (markerInfo.questType == 2) {
                                    val intent =
                                        Intent(this@MapActivity, CardGameActivity::class.java)

                                    intent.putExtra(
                                        "markerId",
                                        (marker.tag as CharacterLocationResponseItem).markerId
                                    )

                                    intent.putExtra(
                                        "characterId",
                                        (marker.tag as CharacterLocationResponseItem).characterId
                                    )


                                    activityResult.launch(intent)
                                } else if (markerInfo.questType == 3) {
                                    val intent =
                                        Intent(this@MapActivity, CalcGameActivity::class.java)

                                    intent.putExtra(
                                        "markerId",
                                        (marker.tag as CharacterLocationResponseItem).markerId
                                    )

                                    intent.putExtra(
                                        "characterId",
                                        (marker.tag as CharacterLocationResponseItem).characterId
                                    )


                                    activityResult.launch(intent)
                                } else if (markerInfo.questType == 4) {
                                    val intent =
                                        Intent(this@MapActivity, NumberPuzzleGameActivity::class.java)

                                    intent.putExtra(
                                        "markerId",
                                        (marker.tag as CharacterLocationResponseItem).markerId
                                    )

                                    intent.putExtra(
                                        "characterId",
                                        (marker.tag as CharacterLocationResponseItem).characterId
                                    )


                                    activityResult.launch(intent)
                                }
                            }
                        }

                        override fun onFailure(call: Call<QuestResponse>, t: Throwable) {
                            println(call)
                            println(t)

                        }
                    })

                    true

                }
                marker.setOnClickListener {
                    val lat = (it.tag as CharacterLocationResponseItem).lat
                    val lng = (it.tag as CharacterLocationResponseItem).lng
                    if (MainHideSeek.DistanceManager.getDistance(curMyLocation.latitude, curMyLocation.longitude, lat, lng) <= 250)
                        function(it)
                    else{
                        val dialog = AlertDialog.Builder(this@MapActivity).apply {
                        }.create()

                        dialog.window?.apply {
                            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        }

                        dialog.apply {
                            setView(layoutInflater.inflate(R.layout.dialog_distance_notice,null))
                            show()
                        }
                    }

                    true
                }
            }
        }
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

        val cameraPosition = CameraPosition(
            LatLng(36.1071562, 128.4164185),  // ?????? ??????
            16.0 // ??? ??????
        )
        naverMap.cameraPosition = cameraPosition


        this.naverMap = naverMap

        val uiSettings = naverMap.uiSettings
        uiSettings.isLocationButtonEnabled = true //?????? ?????? ?????????

        naverMap.locationSource = locationSource // ?????? ???????????? ?????? ????????? ??????
        naverMap.locationTrackingMode = LocationTrackingMode.Follow


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) //gps ???????????? ????????????
        setUpdateLocationListener() //???????????? ???????????? ??????
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
            interval = 1000 //1?????? ????????? GPS ??????
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for ((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    curMyLocation = location
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
        val marker = Marker()
        marker.position = myLocation
        marker.map = null
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0

        updateLocationOverlay(location)
    }

    private fun updateLocationOverlay(location: Location){
        val myLocation = LatLng(location.latitude, location.longitude)
        naverMap.locationOverlay.position = LatLng(myLocation.latitude, myLocation.longitude)
        naverMap.locationOverlay.isVisible = true
    }

    private fun messageSubscribe(
    ) {
        WaitingStompClient.stompClient.topic("/sub/collection").subscribe { topicMessage ->
            val fromJson = GsonBuilder().create().fromJson(topicMessage.payload, MarkerDTO::class.java)
            if (markerMap.containsKey(fromJson.markerId)){
                runOnUiThread {
                    markerMap[fromJson.markerId]!!.map = null
                }
            }

        }
    }

}
