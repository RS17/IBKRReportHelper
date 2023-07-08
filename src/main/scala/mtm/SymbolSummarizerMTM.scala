package mtm

import base.MatchersBase.DividendRowMatcher
import com.github.rs17.mulligan.DefaultModule
import dataio.CSVImporter.importCSVGreedy
import mtm.MatchersMTM.{OptionRowMTM, OptionRowMatcherMTM, StockRowMatcherMTM}

import scala.collection.mutable

/** Summarizes MTM profits.*/
object SymbolSummarizerMTM extends DefaultModule {
  def main(args: Array[String]): Unit = {

    if (args.length == 0) throw new IllegalArgumentException("Must provide path argument")
    val path = args(0)

    val resultsStock = importCSVGreedy(path, StockRowMatcherMTM).flatten
    val resultsOpts = importCSVGreedy(path, OptionRowMatcherMTM).flatten
    val resultsDivs = importCSVGreedy(path, DividendRowMatcher).flatten

    val symbolMap: mutable.Map[String, List[OptionRowMTM]] = mutable.Map[String, List[OptionRowMTM]]()
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
      profitMap(key) = optList.foldLeft(0.0)(_ + _.profit)
    }

    // get stock sum
    for (stockRow <- resultsStock) {
      profitMap(stockRow.symbol) += stockRow.profit
    }

    // get divs sum
    for (divRow <- resultsDivs) {
      profitMap(divRow.symbol) += divRow.amount
    }

    //TODO: count dividend accruals ONLY IF in the future


    println("FINAL RESULTS: \n" + profitMap.toList.sortBy(_._1).map{case (sym, price) => sym + " -> " + price}.mkString("\n"))
  }
}
