package app.olauncher.data.repository

import app.olauncher.domain.model.UsageRule
import app.olauncher.domain.repository.RulesRepository

class RulesRepositoryImpl(
    private val ruleDao: app.olauncher.data.local.RuleDao
) : RulesRepository {

    override suspend fun getRulesForPackage(packageName: String): List<UsageRule> {
        return ruleDao.getRules(packageName).mapNotNull { entity ->
            app.olauncher.data.local.RuleSerializer.deserialize(entity.ruleType, entity.ruleData)
        }
    }

    override suspend fun addRule(packageName: String, rule: UsageRule) {
        val (type, data) = app.olauncher.data.local.RuleSerializer.serialize(rule)
        ruleDao.insertRule(
            app.olauncher.data.local.RuleEntity(
                packageName = packageName,
                ruleType = type,
                ruleData = data
            )
        )
    }

    override suspend fun clearRules(packageName: String) {
        ruleDao.clearRules(packageName)
    }
}
