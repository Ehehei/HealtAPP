# Data Layer — Полный гайд

## Зачем нужен Data слой?

Data слой — это **мост между бизнес-логикой (domain) и источниками данных** (база данных, API, Health Connect).

Domain слой говорит: *«Мне нужен список записей давления пользователя»*
Data слой отвечает: *«Ок, я достану их из Room базы, преобразую в нужный формат и верну»*

Domain **не знает** откуда берутся данные — из SQLite, с сервера или из кэша. Это и есть суть Clean Architecture.

---

## Структура

```
data/
├── local/
│   ├── entity/        ← Таблицы базы данных
│   ├── dao/           ← Запросы к базе данных
│   ├── converter/     ← Конвертеры типов для Room
│   └── db/            ← Сама база данных
├── mapper/            ← Преобразователи Entity ↔ Domain Model
├── repository/        ← Реализация репозиториев из domain
├── source/            ← Внешние источники данных (Health Connect)
└── di/                ← Dependency Injection (сборка всего вместе)
```

---

## 1. `local/entity/` — Таблицы базы данных

### Что это?
Entity — это Kotlin data class с аннотацией `@Entity`. Каждый entity = **одна таблица** в SQLite базе данных.

### Зачем отдельно от domain моделей?
Потому что у базы данных свои правила:
- Room не умеет хранить `LocalDate` — нужно хранить как `Long` (миллисекунды)
- Room не умеет хранить `enum` напрямую — нужно хранить как `String` или `Int`
- В таблице нужны аннотации `@PrimaryKey`, `@ColumnInfo` — это детали БД, им не место в domain

**Domain модель** — это то, как данные выглядят для бизнес-логики.
**Entity** — это то, как данные лежат в базе.

### Пример

Domain модель (чистая, без привязки к БД):
```kotlin
data class BloodPressure(
    val id: Long,
    val userId: String,
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    val date: LocalDateTime    // ← Room не понимает этот тип
)
```

Entity (заточена под Room):
```kotlin
@Entity(tableName = "blood_pressure")
data class BloodPressureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,          // ← Room сам генерирует ID
    val userId: String,
    val systolicPressure: Int,
    val diastolicPressure: Int,
    val pulse: Int,
    val date: Long              // ← Хранится как число (epoch millis)
)
```

### Какие entity нужны в нашем проекте?

| Entity | Таблица | Что хранит |
|---|---|---|
| `BloodPressureEntity` | `blood_pressure` | Записи давления и пульса |
| `BodyPhotoEntity` | `body_photo` | Путь к фото, тип, заметка |
| `StateOfHealthEntity` | `state_of_health` | Самочувствие, сахар, температура |
| `StepEntity` | `step_record` | Шаги за день |
| `UserProfileEntity` | `user_profile` | Имя, рост, вес, дата рождения |
| `WeightEntity` | `weight_record` | Записи веса по датам |

---

## 2. `local/dao/` — Data Access Objects (запросы к БД)

### Что это?
DAO — это интерфейс с аннотациями Room. Каждый метод = **один SQL запрос**. Room генерирует реализацию автоматически во время компиляции.

### Зачем?
Чтобы не писать сырой SQL руками. Ты описываешь *что* хочешь получить, Room генерирует *как*.

### Пример

```kotlin
@Dao
interface BloodPressureDao {

    // Вставить запись. REPLACE = если запись с таким ID уже есть, перезаписать
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BloodPressureEntity): Long

    // Обновить существующую запись
    @Update
    suspend fun update(entity: BloodPressureEntity)

    // Удалить по ID
    @Query("DELETE FROM blood_pressure WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Получить одну запись по ID
    @Query("SELECT * FROM blood_pressure WHERE id = :id")
    suspend fun getById(id: Long): BloodPressureEntity?

    // Получить все записи пользователя, отсортированные по дате
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY date DESC")
    suspend fun getByUserId(userId: String): List<BloodPressureEntity>

    // Получить записи за период
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId AND date BETWEEN :from AND :to")
    suspend fun getByDateRange(userId: String, from: Long, to: Long): List<BloodPressureEntity>

    // Подписка на изменения — Flow автоматически обновляется при изменении таблицы
    @Query("SELECT * FROM blood_pressure WHERE userId = :userId ORDER BY date DESC")
    fun observeByUserId(userId: String): Flow<List<BloodPressureEntity>>
}
```

