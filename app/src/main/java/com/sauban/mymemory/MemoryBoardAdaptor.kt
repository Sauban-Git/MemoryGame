package com.sauban.mymemory

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.sauban.mymemory.models.BoardSize
import com.sauban.mymemory.models.MemoryCard
import kotlin.math.min

class MemoryBoardAdaptor(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) :
    RecyclerView.Adapter<MemoryBoardAdaptor.ViewHolder>() {

    private var boardWidth = 0
    private var boardHeight = 0

    fun setBoardDimensions(width: Int, height: Int) {
        boardWidth = width
        boardHeight = height
        notifyDataSetChanged()
    }


    companion object {
        private const val MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdaptor"
    }

    interface CardClickListener {
        fun onCardClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidth =
            parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)

        val cardHeight =
            parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)

        val view = LayoutInflater.from(context)
            .inflate(R.layout.memory_card, parent, false)

        val layoutParams =
            view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width = cardWidth
        layoutParams.height = cardHeight
        layoutParams.setMargins(
            MARGIN_SIZE,
            MARGIN_SIZE,
            MARGIN_SIZE,
            MARGIN_SIZE
        )

        return ViewHolder(view)
    }


    override fun getItemCount() = boardSize.numCards


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
            val memoryCard = cards[position]
            imageButton.setImageResource(if (memoryCard.isFaceUp) memoryCard.identifier else R.drawable.pattern)


            imageButton.alpha = if(memoryCard.isMatched) .4f else 1.0f
            val colorStateList = if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStateList)

            imageButton.setOnClickListener{
                Log.i(TAG, "Clicked on position $position")
                cardClickListener.onCardClicked(position)
            }
        }
    }
}
