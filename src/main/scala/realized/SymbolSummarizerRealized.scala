package realized

import base.MatchersBase.DividendRowMatcher
import com.github.rs17.mulligan.DefaultModule
import dataio.CSVImporter.importCSVGreedy
import realized.MatchersRealized.{OptionRow, OptionRowMatcher, StockRowMatcher}

import scala.collection.mutable

/** Summarizes Realized profits.  I built this then realized it's not as useful as MTM */
object SymbolSummarizerRealized extends DefaultModule {
  def main(args: Array[String]): Unit = {

    if (args.length == 0) throw new IllegalArgumentException("Must provide path argument")
    val path = args(0)

    val resultsStock = importCSVGreedy(path, StockRowMatcher).flatten
    val resultsOpts = importCSVGreedy(path, OptionRowMatcher).flatten
    val resultsDivs = importCSVGreedy(path, DividendRowMatcher).flatten

    val symbolMap: mutable.Map[String, List[OptionRow]] = mutable.Map[String, List[OptionRow]]()
    val profitMap: mutable.Map[String, Double] = mutable.Map[String, Double]()

    // setup maps
    for (name <- (resultsOpts.map(_.symbolUnderlying) ++ resultsStock.map(_.symbol) ++ resultsDivs.map(_.symbol)).distinct) {
      symbolMap += name -> List()
      profitMap += name -> 0.0
    }
    println(symbolMap)
    // add option rows
    for (optionRow <- resultsOpts) {
      symbolMap(optionRow.symbolUnderlying) = symbolMap(optionRow.symbolUnderlying) :+ optionRow
    }

    // get option sum
    for ((key, optList) <- symbolMap) {
      profitMap(key) = optList.foldLeft(0.0)(_ + _.totalProfit)
    }

    // get stock sum
    for (stockRow <- resultsStock) {
      profitMap(stockRow.symbol) += stockRow.totalProfit
    }

    // get divs sum
    for (divRow <- resultsDivs) {
      profitMap(divRow.symbol) += divRow.amount
    }

    println("FINAL RESULTS: " + profitMap.mkString("\n"))
  }
}
