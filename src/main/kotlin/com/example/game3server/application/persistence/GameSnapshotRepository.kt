package com.example.game3server.application.persistence

import com.example.game3server.domain.Game

data class GameSnapshot<OffsetType>(val game: Game, val offset: OffsetType)

interface GameSnapshotRepository<OffsetType> {
    fun get(): GameSnapshot<OffsetType>
    fun create(): GameSnapshot<OffsetType>
}
