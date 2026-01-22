package com.sauban.mymemory.models

enum class BoardSize(val numCards: Int){
    EASY(numCards = 8),
    MEDIUM(numCards = 12),
    HARD(numCards = 24),

    EXTREME(numCards = 32);

    fun getWidth(): Int {
        return when (this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 3
            EXTREME -> 4
        }
    }

    fun getHeight() : Int {
        return numCards / getWidth()

    }

    fun getNumPairs() : Int {
        return numCards / 2
    }
}