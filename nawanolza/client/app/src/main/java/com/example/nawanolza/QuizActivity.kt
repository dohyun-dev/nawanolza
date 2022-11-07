package com.example.nawanolza

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.nawanolza.databinding.ActivityQuizBinding
import com.example.nawanolza.retrofit.CharacterLocationResponse
import com.example.nawanolza.retrofit.QuestFailResponse
import com.example.nawanolza.retrofit.QuestResponse
import com.example.nawanolza.retrofit.QuestSuccessResponse
import kotlinx.android.synthetic.main.activity_quiz.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizActivity : AppCompatActivity() {


    var quizInfo = QuestResponse()
    lateinit var binding: ActivityQuizBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val markerId = intent.getLongExtra("markerId",-1)
        val memberId = intent.getLongExtra("memberId",-1)
        val characterId = intent.getLongExtra("characterId",-1)



        //퀴즈 받아오기
        var retrofit = Retrofit.Builder()
            .baseUrl("https://k7d103.p.ssafy.io/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var service = retrofit.create(QuestService::class.java)


        println("===markerId")
        println(markerId)
        service.GetQuiz(mapOf("markerId" to markerId.toString(), "questType" to "0")).enqueue(object: Callback<QuestResponse> {

            override fun onResponse(
                call: Call<QuestResponse>,
                response: Response<QuestResponse>
            ) {
                val body = response.body()

                quizInfo = response.body() ?: QuestResponse()
//                println(characterInfo)

                println(quizInfo)
                println("=====quiz!!!!======")

                binding.title.text = body?.quiz?.context
            }

            override fun onFailure(call: Call<QuestResponse>, t: Throwable) {
                println(call)
                println(t)
                println("====quiz 에러 ===")
            }

        })



        exitButton.setOnClickListener{
            finish()
        }


        Obutton.setOnClickListener{
            service.PostSuccess(mapOf("memberId" to memberId.toString(),"markerId" to markerId.toString(), "characterId" to characterId.toString())).enqueue(object: Callback<QuestSuccessResponse> {

                override fun onResponse(
                    call: Call<QuestSuccessResponse>,
                    response: Response<QuestSuccessResponse>
                ) {
                    val body = response.body()
                    println("====quiz success 성공 ===")

                }

                override fun onFailure(call: Call<QuestSuccessResponse>, t: Throwable) {
                    println(call)
                    println(t)
                    println("====quiz success 에러 ===")
                }

            })
        }

        Xbutton.setOnClickListener{
            service.PostFail(mapOf("memberId" to memberId.toString())).enqueue(object: Callback<QuestFailResponse> {

                override fun onResponse(
                    call: Call<QuestFailResponse>,
                    response: Response<QuestFailResponse>
                ) {
                    val body = response.body()
                    println("====quiz fail 성공 ===")

                }

                override fun onFailure(call: Call<QuestFailResponse>, t: Throwable) {
                    println(call)
                    println(t)
                    println("====quiz fail 에러 ===")
                }

            })
        }

    }
}