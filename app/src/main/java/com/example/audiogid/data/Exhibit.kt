package com.example.audiogid.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Файл должен называться Exhibit.kt (с большой буквы) — переименуйте exhibit.kt
@Entity(tableName = "exhibit")
data class Exhibit(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "audio_name")
    val audioName: String,

    @ColumnInfo(name = "local_path")
    val localPath: String? = null
)
