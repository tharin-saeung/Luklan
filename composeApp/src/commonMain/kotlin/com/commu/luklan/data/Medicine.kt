package com.commu.luklan.data

data class Medicine(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val dosage: String = "", // เช่น "1 เม็ด", "2 ช้อนชา"
    val time: String = "",
    val frequency: String = "", // เช่น "ทุกวัน", "สัปดาห์ละ 3 ครั้ง"
    val quantity: Int = 0, // จำนวนยาที่มี
    val unit: String = "เม็ด", // หน่วย: เม็ด, ขวด, กล่อง
    val expiryDate: String = "", // วันหมดอายุ
    val category: String = "", // ประเภทยา: "แก้ปวด", "หัวใจ", "เบาหวาน"
    val storageInstructions: String = "", // วิธีเก็บรักษา
    val notes: String = "", // หมายเหตุเพิ่มเติม
    val userId: String = "",
    val taken: Boolean = false,
    val createdAt: Long = 0L
)

