package com.example.nawanolza

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.nawanolza.databinding.ActivityMainBinding
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
* w : 대기화면
* s/2022-09-30-06-30(종료시간)/7(인원) : 게임 시작 초기 세팅 (10분-7명)
* g/김땡땡 : 김땡땡 잡힘
* a : 영역 밖 알람
* r/0 : 술래팀 승리
* r/1 : 숨는팀 승리
* e : 게임 종료
* */
class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private var countPeople = 0
    val uriRingTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private val messageReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra(MessageConstants.message)
            val path = intent?.getStringExtra(MessageConstants.path)
            Log.i("Main Activity", "Broadcast Received. message: $message, path: $path")

            val imageViewMain: ImageView = findViewById(R.id.imageView_main);
            val textViewMain: TextView = findViewById(R.id.textView_main);
            val imageViewLogo: ImageView = findViewById(R.id.imageView_logo);
            val textViewLogo: TextView = findViewById(R.id.textView_logo);
            val progressBarWaiting: ProgressBar = findViewById(R.id.progressbar_waiting)
            val progressBarAlarm: ProgressBar = findViewById(R.id.progressbar_alarm)
            val textViewWaiting: TextView = findViewById(R.id.textView_waiting)
            val textViewStart1: TextView = findViewById(R.id.textView_start1)
            val textViewStart2: TextView = findViewById(R.id.textView_start2)
            val textViewResult1: TextView = findViewById(R.id.textView_result1)
            val textViewResult2: TextView = findViewById(R.id.textView_result2)
            val testViewEnd: TextView = findViewById(R.id.textView_end)

            if (message != null) {
                val str = message.split("/")
                imageViewMain.visibility = View.GONE
                textViewMain.visibility = View.GONE
                imageViewLogo.visibility = View.VISIBLE
                textViewLogo.visibility = View.VISIBLE

                if(str[0]=="w"){
                    Log.i("Main Activity", "Waiting display")
                    progressBarWaiting.visibility = View.VISIBLE
                    progressBarAlarm.visibility = View.GONE
                    textViewWaiting.visibility = View.VISIBLE
                    textViewStart1.visibility = View.GONE
                    textViewStart2.visibility = View.GONE
                    textViewResult1.visibility = View.GONE
                    textViewResult2.visibility = View.GONE
                    testViewEnd.visibility = View.GONE

                }else if(str[0]=="s"){
                    Log.i("Main Activity", "Starting display")
                    progressBarWaiting.visibility = View.GONE
                    progressBarAlarm.visibility = View.GONE
                    textViewWaiting.visibility = View.GONE
                    textViewStart1.visibility = View.VISIBLE
                    textViewStart2.visibility = View.VISIBLE
                    textViewResult1.visibility = View.GONE
                    textViewResult2.visibility = View.GONE
                    testViewEnd.visibility = View.GONE

                    countPeople = Integer.parseInt(str[2])

                    textViewStart1.text = "남은 인원" + countPeople + "명"
                    val duration: Duration = Duration.between(LocalDateTime.now(), LocalDateTime.parse(str[1], DateTimeFormatter.ISO_DATE_TIME))
                    object : CountDownTimer(duration.seconds*1000, 1000) {
                        override fun onFinish() {
                            Log.i("Main Activity", "타이머 끝")
                        }
                        override fun onTick(p0: Long) {
                            val minutes = p0/1000/60
                            val sec = p0/1000%60

                            if(minutes<10 && sec<10){
                                textViewStart2.text = String.format("0%s:0%s",minutes, sec)
                            } else if(minutes<10 && sec>=10){
                                textViewStart2.text = String.format("0%s:%s",minutes, sec)
                            } else if(minutes>=10 && sec<10){
                                textViewStart2.text = String.format("%s:0%s",minutes, sec)
                            }
                            else{
                                textViewStart2.text = String.format("%s:%s",minutes, sec)
                            }
                        }
                    }.start()

                }else if(str[0]=="g"){
                    Log.i("Main Activity", "Gaming display")
                    progressBarWaiting.visibility = View.GONE
                    progressBarAlarm.visibility = View.GONE
                    textViewWaiting.visibility = View.GONE
                    textViewStart1.visibility = View.VISIBLE
                    textViewStart2.visibility = View.VISIBLE
                    textViewResult1.visibility = View.GONE
                    textViewResult2.visibility = View.GONE
                    testViewEnd.visibility = View.GONE

                    Log.i("ringTone", uriRingTone.toString())

                    val ringTone = RingtoneManager.getRingtone(this@MainActivity, uriRingTone)
                    Log.i("ringTone", ringTone.toString())

                    ringTone.play()

                    countPeople -= 1
                    textViewStart1.text = "남은 인원" + countPeople + "명"
                    Toast.makeText(this@MainActivity,str[1] + "님이 잡혔습니다!", Toast.LENGTH_SHORT).show();
                    
                }else if(str[0]=="r"){
                    if(str[1]=="0"){
                        Log.i("Main Activity", "tagger win display")
                        progressBarWaiting.visibility = View.GONE
                        progressBarAlarm.visibility = View.GONE
                        textViewWaiting.visibility = View.GONE
                        textViewStart1.visibility = View.GONE
                        textViewStart2.visibility = View.GONE
                        textViewResult1.visibility = View.VISIBLE
                        textViewResult2.visibility = View.GONE
                        testViewEnd.visibility = View.GONE
                    }else{
                        Log.i("Main Activity", "non tagger win display")
                        progressBarWaiting.visibility = View.GONE
                        progressBarAlarm.visibility = View.GONE
                        textViewWaiting.visibility = View.GONE
                        textViewStart1.visibility = View.GONE
                        textViewStart2.visibility = View.GONE
                        textViewResult1.visibility = View.GONE
                        textViewResult2.visibility = View.VISIBLE
                        testViewEnd.visibility = View.GONE
                    }

                }else if(str[0]=="e"){
                    Log.i("Main Activity", "end display")
                    progressBarWaiting.visibility = View.GONE
                    progressBarAlarm.visibility = View.GONE
                    textViewWaiting.visibility = View.GONE
                    textViewStart1.visibility = View.GONE
                    textViewStart2.visibility = View.GONE
                    textViewResult1.visibility = View.GONE
                    textViewResult2.visibility = View.GONE
                    testViewEnd.visibility = View.VISIBLE

                }else if(str[0]=="a"){
                    Log.i("Main Activity", "alarm display")

                    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator;
                    vibrator.vibrate(VibrationEffect.createOneShot(500, 100));

                    Log.i("ringTone", uriRingTone.toString())
                    val ringTone = RingtoneManager.getRingtone(this@MainActivity, uriRingTone)
                    Log.i("ringTone", ringTone.toString())
                    ringTone.play()

                    progressBarAlarm.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity,"영역을 벗어났습니다!", Toast.LENGTH_SHORT).show();
                    Handler().postDelayed(Runnable {
                        progressBarAlarm.visibility = View.GONE
                    }, 500)

                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(MessageConstants.intentName)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(messageReceiver, intentFilter)

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(messageReceiver)
    }
}