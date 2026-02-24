package com.altercard

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CardDao_Impl(
  __db: RoomDatabase,
) : CardDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCard: EntityInsertAdapter<Card>

  private val __deleteAdapterOfCard: EntityDeleteOrUpdateAdapter<Card>

  private val __updateAdapterOfCard: EntityDeleteOrUpdateAdapter<Card>
  init {
    this.__db = __db
    this.__insertAdapterOfCard = object : EntityInsertAdapter<Card>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `cards` (`id`,`name`,`number`,`barcodeData`,`barcodeFormat`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Card) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.number)
        val _tmpBarcodeData: String? = entity.barcodeData
        if (_tmpBarcodeData == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpBarcodeData)
        }
        val _tmpBarcodeFormat: String? = entity.barcodeFormat
        if (_tmpBarcodeFormat == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpBarcodeFormat)
        }
      }
    }
    this.__deleteAdapterOfCard = object : EntityDeleteOrUpdateAdapter<Card>() {
      protected override fun createQuery(): String = "DELETE FROM `cards` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Card) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfCard = object : EntityDeleteOrUpdateAdapter<Card>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `cards` SET `id` = ?,`name` = ?,`number` = ?,`barcodeData` = ?,`barcodeFormat` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Card) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.number)
        val _tmpBarcodeData: String? = entity.barcodeData
        if (_tmpBarcodeData == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpBarcodeData)
        }
        val _tmpBarcodeFormat: String? = entity.barcodeFormat
        if (_tmpBarcodeFormat == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpBarcodeFormat)
        }
        statement.bindLong(6, entity.id.toLong())
      }
    }
  }

  public override suspend fun insert(card: Card): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCard.insert(_connection, card)
  }

  public override suspend fun delete(card: Card): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfCard.handle(_connection, card)
  }

  public override suspend fun update(card: Card): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCard.handle(_connection, card)
  }

  public override fun getAllCards(): Flow<List<Card>> {
    val _sql: String = "SELECT * FROM cards ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("cards")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfNumber: Int = getColumnIndexOrThrow(_stmt, "number")
        val _columnIndexOfBarcodeData: Int = getColumnIndexOrThrow(_stmt, "barcodeData")
        val _columnIndexOfBarcodeFormat: Int = getColumnIndexOrThrow(_stmt, "barcodeFormat")
        val _result: MutableList<Card> = mutableListOf()
        while (_stmt.step()) {
          val _item: Card
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpNumber: String
          _tmpNumber = _stmt.getText(_columnIndexOfNumber)
          val _tmpBarcodeData: String?
          if (_stmt.isNull(_columnIndexOfBarcodeData)) {
            _tmpBarcodeData = null
          } else {
            _tmpBarcodeData = _stmt.getText(_columnIndexOfBarcodeData)
          }
          val _tmpBarcodeFormat: String?
          if (_stmt.isNull(_columnIndexOfBarcodeFormat)) {
            _tmpBarcodeFormat = null
          } else {
            _tmpBarcodeFormat = _stmt.getText(_columnIndexOfBarcodeFormat)
          }
          _item = Card(_tmpId,_tmpName,_tmpNumber,_tmpBarcodeData,_tmpBarcodeFormat)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCard(id: Int): Flow<Card?> {
    val _sql: String = "SELECT * FROM cards WHERE id = ?"
    return createFlow(__db, false, arrayOf("cards")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfNumber: Int = getColumnIndexOrThrow(_stmt, "number")
        val _columnIndexOfBarcodeData: Int = getColumnIndexOrThrow(_stmt, "barcodeData")
        val _columnIndexOfBarcodeFormat: Int = getColumnIndexOrThrow(_stmt, "barcodeFormat")
        val _result: Card?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpNumber: String
          _tmpNumber = _stmt.getText(_columnIndexOfNumber)
          val _tmpBarcodeData: String?
          if (_stmt.isNull(_columnIndexOfBarcodeData)) {
            _tmpBarcodeData = null
          } else {
            _tmpBarcodeData = _stmt.getText(_columnIndexOfBarcodeData)
          }
          val _tmpBarcodeFormat: String?
          if (_stmt.isNull(_columnIndexOfBarcodeFormat)) {
            _tmpBarcodeFormat = null
          } else {
            _tmpBarcodeFormat = _stmt.getText(_columnIndexOfBarcodeFormat)
          }
          _result = Card(_tmpId,_tmpName,_tmpNumber,_tmpBarcodeData,_tmpBarcodeFormat)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
