package com.example.nawanolza.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.nawanolza.EnterRoom
import com.example.nawanolza.EnterRoomService
import com.example.nawanolza.R
import kotlinx.android.synthetic.main.fragment_game_room_intro.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GameRoomIntro.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameRoomIntro : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_room_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        var retrofit = Retrofit.Builder()
            .baseUrl("https://k7d103.p.ssafy.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var enterRoomService = retrofit.create(EnterRoomService::class.java)

        create_room.setOnClickListener {
            navController.navigate(R.id.action_gameRoomIntro_to_selectGame)
        }

        enter_room.setOnClickListener {
            var code = EditText.text.toString()
            enterRoomService.requestEnterRoom(code).enqueue(object: Callback<EnterRoom>{
                override fun onResponse(call: Call<EnterRoom>, response: Response<EnterRoom>) {
                    var res = response.body()

                    var dialog = AlertDialog.Builder(activity)
                    dialog.setMessage(res?.roomCode)
                    dialog.show()
                    Toast.makeText(activity, "success", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<EnterRoom>, t: Throwable) {
                    Toast.makeText(activity, "fail", Toast.LENGTH_SHORT).show()
                }

            })
//            Toast.makeText(activity, code, Toast.LENGTH_SHORT).show()
//            navController.navigate(R.id.action_gameRoomIntro_to_hideAndSeek)
        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GameRoomIntro.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GameRoomIntro().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}