### Важные моменты

- `suspend fun` — запрос выполняется в корутине (не блокирует UI)
- `fun ... : Flow<>` — реактивный поток, Room сам пушит новые данные при изменении таблицы
- Один DAO на одну таблицу (entity)

### Какие DAO нужны?

| DAO | Для какой таблицы |
|---|---|
| `BloodPressureDao` | `blood_pressure` |
| `BodyPhotoDao` | `body_photo` |
| `StateOfHealthDao` | `state_of_health` |
| `StepDao` | `step_record` |
| `UserProfileDao` | `user_profile` |
| `WeightDao` | `weight_record` |

---

## 3. `local/converter/` — Type Converters

### Что это?
Room понимает только примитивные типы: `Int`, `Long`, `String`, `Float`, `Boolean`, `ByteArray`. Если у тебя в Entity есть другой тип — нужен конвертер.

### Зачем?
В наших Entity мы храним даты как `Long`. Конвертеры говорят Room:
- *«Когда видишь `LocalDate` → сохрани как `Long`»*
- *«Когда читаешь `Long` из базы → верни как `LocalDate`»*

### Пример

```kotlin
class DateConverters {

    // LocalDate → Long (для записи в БД)
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    // Long → LocalDate (для чтения из БД)
    @TypeConverter
    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    // Аналогично для LocalDateTime
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long =
        dateTime.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(millis: Long): LocalDateTime =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
}
```

### Когда нужны конвертеры?

| Тип в коде | Тип в базе | Нужен конвертер? |
|---|---|---|
| `Int`, `Long`, `String` | Такой же | Нет |
| `LocalDate` | `Long` | Да |
| `LocalDateTime` | `Long` | Да |
| `Enum` (Gender, PhotoType) | `String` | Да (или хранить как String в entity) |

---

## 4. `local/db/` — Room Database

### Что это?
Один класс, который объединяет **все entity** и **все DAO** в единую базу данных.

### Зачем?
Это точка входа в базу данных. Room читает этот класс и:
- Создаёт SQLite файл с нужными таблицами
- Генерирует реализации всех DAO
- Управляет миграциями при обновлении схемы

### Пример

```kotlin
@Database(
    entities = [
        BloodPressureEntity::class,
        BodyPhotoEntity::class,
        StateOfHealthEntity::class,
        StepEntity::class,
        UserProfileEntity::class,
        WeightEntity::class
    ],
    version = 1,
    exportSchema = true     // сохраняет схему в JSON для миграций
)
@TypeConverters(DateConverters::class)  // подключаем конвертеры
abstract class HealthDatabase : RoomDatabase() {
    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun bodyPhotoDao(): BodyPhotoDao
    abstract fun stateOfHealthDao(): StateOfHealthDao
    abstract fun stepDao(): StepDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightDao(): WeightDao
}
```

### Важно
- **Один** Database на всё приложение (singleton)
- `version = 1` — при изменении схемы нужно увеличивать и писать миграцию
- `exportSchema = true` — Room сохраняет JSON-схему в папку `/schemas` (полезно для тестов и миграций)

---

## 5. `mapper/` — Маппeры (преобразователи)

### Что это?
Функции, которые преобразуют Entity → Domain Model и обратно.

### Зачем?
Entity и Domain модель — это **разные** классы. DAO возвращает Entity, а репозиторий должен вернуть Domain модель. Маппер — это мост между ними.

```
[Room DB] → Entity → Mapper → Domain Model → [Use Case]
[Use Case] → Domain Model → Mapper → Entity → [Room DB]
```

### Пример

```kotlin
// BloodPressureMapper.kt

fun BloodPressureEntity.toDomain() = BloodPressure(
    id = id,
    userId = userId,
    systolicPressure = systolicPressure,
    diastolicPressure = diastolicPressure,
    pulse = pulse,
    date = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

fun BloodPressure.toEntity() = BloodPressureEntity(
    id = id,
    userId = userId,
    systolicPressure = systolicPressure,
    diastolicPressure = diastolicPressure,
    pulse = pulse,
    date = date.atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
)

// Для списков
fun List<BloodPressureEntity>.toDomain() = map { it.toDomain() }
```

