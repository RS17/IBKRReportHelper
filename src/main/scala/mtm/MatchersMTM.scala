package mtm

import com.github.rs17.mulligan.DefaultObject
import dataio.CSVImporter.ImportMatcher

object MatchersMTM {
  case class StockRowMTM(symbol: String, profit: Double) extends DefaultObject

  object StockRowMatcherMTM extends ImportMatcher[StockRowMTM] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Positions and Mark-to-Market Profit and Loss" && strs(3) == "Stocks" &&
        strs(2) == "Summary"

    override def builder: Array[String] => StockRowMTM = (strs: Array[String]) =>
      StockRowMTM(
        symbol = strs(5),
        profit = strs(17).toDouble
      )

    override def testMe: Boolean = {
      val strs = Array("", "", "Stocks")
      val result = matcher(strs)
      !result
    }
  }

  case class OptionRowMTM(symbolUnderlying: String, putCall: String, date: String, strike: Double, profit: Double) extends DefaultObject

  object OptionRowMatcherMTM extends ImportMatcher[OptionRowMTM] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Positions and Mark-to-Market Profit and Loss" &&
        strs(3) == "Equity and Index Options" &&
        strs(2) == "Summary"

    override def builder: Array[String] => OptionRowMTM = (strs: Array[String]) => {
      val symbolRow = strs(6).split(" ")
      OptionRowMTM(
        symbolUnderlying = symbolRow(0),
        putCall = symbolRow(3),
        date = symbolRow(1),
        strike = symbolRow(2).toDouble,
        profit = strs(17).toDouble
      )
    }
  }
}
