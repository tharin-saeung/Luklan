package com.commu.luklan.data

data class Medicine(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val dosage: String = "", // เช่น "1 เม็ด", "2 ช้อนชา"
    val time: String = "",
    val times: List<String> = emptyList(), // รองรับหลายเวลาต่อวัน (เช่น ["08:00","20:00"]) - backward compatible
    val frequency: String = "", // เช่น "ทุกวัน", "สัปดาห์ละ 3 ครั้ง"
    val timeUnit: String = "วัน", // หน่วยเวลา: วัน, สัปดาห์, เดือน
    val frequencyCount: Int = 1, // จำนวนครั้งต่อหน่วยเวลา (เช่น 2 ครั้งต่อสัปดาห์)
    val amountPerDose: String = "", // จำนวนที่กินต่อครั้ง (เช่น "1 เม็ด")
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

