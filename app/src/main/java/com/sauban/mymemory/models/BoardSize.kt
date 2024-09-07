package com.sauban.mymemory.models

enum class BoardSize(val numCards: Int){
    EASY(numCards = 8),
    MEDIUM(numCards = 24),
    HARD(numCards = 32);

    fun getWidth(): Int {
        return when (this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight() : Int {
        return numCards / getWidth()

    }

    fun getNumPairs() : Int {
        return numCards / 2
    }
}