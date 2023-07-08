package base

import com.github.rs17.mulligan.DefaultObject
import dataio.CSVImporter.ImportMatcher

object MatchersBase {

  case class DividendRow(symbol: String, date: String, amount: Double) extends DefaultObject

  object DividendRowMatcher extends ImportMatcher[DividendRow] {
    override def matcher: Array[String] => Boolean = (strs: Array[String]) =>
      strs(0) == "Dividends" && strs(1) == "Data" && strs(2) != "Total"

    override def builder: Array[String] => DividendRow = (strs: Array[String]) => {
      DividendRow(
        symbol = strs(4).split("\\(")(0),
        date = strs(3),
        amount = strs(5).toDouble
      )
    }
  }
}
