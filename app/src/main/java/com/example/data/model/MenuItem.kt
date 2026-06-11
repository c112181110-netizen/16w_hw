package com.example.data.model

enum class Category(val displayName: String) {
    BENTO("經典主餐"),
    DRINK("特調飲品"),
    SNACK("超人氣小吃")
}

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Int,
    val description: String,
    val category: Category,
    val iconUrl: String, // Decorative or Coil URL
    val badge: String? = null, // e.g. "HOT", "NEW", "推薦"
    val spicyLevel: Int = 0, // 0 = not spicy, 1 to 3 stars
    val hasIceSugarCustomization: Boolean = false // If true, can customize ice/sugar in the cart
)

object MenuCatalog {
    val items = listOf(
        MenuItem(
            id = 1,
            name = "經典排骨飯",
            price = 110,
            description = "厚實酥嫩現炸排骨，配上香脆酸菜、滷豆干與每日時蔬，滿滿懷舊古早味。",
            category = Category.BENTO,
            iconUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=120",
            badge = "人氣之王",
            spicyLevel = 0
        ),
        MenuItem(
            id = 2,
            name = "酥炸大雞腿飯",
            price = 120,
            description = "金黃脆皮特大雞腿，外皮酥脆鮮嫩多汁，香氣爆棚。配菜豐富飽足感爆表。",
            category = Category.BENTO,
            iconUrl = "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&q=80&w=120",
            badge = "主廚推薦",
            spicyLevel = 0
        ),
        MenuItem(
            id = 3,
            name = "香滷控肉飯",
            price = 95,
            description = "特選肥瘦黃金比例三層肉，老滷慢火細燉至香軟 Q 彈、入口即化，鹹香超下飯。",
            category = Category.BENTO,
            iconUrl = "https://images.unsplash.com/photo-1512058564366-18510be2db19?auto=format&fit=crop&q=80&w=120",
            spicyLevel = 0
        ),
        MenuItem(
            id = 4,
            name = "經典滷肉飯",
            price = 60,
            description = "手切帶皮肥肉，慢熬十二小時釋放滿滿膠質，鹹甜適中，淋在香熱白飯上太完美。",
            category = Category.BENTO,
            iconUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&q=80&w=120",
            badge = "超值首選"
        ),
        MenuItem(
            id = 5,
            name = "珍珠鮮奶茶",
            price = 65,
            description = "每日手作Q彈黑糖蜜珍珠，搭配濃厚肯亞紅茶與100%小農優質鮮奶，濃醇香滑。",
            category = Category.DRINK,
            iconUrl = "https://images.unsplash.com/photo-1541658016709-82535e94bc69?auto=format&fit=crop&q=80&w=120",
            badge = "必點特調",
            hasIceSugarCustomization = true
        ),
        MenuItem(
            id = 6,
            name = "古早味紅茶",
            price = 35,
            description = "傳承古法，採用決明子與阿薩姆紅茶葉慢火熬起，清爽甘甜、麥香滿溢，消暑必備。",
            category = Category.DRINK,
            iconUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&q=80&w=120",
            hasIceSugarCustomization = true
        ),
        MenuItem(
            id = 7,
            name = "清爽冬瓜檸檬",
            price = 45,
            description = "手工熬製古法冬瓜露，完美調配屏東鮮榨檸檬汁，酸甜交織、清涼解渴不甜膩。",
            category = Category.DRINK,
            iconUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&q=80&w=120",
            hasIceSugarCustomization = true
        ),
        MenuItem(
            id = 8,
            name = "黃金炸豆腐",
            price = 50,
            description = "現炸外酥內嫩雞蛋豆腐，淋上大蒜特調醬油膏，咬下多汁燙口，超人氣代表作。",
            category = Category.SNACK,
            iconUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&q=80&w=120",
            badge = "搶手小菜"
        ),
        MenuItem(
            id = 9,
            name = "澎湖手作花枝丸",
            price = 55,
            description = "咬得到飽滿新鮮花枝顆粒，Q彈扎實，炸到外表微微金黃，撒上椒鹽最極致。",
            category = Category.SNACK,
            iconUrl = "https://images.unsplash.com/photo-1534422298391-e4f8c172dddb?auto=format&fit=crop&q=80&w=120"
        ),
        MenuItem(
            id = 10,
            name = "特製椒鹽鹹酥雞",
            price = 60,
            description = "無骨雞肉以秘製中藥香料醃製，外皮香酥、肉質鮮嫩多汁，搭配九層塔香氣滿分。",
            category = Category.SNACK,
            iconUrl = "https://images.unsplash.com/photo-1562967914-608f82629710?auto=format&fit=crop&q=80&w=120",
            badge = "推薦",
            spicyLevel = 1
        )
    )
}
