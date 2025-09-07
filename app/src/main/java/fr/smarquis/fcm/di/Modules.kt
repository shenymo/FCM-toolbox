package fr.smarquis.fcm.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import fr.smarquis.fcm.data.db.AppDatabase
import fr.smarquis.fcm.data.model.Payload
import fr.smarquis.fcm.data.repository.ConnectionHistoryRepository
import fr.smarquis.fcm.data.repository.InMemoryTokenRepository
import fr.smarquis.fcm.data.repository.MessageRepository
import fr.smarquis.fcm.data.repository.TokenRepository
import fr.smarquis.fcm.usecase.GetTokenUseCase
import fr.smarquis.fcm.usecase.ResetTokenUseCase
import fr.smarquis.fcm.usecase.UpdateTokenUseCase
import fr.smarquis.fcm.viewmodel.ConnectionAnalysisViewModel
import fr.smarquis.fcm.viewmodel.MainViewModel
import fr.smarquis.fcm.viewmodel.PresenceLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val main = module {
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { ConnectionAnalysisViewModel(get()) }
    single { MessageRepository(get()) }
    single { ConnectionHistoryRepository(get()) }
    single { PresenceLiveData(get(), get(), get(), get(), scope = CoroutineScope(SupervisorJob() + Default)) }
}

val token = module {
    single<TokenRepository> { InMemoryTokenRepository(get(), scope = CoroutineScope(SupervisorJob() + Default)) }
    single { GetTokenUseCase(get()) }
    single { UpdateTokenUseCase(get()) }
    single { ResetTokenUseCase(get()) }
}

val firebase = module {
    single { FirebaseMessaging.getInstance() }
    single { FirebaseDatabase.getInstance() }
}

val database = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "database")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<AppDatabase>().dao() }
    single { get<AppDatabase>().connectionHistoryDao() }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `connection_history` (
                `id` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `network_type` TEXT,
                `duration` INTEGER,
                `device_info` TEXT,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

val json = module {
    single {
        Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(Payload::class.java, "type")
                    .withSubtype(Payload.App::class.java, "app")
                    .withSubtype(Payload.Link::class.java, "link")
                    .withSubtype(Payload.Ping::class.java, "ping")
                    .withSubtype(Payload.Raw::class.java, "raw")
                    .withSubtype(Payload.Text::class.java, "text"),
            )
            .add(KotlinJsonAdapterFactory()).build()
    }
    single {
        mapOf(
            "app" to Payload.App::class.java,
            "link" to Payload.Link::class.java,
            "ping" to Payload.Ping::class.java,
            "text" to Payload.Text::class.java,
        )
    }
}
