package realized

import com.github.rs17.mulligan.DefaultObject
import dataio.CSVImporter.ImportMatcher

object MatchersRealized {
  case class StockRow(symbol: String, realizedProfit: Double, unrealizedProfit: Double, totalProfit: Double) extends DefaultObject

  object StockRowMatcher extends ImportMatcher[StockRow] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Realized & Unrealized Performance Summary" && strs(2) == "Stocks"

    override def builder: Array[String] => StockRow = (strs: Array[String]) =>
      StockRow(
        symbol = strs(3),
        realizedProfit = strs(9).toDouble,
        unrealizedProfit = strs(14).toDouble,
        totalProfit = strs(9).toDouble + strs(14).toDouble
      )

    override def testMe: Boolean = {
      val strs = Array("", "", "Stocks")
      val result = matcher(strs)
      !result
    }
  }

  case class OptionRow(symbolUnderlying: String, putCall: String, date: String, strike: Double, realizedProfit: Double, unrealizedProfit: Double, totalProfit: Double) extends DefaultObject

  object OptionRowMatcher extends ImportMatcher[OptionRow] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Realized & Unrealized Performance Summary" &&
        strs(2) == "Equity and Index Options"

    override def builder: Array[String] => OptionRow = (strs: Array[String]) => {
      val symbolRow = strs(3).split(" ")
      OptionRow(
        symbolUnderlying = symbolRow(0),
        putCall = symbolRow(3),
        date = symbolRow(1),
        strike = symbolRow(2).toDouble,
        realizedProfit = strs(9).toDouble,
        unrealizedProfit = strs(14).toDouble,
        totalProfit = strs(9).toDouble + strs(14).toDouble
      )
    }
  }

  case class OpenStockRow(symbol: String, size: Integer, value: Double, unrealizedProfit: Double) extends DefaultObject

  object OpenStockRowMatcher extends ImportMatcher[OpenStockRow] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Open Positions" && strs(1) == "Data" && strs(3) == "Stocks"

    override def builder: Array[String] => OpenStockRow = (strs: Array[String]) =>
      OpenStockRow(
        symbol = strs(5),
        size = strs(6).toInt,
        value = strs(11).toDouble,
        unrealizedProfit = strs(12).toDouble
      )

    override def testMe: Boolean = {
      val strs = Array("Open Positions", "Data", "Summary", "Stocks")
      matcher(strs)
    }
  }

  case class OpenOptionRow(symbolUnderlying: String, putCall: String, date: String, strike: Double, size: Integer, unrealizedProfit: Double) extends DefaultObject

  object OpenOptionRowMatcher extends ImportMatcher[OpenOptionRow] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Open Positions" && strs(1) == "Data" &&
    strs(3) == "Equity and Index Options"

    override def builder: Array[String] => OpenOptionRow = (strs: Array[String]) => {
      val symbolRow = strs(5).split(" ")
      OpenOptionRow(
        symbolUnderlying = symbolRow(0),
        putCall = symbolRow(3),
        date = symbolRow(1),
        strike = symbolRow(2).toDouble,
        size = strs(6).toInt,
        unrealizedProfit = strs(12).toDouble
      )
    }
  }
}
