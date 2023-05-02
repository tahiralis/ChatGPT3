package com.myownbot_chatgpt.chatgpt3

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

 //   lateinit var answersText : TextView
    lateinit var noteTxt : TextView
    lateinit var messageList : MessagesList
    lateinit var editQues : EditText
    lateinit var senBtn : ImageButton
    lateinit var micBtn : ImageButton

    lateinit var us : UserC
    lateinit var chatGpt : UserC
    lateinit var adapter: MessagesListAdapter<MessageC>
    lateinit var tts: TextToSpeech
    lateinit var speechRecognizer: SpeechRecognizer


   // private val client = OkHttpClient()



     var apiKey : String = "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"


    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

   //     answersText = findViewById(R.id.recTx)
        noteTxt = findViewById(R.id.noteTex)
        messageList = findViewById(R.id.mesList)
        editQues = findViewById(R.id.writeQMsg)
        senBtn = findViewById(R.id.sendBtn)
        micBtn = findViewById(R.id.micBtn)

        messageList = findViewById(R.id.mesList)

        var imageLoader : ImageLoader = object : ImageLoader{
            override fun loadImage(imageView: ImageView?, url: String?, payload: Any?) {
                Picasso.get().load(url).into(imageView)
            }

        }

        adapter =
            MessagesListAdapter<MessageC>("1", imageLoader)
        messageList.setAdapter(adapter)

        us = UserC("1","Hamza","")
        chatGpt = UserC("2","ChatGPT","")


        editQues.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (editQues.text.toString().trim().isNotEmpty()) {
                    // enable the send button
                    senBtn.setColorFilter(ContextCompat.getColor(applicationContext, com.stfalcon.chatkit.R.color.blue),
                        PorterDuff.Mode.SRC_IN)
                    senBtn.isEnabled = true
                    senBtn.isClickable = true
                } else {
                    // disable the send button
                    senBtn.setColorFilter(ContextCompat.getColor(applicationContext, com.stfalcon.chatkit.R.color.gray),
                        PorterDuff.Mode.SRC_IN)
                    senBtn.isEnabled = false
                    senBtn.isClickable = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        senBtn.setOnClickListener {
            val messageText = editQues.text.toString().trim()
            if (messageText.isNotEmpty()) {
                var message : MessageC =  MessageC("m1",editQues.text.toString(), us, Calendar.getInstance().time,"" )
                adapter.addToStart(message,true)

                if (messageText.startsWith("create image")){
                    generateImages(editQues.text.toString())
                } else {
                    performAction(editQues.text.toString())
                }
            }

            noteTxt.visibility = View.GONE
            editQues.text.clear()

        }




        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR){
                tts.setLanguage(Locale.UK)
            }
        })

        if (ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 121)
        }


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

      /*  micBtn.setOnClickListener {

            speechRecognizer.startListening(intent)
        }*/

        micBtn.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    micBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
                    // start listening to speech when the button is pressed
                    noteTxt.visibility = View.GONE
                    speechRecognizer.startListening(intent)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    micBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, com.stfalcon.chatkit.R.color.gray))
                    speechRecognizer.stopListening()
                    true
                }
                else -> false
            }
        }

        micBtn.setOnLongClickListener {
            micBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
            speechRecognizer.startListening(intent)
            true
        }

       /* micBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // set the background color to blue when the button is pressed
                    micBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.blue))
                    // start listening to speech when the button is pressed
                    speechRecognizer.startListening(intent)
                    noteTxt.visibility = View.GONE
                }
                MotionEvent.ACTION_UP -> {
                    // set the background color to gray when the button is released
                    micBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.grey))
                    // stop listening to speech when the button is released
                    speechRecognizer.stopListening()
                }
            }
            // return true to consume the touch event
            true
        }*/

        speechRecognizer.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(p0: Int) {

            }

            override fun onResults(p0: Bundle?) {
                val arrayOfRes =  p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val message : MessageC =  MessageC("m1",arrayOfRes!!.get(0), us, Calendar.getInstance().time,"" )
                adapter.addToStart(message,true)

                if (arrayOfRes!!.get(0).startsWith("create image")){
                    generateImages(arrayOfRes!!.get(0))
                } else {
                    performAction(arrayOfRes!!.get(0))
                }
            }

            override fun onPartialResults(p0: Bundle?) {

            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }

        })





    }


    //okHttp

    /*  fun performAction(input : String, callback: (String) -> Unit){

        val url = "https://api.openai.com/v1/completions"

        val jsonObject = JSONObject().apply {
            put("model", "text-davinci-003")
            put("prompt", input)
            put("max_tokens", 7)
            put("temperature", 0)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val body = response.body?.string()
                if (body != null){
                    Log.d("message", body)
                } else {
                    Log.d("TAG", "onResponse: null")
                }

                val jsonObject = JSONObject(body)
                var jsonArray : JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)

            }
        })
    }
*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)

        return true
    }

    var isTTS : Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.voice){
            if (isTTS){
                isTTS = false
                item.setIcon(R.drawable.baseline_voice_over_off_24)
                tts.stop()
            } else {
                isTTS = true
                item.setIcon(R.drawable.baseline_record_voice_over_24)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // volley library

    fun performAction(input:String){

       // answersText.text = input.toString()

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openai.com/v1/completions"

        val jsonObject = JSONObject().apply {
            put("model", "text-davinci-003")
            put("prompt", input)
            put("max_tokens", 500)
            put("temperature", 0.7)
        }


// Request a string response from the provided URL.
        val stringRequest = object :JsonObjectRequest(
            Method.POST, url,jsonObject,
            Response.Listener { response ->
                // Display the first 500 characters of the response string.
                var answer = response.getJSONArray("choices").
                getJSONObject(0).
                getString("text")
            //    answersText.text = answer

                var message : MessageC =  MessageC("m2",answer.trim(), chatGpt, Calendar.getInstance().time,"" )
                adapter.addToStart(message,true)

                Log.d("volley_message",  answer)

                if (isTTS == true){
                    tts.speak(answer,TextToSpeech.QUEUE_FLUSH,null, null)
                }

            },
            Response.ErrorListener { error ->

                Log.d("error_response", "performAction: that didn't work")

            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String,String>()
                map["Content-Type"] = "application/json"
                map["Authorization"] = "Bearer $apiKey"

                return map
            }
        }

        stringRequest.setRetryPolicy(object: RetryPolicy{
            override fun getCurrentTimeout(): Int {
                return 60000
            }

            override fun getCurrentRetryCount(): Int {
                return 15
            }

            override fun retry(error: VolleyError?) {
                TODO("Not yet implemented")
            }

        })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }



    fun generateImages(input:String){

     //   answersText.text = input.toString()

        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openai.com/v1/images/generations"

        val jsonObject = JSONObject().apply {
            put("prompt", input)
            put("n", 2)
            put("size", "512x512")
        }


// Request a string response from the provided URL.
        val stringRequest = object :JsonObjectRequest(
            Method.POST, url,jsonObject,
            Response.Listener { response ->
                // Display the first 500 characters of the response string.
                var jsonArrayRes = response.getJSONArray("data")

               for (i in 0 until jsonArrayRes.length()){
                   var answer = jsonArrayRes.getJSONObject(i).
                   getString("url")
                   //  answersText.text = answer

                   var message : MessageC =  MessageC("m2","image", chatGpt, Calendar.getInstance().time,answer )
                   adapter.addToStart(message,true)
               }



              //  Log.d("volley_message",  answer)

            },
            Response.ErrorListener { error ->

                Log.d("error_response", "performAction: that didn't work")

            })
        {
            override fun getHeaders(): MutableMap<String, String> {
                val map = HashMap<String,String>()
                map["Content-Type"] = "application/json"
                map["Authorization"] = "Bearer $apiKey"

                return map
            }
        }

        stringRequest.setRetryPolicy(object: RetryPolicy{
            override fun getCurrentTimeout(): Int {
                return 60000
            }

            override fun getCurrentRetryCount(): Int {
                return 15
            }

            override fun retry(error: VolleyError?) {
                TODO("Not yet implemented")
            }

        })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }


}