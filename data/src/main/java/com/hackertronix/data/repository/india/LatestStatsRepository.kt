package com.hackertronix.data.repository.india

import com.hackertronix.data.local.Covid19StatsDatabase
import com.hackertronix.data.network.IndApi
import com.hackertronix.model.india.latest.Latest
import com.hackertronix.model.overview.Overview
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LatestStatsRepository(
    private val apiClient: IndApi,
    private val database: Covid19StatsDatabase
) {

    fun getLatestStats(): Observable<Latest> {
        return database.latestStatsDao().getLatestStats()
            .toObservable()
            .flatMap {
                if (it.isEmpty()) {
                    return@flatMap getLatestStatsFromApi()
                }
                return@flatMap Observable.just(it.first())
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getLatestStatsFromApi(): Observable<Latest>? {
        return apiClient.getLatestStats().toObservable()
            .doOnNext {
                database.latestStatsDao().deleteLatest()
                database.latestStatsDao().insertLatest(it)
            }
    }


}