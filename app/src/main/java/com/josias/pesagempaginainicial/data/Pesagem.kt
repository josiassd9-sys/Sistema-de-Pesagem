package com.josias.pesagempaginainicial.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pesagens")
data class Pesagem(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    val cliente: String,
    val motorista: String,
    val placa: String,
    val pesoAtual: Double,
    val tara: Double,
    val desconto: Double,
    val subtotal: Double,
    val pesoLiquidoTotal: Double,
    val valor: Double,
    val timestamp: Long = System.currentTimeMillis()
)
