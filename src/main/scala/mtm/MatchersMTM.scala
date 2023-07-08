package mtm

import com.github.rs17.mulligan.DefaultObject
import dataio.CSVImporter.ImportMatcher

object MatchersMTM {
  case class StockRowMTM(symbol: String, profit: Double) extends DefaultObject

  object StockRowMatcherMTM extends ImportMatcher[StockRowMTM] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Positions and Mark-to-Market Profit and Loss" && strs(2) == "Stocks"

    override def builder: Array[String] => StockRowMTM = (strs: Array[String]) =>
      StockRowMTM(
        symbol = strs(4),
        profit = strs(16).toDouble
      )

    override def testMe: Boolean = {
      val strs = Array("", "", "Stocks")
      val result = matcher(strs)
      result == false
    }
  }

  case class OptionRowMTM(symbolUnderlying: String, putCall: String, date: String, strike: Double, profit: Double) extends DefaultObject

  object OptionRowMatcherMTM extends ImportMatcher[OptionRowMTM] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Positions and Mark-to-Market Profit and Loss" &&
        strs(2) == "Equity and Index Options"

    override def builder: Array[String] => OptionRowMTM = (strs: Array[String]) => {
      val symbolRow = strs(5).split(" ")
      OptionRowMTM(
        symbolUnderlying = symbolRow(0),
        putCall = symbolRow(3),
        date = symbolRow(1),
        strike = symbolRow(2).toDouble,
        profit = strs(16).toDouble
      )
    }
  }
}
