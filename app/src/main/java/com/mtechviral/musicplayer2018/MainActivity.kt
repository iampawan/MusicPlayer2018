package com.mtechviral.musicplayer2018

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.mtechviral.mplaylib.MusicFinder
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*

class MainActivity : AppCompatActivity() {


    var albumArt: ImageView? = null

    var playButton: ImageButton? = null
    var shuffleButton: ImageButton? = null

    var songTitle: TextView? = null
    var songArtist : TextView? = null

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

            //Ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }else{
            createPlayer()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            createPlayer()
        }else{
            longToast("Permission not granted. Shutting down.")
            finish()
        }
    }

    private fun createPlayer(){

        var songsJob = async {
            val songFinder = MusicFinder(contentResolver)
            songFinder.prepare()
            songFinder.allSongs
        }

        launch(kotlinx.coroutines.experimental.android.UI){
            val songs = songsJob.await()

            val playerUI = object:AnkoComponent<MainActivity>{
                override fun createView(ui: AnkoContext<MainActivity>)= with(ui) {

                    relativeLayout{
                        backgroundColor = Color.BLACK

                        albumArt = imageView{
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(matchParent, matchParent)

                        verticalLayout{
                            backgroundColor = Color.parseColor("#99000000")
                            songTitle = textView{
                                textColor = Color.WHITE
                                typeface = Typeface.DEFAULT_BOLD
                                textSize = 18f
                            }

                            songArtist = textView{
                                textColor = Color.WHITE
                            }

                            linearLayout{
                                playButton = imageButton{
                                    imageResource = R.drawable.ic_play_arrow_black_24dp
                                    onClick {
                                        playOrPause()
                                    }
                                }.lparams(0, wrapContent,0.5f)

                                shuffleButton = imageButton{
                                    imageResource = R.drawable.ic_shuffle_black_24dp
                                    onClick {
                                        playRandom()
                                    }
                                }.lparams(0, wrapContent,0.5f)
                            }.lparams(matchParent, wrapContent){
                                topMargin = dip(5)
                            }



                        }.lparams(matchParent, wrapContent){
                            alignParentBottom()
                        }
                    }

                }
                fun playRandom(){
                    Collections.shuffle(songs)
                    val song = songs[0]
                    mediaPlayer?.reset()
                    mediaPlayer = MediaPlayer.create(ctx,song.uri)
                    mediaPlayer?.setOnCompletionListener {
                        playRandom()
                    }
                    albumArt?.imageURI = song.albumArt
                    songTitle?.text = song.title
                    songArtist?.text = song.artist
                    mediaPlayer?.start()
                    playButton?.imageResource = R.drawable.ic_pause_black_24dp
                }

                fun playOrPause(){
                    var songPlaying:Boolean? = mediaPlayer?.isPlaying

                    if(songPlaying == true){
                        mediaPlayer?.pause()
                        playButton?.imageResource = R.drawable.ic_play_arrow_black_24dp
                    }
                    else{
                        mediaPlayer?.start()
                        playButton?.imageResource = R.drawable.ic_pause_black_24dp
                    }
                }
            }
            playerUI.setContentView(this@MainActivity)
            playerUI.playRandom()


        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
