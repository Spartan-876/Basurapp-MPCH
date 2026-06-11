package com.utp.basurapp.recolectorapp.data

data class NominatimResponse(
    val display_name: String?,
    val address: Address?
)

data class Address(
    val road: String?,
    val house_number: String?,
    val city: String?,
    val town: String?,
    val village: String?
)
