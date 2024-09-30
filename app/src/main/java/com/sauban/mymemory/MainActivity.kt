package com.sauban.mymemory

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.sauban.mymemory.databinding.ActivityMainBinding
import com.sauban.mymemory.models.BoardSize
import com.sauban.mymemory.models.MemoryGame
import com.sauban.mymemory.utils.MusicService


class MainActivity : AppCompatActivity() {


    companion object{
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE = 814155
    }

    private val soundPool by lazy {
        SoundPool.Builder().setMaxStreams(5).build()
    }
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private lateinit var binding : ActivityMainBinding
    private var isExpanded = false

    private val fromTopFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_top_anim)
    }
    private val toTopFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_top_anim)
    }
    private val rotateClockFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise)
    }
    private val rotateAntiClockAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_anti_clock_wise)
    }


    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdaptor
    private var boardSize: BoardSize = BoardSize.EASY

    private var soundFlip: Int = 0
    private var soundWrong: Int = 0
    private var soundMatched: Int = 0
    private var soundCompliment: Int = 0



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MusicService::class.java)
        startService(intent)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        soundFlip = soundPool.load(this, R.raw.flip, 1)
        soundWrong = soundPool.load(this, R.raw.error, 1)
        soundMatched = soundPool.load(this, R.raw.pairmatch, 1)
        soundCompliment = soundPool.load(this, R.raw.excellent, 1)



        binding.menuBtn.setOnClickListener {
            if (isExpanded) {
                shrinkFab()
            }else {
                expandFab()
            }
        }
        binding.editBtn.setOnClickListener(View.OnClickListener {
            if (isExpanded) {shrinkFab()}
            val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
            val radioGroupSize  = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
            when (boardSize) {
                BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
                BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
                BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
            }
            showAlertDialog("Choose new level", boardSizeView, View.OnClickListener {
                // Set a new value for the board size
                boardSize = when (radioGroupSize.checkedRadioButtonId) {
                    R.id.rbEasy -> BoardSize.EASY
                    R.id.rbMedium -> BoardSize.MEDIUM
                    else -> BoardSize.HARD
                }
                setupBoard()

            })
        })

        binding.refreshBtn.setOnClickListener(View.OnClickListener {
            if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame() ) {
                showAlertDialog("Do you want to quit your progress?", null, View.OnClickListener {
                    Toast.makeText(this, "Game refreshed!",Toast.LENGTH_SHORT).show()
                    setupBoard()
                })
            }else {
                Toast.makeText(this, "Game refreshed!",Toast.LENGTH_SHORT).show()
                setupBoard()
            }
            if (isExpanded) {shrinkFab()}
        })


        setupBoard()
    }

    override fun onPause() {
        super.onPause()
        val intent = Intent(this, MusicService::class.java)
        stopService(intent)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
    }


    private fun expandFab() {
        binding.editBtn.visibility = View.VISIBLE
        binding.refreshBtn.visibility = View.VISIBLE
        binding.editBtn.isClickable = true
        binding.refreshBtn.isClickable = true
        binding.menuBtn.startAnimation(rotateClockFabAnim)
        binding.editBtn.startAnimation(fromTopFabAnim)
        binding.refreshBtn.startAnimation(fromTopFabAnim)
        binding.editTv.startAnimation(fromTopFabAnim)
        binding.refreshTv.startAnimation(fromTopFabAnim)
        isExpanded = !isExpanded
    }

    private fun shrinkFab() {
        binding.menuBtn.startAnimation(rotateAntiClockAnim)
        binding.editBtn.startAnimation(toTopFabAnim)
        binding.refreshBtn.startAnimation(toTopFabAnim)
        binding.editTv.startAnimation(toTopFabAnim)
        binding.refreshTv.startAnimation(toTopFabAnim)
        binding.editBtn.isClickable = false
        binding.refreshBtn.isClickable = false
        isExpanded = !isExpanded
    }



    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
       val builder =  AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok") {_, _ ->
                positiveClickListener.onClick(null)
            }
        val dialog = builder.create()

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun setupBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "EASY: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "MEDIUM: 8 x 3"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "HARD: 8 x 4"
                tvNumPairs.text = "Pairs: 0 / 16"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdaptor(this, boardSize, memoryGame.cards,object: MemoryBoardAdaptor.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())
        val cardTime: Int = 1000 * boardSize.getWidth()
        showAllCardsTemporarily(cardTime)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showAllCardsTemporarily(cardTime: Int) {
        for (i in 0 until memoryGame.cards.size) {
            memoryGame.cards[i].isFaceUp = true
        }
        adapter.notifyDataSetChanged()

        Handler(Looper.getMainLooper()).postDelayed({
            for (i in 0 until memoryGame.cards.size) {
                memoryGame.cards[i].isFaceUp = false
            }
            adapter.notifyDataSetChanged()
        },cardTime.toLong())
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (isExpanded) shrinkFab()
        soundPool.play(soundFlip, 1f, 1f, 1, 0, 1f)
        if (memoryGame.haveWonGame()) {
            //Alert the user for invalid move
            Snackbar.make(clRoot, "You have already won! Click on Plus icon and restart or edit the Game",Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            //Alert the user of an invalid move
            soundPool.play(soundWrong, 1f, 1f, 1, 0, 1f)
            Snackbar.make(clRoot, "Invalid move!",Snackbar.LENGTH_SHORT).show()
            return
        }
        //Actually flipping over the card
        if (memoryGame.flipCard(position)) {
            soundPool.play(soundMatched, 1f, 1f, 1, 0, 1f)
            Log.i(TAG,"Found a match! Num pairs found: ${memoryGame.numPairsFound}")

            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            val checkMoves = boardSize.getNumPairs() + (boardSize.getNumPairs() / 2)
            if (memoryGame.haveWonGame()) {
                Toast.makeText(this, "Congratulations! You have won the Game",Toast.LENGTH_SHORT).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.GREEN, Color.MAGENTA)).oneShot()
                soundPool.play(soundCompliment, 1f, 1f, 1, 0, 1f)

            }else if (memoryGame.getNumMoves() > checkMoves) {
                Toast.makeText(this, "Game over! Try again.",Toast.LENGTH_LONG).show()
                setupBoard()
            }
        }
        tvNumMoves.text = "Move: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }

}
