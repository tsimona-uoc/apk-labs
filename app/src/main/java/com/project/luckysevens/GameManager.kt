package com.project.luckysevens

import kotlin.random.Random

class GameManager(initialCoins: Int = 125) {

    // Lista de iconos disponibles (referencias a drawables)
    val icons = listOf(
        R.drawable.ic_rubi,    // 0
        R.drawable.ic_siete,   // 1
        R.drawable.ic_campana, // 2
        R.drawable.ic_sandia,  // 3
        R.drawable.ic_naranja, // 4
        R.drawable.ic_cereza   // 5
    )

    var coins: Int = initialCoins
        private set

    var currentBet: Int = 10
        private set

    var currentSlots: IntArray = intArrayOf(1, 1, 1)
        private set

    fun increaseBet() {
        if (currentBet + 10 <= coins) {
            currentBet += 10
        }
    }

    fun decreaseBet() {
        if (currentBet - 10 >= 10) {
            currentBet -= 10
        }
    }

    fun spin(): Int {
        if (coins < currentBet || currentBet == 0) return 0

        coins -= currentBet

        currentSlots = intArrayOf(
            Random.nextInt(icons.size),
            Random.nextInt(icons.size),
            Random.nextInt(icons.size)
        )

        val winnings = calculateWinnings()
        coins += winnings

        if (currentBet > coins && coins >= 10) {
            currentBet = (coins / 10) * 10
        } else if (coins < 10 && coins > 0) {
            currentBet = coins
        } else if (coins == 0) {
            currentBet = 0
        }

        return winnings
    }

    private fun calculateWinnings(): Int {
        val (s1, s2, s3) = currentSlots
        

        return when {
            // TRES IGUALES (Premios mayores)
            s1 == s2 && s2 == s3 -> {
                val multiplier = when (s1) {
                    0 -> 50 // Rubí: x50
                    1 -> 20 // Siete: x20
                    2 -> 15 // Campana: x15
                    3 -> 10 // Sandía: x10
                    4 -> 5  // Naranja: x5
                    5 -> 3  // Cereza: x3
                    else -> 1
                }
                currentBet * multiplier
            }
            
            // DOS IGUALES
            s1 == s2 || s2 == s3 || s1 == s3 -> {
                // Si hay dos iguales, devolvemos el doble de la apuesta
                currentBet * 2
            }
            
            else -> 0
        }
    }

    fun getDrawableId(slotIndex: Int): Int {
        return icons[currentSlots[slotIndex]]
    }
}
