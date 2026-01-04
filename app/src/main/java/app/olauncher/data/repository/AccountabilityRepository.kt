package app.olauncher.data.repository

import app.olauncher.data.local.AccountabilityDao
import app.olauncher.data.local.AccountabilityEntity
import kotlinx.coroutines.flow.Flow

class AccountabilityRepository(private val accountabilityDao: AccountabilityDao) {
    
    val allLogs: Flow<List<AccountabilityEntity>> = accountabilityDao.getAllLogs()

    suspend fun getLogForDate(date: String): AccountabilityEntity? {
        return accountabilityDao.getLogForDate(date)
    }

    suspend fun insertLog(log: AccountabilityEntity) {
        accountabilityDao.insertLog(log)
    }
}