### Зачем extension-функции?
Чистый синтаксис: `entity.toDomain()` вместо `BloodPressureMapper.toDomain(entity)`. Kotlin-way.

### Какие маппeры нужны?

По одному файлу на пару Entity ↔ Domain:
- `BloodPressureMapper.kt`
- `BodyPhotoMapper.kt`
- `StateOfHealthMapper.kt`
- `StepMapper.kt`
- `UserProfileMapper.kt`
- `WeightMapper.kt`

---

## 6. `repository/` — Реализации репозиториев

### Что это?
Классы, которые реализуют интерфейсы из domain слоя. Это **самая важная** часть data слоя.

### Зачем?
Domain слой определяет **контракт** (интерфейс):
```kotlin
// domain/repository/BloodPressureRepository.kt (уже написан)
interface BloodPressureRepository {
    suspend fun insert(bp: BloodPressure): Long
    suspend fun getByUserId(userId: String): List<BloodPressure>
    fun observeByUserId(userId: String): Flow<List<BloodPressure>>
    // ...
}
```

Data слой предоставляет **реализацию**:
```kotlin
// data/repository/BloodPressureRepositoryImpl.kt
class BloodPressureRepositoryImpl(
    private val dao: BloodPressureDao
) : BloodPressureRepository {

    override suspend fun insert(bp: BloodPressure): Long =
        dao.insert(bp.toEntity())

    override suspend fun getByUserId(userId: String): List<BloodPressure> =
        dao.getByUserId(userId).toDomain()

    override fun observeByUserId(userId: String): Flow<List<BloodPressure>> =
        dao.observeByUserId(userId).map { entities -> entities.toDomain() }

    override suspend fun update(bp: BloodPressure) =
        dao.update(bp.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    override suspend fun getById(id: Long): BloodPressure? =
        dao.getById(id)?.toDomain()

    override suspend fun getByDateRange(
        userId: String,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<BloodPressure> =
        dao.getByDateRange(
            userId,
            from.toEpochMilli(),  // через маппер
            to.toEpochMilli()
        ).toDomain()
}
```

### Паттерн простой

Каждый метод репозитория делает 3 вещи:
1. **Принимает** domain модель
2. **Конвертирует** в entity (через маппер)
3. **Вызывает** DAO и конвертирует результат обратно

```
UseCase → Repository(domain model) → Mapper → DAO(entity) → Room → SQLite
SQLite → Room → DAO(entity) → Mapper → Repository(domain model) → UseCase
```

### Какие реализации нужны?

| Реализация | Интерфейс из domain | Источник данных |
|---|---|---|
| `BloodPressureRepositoryImpl` | `BloodPressureRepository` | Room (DAO) |
| `BodyPhotoRepositoryImpl` | `BodyPhotoRepository` | Room (DAO) |
| `StateOfHealthRepositoryImpl` | `StateOfHealthRepository` | Room (DAO) |
| `StepRepositoryImpl` | `StepRepository` | Room (DAO) |
| `UserProfileRepositoryImpl` | `UserProfileRepository` | Room (DAO) |
| `WeightRepositoryImpl` | `WeightRepository` | Room (DAO) |

---

## 7. `source/` — Внешние источники данных

### Что это?
Реализации источников, которые **не являются** нашей базой данных. В нашем случае — Google Health Connect.

### Зачем?
`HealthConnectDataSource` — это интерфейс из domain. Он говорит *«дай мне шаги с устройства»*. Реализация в source/ знает **как** общаться с Google Health Connect API.

### Пример

```kotlin
class HealthConnectDataSourceImpl(
    private val context: Context
) : HealthConnectDataSource {

    private val client by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    override suspend fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE

    override suspend fun getSteps(from: LocalDate, to: LocalDate): List<StepRecord> {
        val response = client.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(/* ... */)
            )
        )
        return response.records.map { record ->
            StepRecord(
                id = 0,
                userId = "",
                steps = record.count.toInt(),
                date = record.startTime.toLocalDate()
            )
        }
    }
}
```

### Что ещё может быть в source/?
- `RemoteDataSource` — если добавишь синхронизацию с сервером (Retrofit)
- `FirebaseDataSource` — если будешь тянуть данные из Firestore
- Пока хватает только `HealthConnectDataSourceImpl`

