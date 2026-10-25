package com.example.data.model

data class MobileOperator(
    val id: String,
    val name: String,
    val prefix: List<String>,
    val colorHex: Long,
    val bundles: List<RechargeOffer>
)

data class RechargeOffer(
    val title: String,
    val titleBn: String,
    val amount: Double,
    val validity: String,
    val validityBn: String,
    val description: String,
    val descriptionBn: String
)

data class UtilityBiller(
    val id: String,
    val name: String,
    val nameBn: String,
    val category: String, // Electricity, Gas, Water, Internet, TV, Education
    val categoryBn: String,
    val accountLabel: String,
    val accountLabelBn: String,
    val exampleAccount: String
)

data class ScheduledBank(
    val code: String,
    val name: String,
    val nameBn: String,
    val supportedMethods: List<String> // BEFTN, NPSB, RTGS
)

object FinancialCatalog {
    val operators = listOf(
        MobileOperator(
            id = "GP",
            name = "Grameenphone",
            prefix = listOf("017", "013"),
            colorHex = 0xFF0077C8,
            bundles = listOf(
                RechargeOffer("Combo 15GB + 300 Min", "কম্বো ১৫ জিবি + ৩০০ মিনিট", 399.0, "30 Days", "৩০ দিন", "Heavy usage monthly pack", "মাসিক সেরা অফার"),
                RechargeOffer("Data 5GB 4G", "৫ জিবি ৪জি ইন্টারনেট", 149.0, "7 Days", "৭ দিন", "Weekly high-speed data", "সাপ্তাহিক দ্রুতগতির ডাটা"),
                RechargeOffer("Voice 120 Mins", "১২০ মিনিট টকটাইম", 84.0, "10 Days", "১০ দিন", "Any local network minutes", "যেকোনো অপারেটরে মিনিট"),
                RechargeOffer("Regular Top-Up ৳50", "রেগুলার রিচার্জ ৫০ টাকা", 50.0, "Unlimited", "সীমাহীন", "Standard balance recharge", "সাধারণ ব্যালেন্স রিচার্জ")
            )
        ),
        MobileOperator(
            id = "ROBI",
            name = "Robi",
            prefix = listOf("018"),
            colorHex = 0xFFED1C24,
            bundles = listOf(
                RechargeOffer("Superpack 20GB + 400 Min", "সুপারপ্যাক ২০ জিবি + ৪০০ মিনিট", 448.0, "30 Days", "৩০ দিন", "All-in-one monthly plan", "মাসিক অল-ইন-ওয়ান"),
                RechargeOffer("Unlimited 7GB", "৭ জিবি ইন্টারনেট", 179.0, "7 Days", "৭ দিন", "Smooth streaming pack", "স্ট্রিমিং প্যাক"),
                RechargeOffer("Talktime 200 Min", "২০০ মিনিট টকটাইম", 127.0, "15 Days", "১৫ দিন", "Best call rate", "সাশ্রয়ী কলরেট"),
                RechargeOffer("Regular Top-Up ৳100", "রেগুলার রিচার্জ ১০০ টাকা", 100.0, "Unlimited", "সীমাহীন", "Standard balance recharge", "সাধারণ ব্যালেন্স রিচার্জ")
            )
        ),
        MobileOperator(
            id = "BANGLALINK",
            name = "Banglalink",
            prefix = listOf("019", "014"),
            colorHex = 0xFFFF6600,
            bundles = listOf(
                RechargeOffer("Orange Blast 18GB", "১৮ জিবি অরেঞ্জ ব্লাস্ট", 298.0, "30 Days", "৩০ দিন", "Fast internet pack", "দ্রুতগতির ইন্টারনেট"),
                RechargeOffer("Talktime 150 Min", "১৫০ মিনিট টকটাইম", 97.0, "10 Days", "১০ দিন", "Clear voice calls", "ক্লিয়ার ভয়েস কল"),
                RechargeOffer("Super Data 10GB", "১০ জিবি সুপার ডাটা", 199.0, "7 Days", "৭ দিন", "Weekly super data", "সাপ্তাহিক সুপার ডাটা"),
                RechargeOffer("Regular Top-Up ৳30", "রেগুলার রিচার্জ ৩০ টাকা", 30.0, "Unlimited", "সীমাহীন", "Standard balance recharge", "সাধারণ ব্যালেন্স রিচার্জ")
            )
        ),
        MobileOperator(
            id = "TELETALK",
            name = "Teletalk",
            prefix = listOf("015"),
            colorHex = 0xFF009944,
            bundles = listOf(
                RechargeOffer("Bornomala 10GB + 100 Min", "বর্ণমালা ১০ জিবি + ১০০ মিনিট", 198.0, "30 Days", "৩০ দিন", "Special student & govt plan", "সাশ্রয়ী প্যাকেজ"),
                RechargeOffer("Regular Top-Up ৳50", "রেগুলার রিচার্জ ৫০ টাকা", 50.0, "Unlimited", "সীমাহীন", "Standard balance recharge", "সাধারণ ব্যালেন্স রিচার্জ")
            )
        ),
        MobileOperator(
            id = "AIRTEL",
            name = "Airtel",
            prefix = listOf("016"),
            colorHex = 0xFFE40000,
            bundles = listOf(
                RechargeOffer("Airtel Dhamaka 12GB", "এয়ারটেল ধামাকা ১২ জিবি", 249.0, "30 Days", "৩০ দিন", "Youth special pack", "তারুণ্যের অফার"),
                RechargeOffer("Regular Top-Up ৳100", "রেগুলার রিচার্জ ১০০ টাকা", 100.0, "Unlimited", "সীমাহীন", "Standard balance recharge", "সাধারণ ব্যালেন্স রিচার্জ")
            )
        )
    )

