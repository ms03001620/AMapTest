package jp.linktivity.citypass.temp.one;


data class Seat(
    val id: String,
    val x: Float,      // 座位中心X坐标
    val y: Float,      // 座位中心Y坐标
    val width: Float,  // 座位宽度
    val height: Float, // 座位高度
    var selected: Boolean = false
)