---

## 8. `di/` — Dependency Injection модуль

### Что это?
Koin модуль, который говорит: *«когда кому-то нужен `BloodPressureRepository` — дай ему `BloodPressureRepositoryImpl`, а в него передай `BloodPressureDao`»*.

### Зачем?
Без DI тебе пришлось бы **вручную** создавать все зависимости:
```kotlin
// Без DI — ад
val db = Room.databaseBuilder(...).build()
val dao = db.bloodPressureDao()
val repo = BloodPressureRepositoryImpl(dao)
val useCase = SaveBloodPressureUseCase(repo)
val viewModel = BloodPressureViewModel(useCase)
```

С Koin ты описываешь это **один раз**, а дальше всё внедряется автоматически.

### Пример

```kotlin
val dataModule = module {

    // === Database (один экземпляр на всё приложение) ===
    single {
        Room.databaseBuilder(
            get(),                          // Context (Koin найдёт сам)
            HealthDatabase::class.java,
            "health_database"
        ).build()
    }

    // === DAOs (берём из Database) ===
    single { get<HealthDatabase>().bloodPressureDao() }
    single { get<HealthDatabase>().bodyPhotoDao() }
    single { get<HealthDatabase>().stateOfHealthDao() }
    single { get<HealthDatabase>().stepDao() }
    single { get<HealthDatabase>().userProfileDao() }
    single { get<HealthDatabase>().weightDao() }

    // === Repositories (реализация → интерфейс) ===
    singleOf(::BloodPressureRepositoryImpl) { bind<BloodPressureRepository>() }
    singleOf(::BodyPhotoRepositoryImpl)     { bind<BodyPhotoRepository>() }
    singleOf(::StateOfHealthRepositoryImpl) { bind<StateOfHealthRepository>() }
    singleOf(::StepRepositoryImpl)          { bind<StepRepository>() }
    singleOf(::UserProfileRepositoryImpl)   { bind<UserProfileRepository>() }
    singleOf(::WeightRepositoryImpl)        { bind<WeightRepository>() }

    // === Внешние источники ===
    singleOf(::HealthConnectDataSourceImpl) { bind<HealthConnectDataSource>() }
}
```

### Как это работает?

```
get() просит BloodPressureRepository
  → Koin видит: bind<BloodPressureRepository>() → BloodPressureRepositoryImpl
  → BloodPressureRepositoryImpl нужен BloodPressureDao
  → Koin видит: get<HealthDatabase>().bloodPressureDao()
  → HealthDatabase нужен Context
  → Koin берёт androidContext из Application
  → Всё собрано ✓
```

---

## Полная картина: как данные текут через приложение

```
┌─────────────────────────────────────────────────────────┐
│                      APP (UI)                           │
│  ViewModel вызывает UseCase                             │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                     DOMAIN                              │
│  UseCase вызывает Repository (интерфейс)                │
│  UseCase НЕ ЗНАЕТ про Room, Entity, SQL                 │
└──────────────────────┬──────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────┐
│                      DATA                               │
│                                                         │
│  Repository (impl)                                      │
│       │                                                 │
│       ├── Mapper: Domain Model ←→ Entity                │
│       │                                                 │
│       ├── DAO → Room → SQLite (локальные данные)        │
│       │                                                 │
│       └── Source → Health Connect (внешние данные)       │
│                                                         │
│  DI Module: собирает всё вместе                         │
└─────────────────────────────────────────────────────────┘
```

---

## FAQ

### Почему Entity и Domain Model — разные классы?
Потому что если Room изменит API или ты переедешь на другую БД — domain слой не изменится. Domain не зависит от деталей хранения.

### Почему маппeры — отдельные файлы, а не внутри Repository?
Чтобы репозиторий оставался чистым и маппeры можно было тестировать отдельно.

### Зачем Flow в DAO?
`Flow` автоматически обновляет UI при изменении данных в таблице. Записал новое давление → список на экране обновился сам.

### Можно ли без DI?
Можно, но будешь вручную создавать 20+ объектов и передавать зависимости. Koin делает это за тебя.

### Зачем suspend в DAO?
Room запрещает обращаться к БД в main thread (UI зависнет). `suspend` = запрос выполняется в фоновом потоке через корутины.
