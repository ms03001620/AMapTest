package com.polestar.repository.data.charging

import java.io.Serializable


/*{
    "endTime": "23:59",
    "startTime": "00:00",
    "outFeeService": 100.0,
    "outFeeElectric": 80.0
}*/
data class ElectricFee(
    val endTime: String? = null,
    val startTime: String? = null,
    val outFeeService: Float? = null,
    val outFeeElectric: Float? = null,
) : Serializable