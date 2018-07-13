package com.example.sj.formattableedittext

import io.reactivex.Single

object UsersPresenter{
    fun getUsersByQuery(searchQuery: String, limit: Int = 50): Single<List<UserItem>> {
        val friends: MutableList<UserItem> = mutableListOf()
        with(friends) {
            add(UserItem("rkWkZdQkkM", "Galina Fedorova"))
            add(UserItem("HJg4CjkOL", "Dmitryw"))
            add(UserItem("B1l7QqAgHW", "Sergey Petrov"))
            add(UserItem("r1LMMJOxEZ", "Full DiveOne"))
            add(UserItem("SymZ-MC8", "Alexandr Telegin"))
            add(UserItem("SkeyZFMHfl", "Eddie Ow"))
            add(UserItem("B1M272J5GZ", "Alex Suvorov"))
            add(UserItem("S17hVlEsEe", "Dmitry Chernov"))
            add(UserItem("HyH7Odi6x", "Grant Hao-wei Lin"))
            add(UserItem("H1Qi6aSYMg", "Giovanno Yosen Utomo"))
            add(UserItem("HyStUWxreW", "Fausto Sihite"))
            add(UserItem("BJNnTVYCm-", "Josh Holtgrewe"))
            add(UserItem("BJ4ZEc7rLb", "Tom Griffin"))
            add(UserItem("HkvZ-WdI", "Tookey Williams"))
            add(UserItem("BJe8O3m0I", "Marina Gonokhina"))
            add(UserItem("rJ5Gu-1Le", "Brandon Torres"))
            add(UserItem("SJVoTq9w", "Павел Кравцов"))
            add(UserItem("B1gPdCN2kG", "Роман Тяглик"))
            add(UserItem("B1bkhsewIz", "Воробей"))
        }


        if (searchQuery.isEmpty()) return Single.just(friends)
        return Single.just(friends.filter { it.displayName.contains(searchQuery, true) }) // intellectual sorting algorithm
    }
}