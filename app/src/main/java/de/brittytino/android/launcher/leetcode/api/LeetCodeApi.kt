package de.brittytino.android.launcher.leetcode.api

import retrofit2.http.Body
import retrofit2.http.POST

interface LeetCodeApi {
    @POST("graphql")
    suspend fun getUserProfile(@Body query: GraphQLQuery): GraphQLResponse
}

data class GraphQLQuery(
    val query: String,
    val variables: Map<String, Any>
)

data class GraphQLResponse(
    val data: Data?
)

data class Data(
    val matchedUser: MatchedUser?,
    val activeDailyCodingChallengeQuestion: ActiveDailyCodingChallengeQuestion?
)

data class ActiveDailyCodingChallengeQuestion(
    val date: String,
    val link: String,
    val question: Question
)

data class Question(
    val questionId: String,
    val frontendQuestionId: String,
    val title: String,
    val titleSlug: String,
    val difficulty: String
)

data class MatchedUser(
    val username: String,
    val profile: Profile,
    val submitStats: SubmitStats,
    val submissionCalendar: String // JSON string
)

data class Profile(
    val realName: String?,
    val userAvatar: String?,
    val ranking: Int
)

data class SubmitStats(
    val acSubmissionNum: List<SubmissionNum>
)

data class SubmissionNum(
    val difficulty: String,
    val count: Int,
    val submissions: Int
)
