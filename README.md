# برنامج ديون عيادة الرحمن (Al-Rahman Clinic Debt Management System)

تطبيق أندرويد متكامل ومصمم خصيصاً لإدارة سجلات ديون المرضى، وتتبع المستحقات المالية، والجدولة التلقائية لرسائل التذكير عبر SMS وواتساب، مع توفير خيارات متقدمة للنسخ الاحتياطي وتصدير التقارير بصيغ PDF و Excel.

**برمجة وإشراف:** الدكتور / مالك الهمة  
**رقم الهاتف:** 771124103  

---

## 📱 مميزات التطبيق الرئيسية

### 1. إدارة الديون والمرضى (Room Database)
- تسجيل سريع لبيانات المريض مع حقول ذكية واقتراحات تلقائية لنوع العمل الطبي (حشوة أسنان، علاج عصب، تقويم، زراعة، تنظيف، إلخ).
- مبالغ سريعة وافتراضية بنقرة زر (+5,000، +10,000، +20,000، +50,000، +100,000 د.ع/ريال).
- تتبع حالة السداد: تسديد جزئي، أو سداد كامل للحساب.
- بحث فوري باسم المريض، نوع العمل، رقم الهاتف، أو المنطقة السكنية.
- تصنيف وتبويب لحظي: (الكل، متبقي الديون، الحسابات المسددة، التذكيرات المجدولة).

### 2. الجدولة والأتمتة الذكية للرسائل (SMS & WhatsApp Automation)
- **جدولة دقيقة عبر ساعة الهاتف:** استخدام `AlarmManager` و `SCHEDULE_EXACT_ALARM` المتوافقة مع أندرويد 15.
- **إرسال رسائل SMS تلقائية في الخلفية:** عبر `SmsManager` بالتعاون مع `SmsAlarmReceiver`.
- **استرجاع التنبيهات بعد إعادة التشغيل:** عبر `BootReceiver` لإعادة جدولة كافة التذكيرات غير المنتهية تلقائياً.
- **تكامل مباشر مع واتساب:** زر مخصص لفتح محادثة واتساب مع المريض مباشرة مع تعبئة نص التذكير والمبلغ المتبقي مسبقاً.
- **اتصال هاتفي مباشر:** إمكانية الاتصال بالمريض أو الاتصال بالدكتور المبرمج بنقرة واحدة.

### 3. النسخ الاحتياطي والتقارير المالية (Backup & Export)
- **تصدير كشف حساب PDF:** توليد مستند PDF رسمي منسق بجدول للمرضى والمبالغ المتبقية وإجمالي الديون وتذييل رسمي بالدكتور مالك الهمة.
- **تصدير ملف Excel (CSV):** متوافق كلياً مع قراءة النصوص العربية بترميز UTF-8 مع علامة BOM لفتحه مباشرة في برامج الإكسل.
- **النسخ الاحتياطي والاستعادة المحلية (Local JSON Backup):** تصدير واسترجاع قاعدة البيانات بالكامل عبر Scoped Storage بدون الحاجة للإنترنت، لحماية البيانات عند تبديل الهاتف أو إعادة ضبط المصنع.

### 4. واجهة مستخدم عصرية (UI/UX - Jetpack Compose)
- تصميم مريح ومتناسق يعتمد لوحة ألوان العيادة: **الكحلي (`#0D2040`)، الأزرق (`#1E56A0`)، والأبيض**.
- دعم كامل للغة العربية والاتجاه من اليمين لليسار (RTL).
- بطاقات وإحصائيات مالية واضحة وشريط تذييل سفلي دائم.

---

## 🛠️ البنية التقنية (Tech Stack)

| المكون | التقنية المستخدمة |
|---|---|
| **لغة البرمجة** | Kotlin 2.x |
| **واجهة المستخدم** | Jetpack Compose & Material 3 |
| **إدارة الحالة** | MVVM + StateFlow + ViewModel |
| **قاعدة البيانات** | Room Database (SQLite) + KSP |
| **الجدولة والتنبيهات** | AlarmManager + BroadcastReceivers + NotificationCompat |
| **المستهدف البرمجي** | Android 15 (Target SDK 36, Min SDK 24) |
| **الأتمتة و CI/CD** | GitHub Actions Workflow (`android.yml`) |

---

## 📁 هيكلية المشروع (Project Architecture)

