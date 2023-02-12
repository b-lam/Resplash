package com.b_lam.resplash.data.billing

object Sku {

    const val RESPLASH_PRO = "pro"
    const val COFFEE = "coffee"
    const val SMOOTHIE = "smoothie"
    const val PIZZA = "pizza"
    const val FANCY_MEAL = "meal"

    val INAPP_PRODUCTS = listOf(RESPLASH_PRO, COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
    val CONSUMABLE_PRODUCTS = listOf(COFFEE, SMOOTHIE, PIZZA, FANCY_MEAL)
}