    val billers = listOf(
        UtilityBiller("DESCO", "DESCO (Prepaid & Postpaid)", "ডেসকো বিদ্যুৎ", "Electricity", "বিদ্যুৎ", "Customer Account No / Meter No", "গ্রাহক হিসাব নম্বর / মিটার নম্বর", "102938475"),
        UtilityBiller("DPDC", "DPDC (Dhaka Power Distribution)", "ডিপিডিসি বিদ্যুৎ", "Electricity", "বিদ্যুৎ", "Customer Bill No", "গ্রাহক বিল নম্বর", "77889912"),
        UtilityBiller("BREB", "Palli Bidyut (BREB)", "পল্লি বিদ্যুৎ (আরইবি)", "Electricity", "বিদ্যুৎ", "SMS Account No", "এসএমএস হিসাব নম্বর", "1039485761"),
        UtilityBiller("NESCO", "NESCO Electricity", "নেসকো বিদ্যুৎ", "Electricity", "বিদ্যুৎ", "Customer Number", "গ্রাহক নম্বর", "554433221"),
        UtilityBiller("TITAS", "Titas Gas Transmission", "তিতাস গ্যাস", "Gas", "গ্যাস", "Customer Code / Bill Number", "গ্রাহক কোড / বিল নম্বর", "99887766"),
        UtilityBiller("JALALABAD", "Jalalabad Gas", "জালালাবাদ গ্যাস", "Gas", "গ্যাস", "Customer ID", "গ্রাহক আইডি", "33221100"),
        UtilityBiller("DWASA", "Dhaka WASA", "ঢাকা ওয়াসা", "Water", "পানি", "WASA Bill No", "ওয়াসা বিল নম্বর", "12345678"),
        UtilityBiller("CWASA", "Chattogram WASA", "চট্টগ্রাম ওয়াসা", "Water", "পানি", "Customer No", "গ্রাহক নম্বর", "87654321"),
        UtilityBiller("LINK3", "Link3 Broadband Internet", "লিংক৩ ইন্টারনেট", "Internet", "ইন্টারনেট", "Subscriber ID", "সাবস্ক্রাইবার আইডি", "L3-9821"),
        UtilityBiller("CARNIVAL", "Carnival Internet", "কার্নিভাল ইন্টারনেট", "Internet", "ইন্টারনেট", "Customer Code", "গ্রাহক কোড", "CRN-4421"),
        UtilityBiller("AKASH", "AKASH Digital TV (DTH)", "আকাশ ডিজিটাল টিভি", "Television", "টেলিভিশন", "Subscriber ID / Smartcard No", "স্মার্টকার্ড নম্বর", "0192837465"),
        UtilityBiller("BUET", "BUET Examination & Tuition", "বুয়েট ফি", "Education", "শিক্ষা", "Student Roll / Application ID", "রোল / আবেদন আইডি", "2005012")
    )

    val banks = listOf(
        ScheduledBank("CITY", "The City Bank Limited", "দ্য সিটি ব্যাংক", listOf("NPSB", "BEFTN", "RTGS")),
        ScheduledBank("BRAC", "BRAC Bank PLC", "ব্র্যাক ব্যাংক", listOf("NPSB", "BEFTN", "RTGS")),
        ScheduledBank("DBBL", "Dutch-Bangla Bank (DBBL/Rocket)", "ডাচ-বাংলা ব্যাংক", listOf("NPSB", "BEFTN")),
        ScheduledBank("EBL", "Eastern Bank PLC (EBL)", "ইস্টার্ন ব্যাংক", listOf("NPSB", "BEFTN", "RTGS")),
        ScheduledBank("ISLAMI", "Islami Bank Bangladesh PLC", "ইসলামী ব্যাংক বাংলাদেশ", listOf("NPSB", "BEFTN", "RTGS")),
        ScheduledBank("SONALI", "Sonali Bank PLC", "সোনালী ব্যাংক", listOf("BEFTN", "RTGS")),
        ScheduledBank("SCB", "Standard Chartered Bangladesh", "স্ট্যান্ডার্ড চার্টার্ড", listOf("NPSB", "BEFTN", "RTGS")),
        ScheduledBank("MTB", "Mutual Trust Bank PLC", "মিউচুয়াল ট্রাস্ট ব্যাংক", listOf("NPSB", "BEFTN"))
    )

    val sampleRecipients = listOf(
        Beneficiary(beneficiaryId = "rec_1", userId = "", name = "Tanvir Hasan", phoneOrAccount = "+8801712345678", type = "MOBILE_WALLET"),
        Beneficiary(beneficiaryId = "rec_2", userId = "", name = "Nusrat Jahan", phoneOrAccount = "+8801819876543", type = "MOBILE_WALLET"),
        Beneficiary(beneficiaryId = "rec_3", userId = "", name = "Kamrul Islam", phoneOrAccount = "+8801915556677", type = "MOBILE_WALLET"),
        Beneficiary(beneficiaryId = "rec_4", userId = "", name = "Mother (আম্মু)", phoneOrAccount = "+8801711223344", type = "MOBILE_WALLET"),
        Beneficiary(beneficiaryId = "rec_5", userId = "", name = "BRAC Bank Savings", phoneOrAccount = "1501204892019001", type = "BANK_ACCOUNT", bankName = "BRAC Bank PLC")
    )
}