```
app/src/main/
├── AndroidManifest.xml                  # تعريف الأذونات والمستقبلات والأنشطة
├── java/com/example/
│   ├── MainActivity.kt                  # النشاط الرئيسي وضبط وضع الـ RTL
│   ├── alarm/
│   │   └── AlarmScheduler.kt           # فئة جدولة التنبيهات الدقيقة
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt          # قاعدة بيانات Room
│   │   │   └── PatientDebtDao.kt       # استعلامات وجمل الـ DAO
│   │   ├── model/
│   │   │   └── PatientEntity.kt        # كينونة بيانات المريض والديون
│   │   └── repository/
│   │       └── PatientDebtRepository.kt # مستودع البيانات وتصدير PDF و CSV و JSON
│   ├── receiver/
│   │   ├── BootReceiver.kt             # إعادة جدولة الرسائل عند تشغيل الهاتف
│   │   └── SmsAlarmReceiver.kt         # مستقبل التنبيه وإرسال SMS والإشعار
│   ├── ui/
│   │   ├── components/
│   │   │   ├── ClinicDialogs.kt        # نوافذ الدفع وتأكيد الحذف
│   │   │   ├── ClinicStatsCard.kt      # بطاقة الإحصائيات المالية
│   │   │   ├── ClinicTopBar.kt        # الشريط العلوي وأزرار التصدير
│   │   │   ├── FooterDoctorMalik.kt    # تذييل الشاشة وزر الاتصال
│   │   │   ├── PatientCard.kt          # بطاقة عرض المريض والإجراءات السريعة
│   │   │   └── PatientFormSection.kt   # نموذج إضافة وتعديل الدين والجدولة
│   │   ├── screens/
│   │   │   └── MainClinicScreen.kt     # الشاشة الرئيسية وحاويات التبويب
│   │   ├── theme/
│   │   │   ├── Color.kt                # ألوان العيادة (كحلي، أزرق، أبيض)
│   │   │   ├── Theme.kt                # ثيم Material 3
│   │   │   └── Type.kt                 # الخطوط والطباعة
│   │   └── viewmodel/
│   │       └── ClinicViewModel.kt      # إدارة الحالة والعمليات
└── res/
    ├── drawable/                       # أيقونات المتجه والخلفيات
    ├── mipmap-*/                       # أيقونات التطبيق التكيفية (Adaptive Launcher Icons)
    └── values/                         # ملفات النصوص والألوان
```

---

## 📋 فئة الكينونة الأساسية (`PatientEntity.kt`)

```kotlin
package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientName: String,
    val workType: String,
    val amount: Double,
    val phoneNumber: String,
    val area: String = "",
    val scheduledMessage: String = "",
    val scheduledDateTime: Long = 0L,
    val isSent: Boolean = false,
    val isPaid: Boolean = false,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

---

## 🔐 إعدادات البناء التلقائي على GitHub (CI/CD)

يحتوي المشروع على ملف `.github/workflows/android.yml` لبناء ملف الـ APK وتوقيعه تلقائياً.

لإتمام عملية البناء التلقائي في مستودع GitHub الخاص بك:
1. اذهب إلى **Settings** > **Secrets and variables** > **Actions**.
2. أضف الرموز السرية التالية (كما تم إعدادها):
   - `KEYSTORE_BASE64`: ملف مفتاح التوقيع مشفراً بصيغة Base64.
   - `CM_KEYSTORE_PASSWORD`: كلمة مرور Keystore.
   - `CM_KEY_PASSWORD`: كلمة مرور المفتاح.
   - `CM_KEY_ALIAS`: الاسم التعريفي للمفتاح (Alias).
   - `GEMINI_API_KEY`: مفتاح الذكاء الاصطناعي (اختياري).
3. عند عمل `git push` لفرع `main` أو `master`، سيتم بناء ملف الـ APK وتوفيره للتحميل في تبويب **Artifacts**.

---

## 💻 أوامر البناء والاختبار محلياً

```bash
# تجميع واختبار التطبيق
gradle :app:testDebugUnitTest

# بناء ملف APK التجريبي
gradle assembleDebug

# بناء ملف APK النهائي (Release)
gradle assembleRelease
```

---

## 📞 الدعم والتواصل
- **برمجة وتطوير:** الدكتور / مالك الهمة
- **هاتف:** 771124103
- **العيادة:** عيادة الرحمن
