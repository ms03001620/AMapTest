package com.polestar.repository.data.charging

import java.io.Serializable

/**
 * 电源类型: 0:交流电 1:直流电
 */
data class GunKw(val type: String?, val kw: String?) : Serializable