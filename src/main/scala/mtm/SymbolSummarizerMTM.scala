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
    val symbol = if(args.length == 2) Some(args(1)) else None

    val resultsStock = importCSVGreedy(path, StockRowMatcherMTM).flatten.filter(stockRow => symbol.forall(_ == stockRow.symbol))
    val resultsOpts = importCSVGreedy(path, OptionRowMatcherMTM).flatten.filter(optionRow => symbol.forall(_ == optionRow.symbolUnderlying))
    val resultsDivs = importCSVGreedy(path, DividendRowMatcher).flatten.filter(divRow => symbol.forall(_ == divRow.symbol))

    val symbolMap: mutable.Map[String, List[OptionRowMTM]] = mutable.Map[String, List[OptionRowMTM]]()
    val symbolDateMap: mutable.Map[String, List[OptionRowMTM]] = mutable.Map[String, List[OptionRowMTM]]()
    val profitMap: mutable.Map[String, Double] = mutable.Map[String, Double]()
    val profitDateMap: mutable.Map[String, Double] = mutable.Map[String, Double]()

    // setup maps
    for (name <- (resultsOpts.map(_.symbolUnderlying) ++ resultsStock.map(_.symbol) ++ resultsDivs.map(_.symbol)).distinct) {
      symbolMap += name -> List()
      profitMap += name -> 0.0
      profitDateMap += name -> 0.0
    }

    // add option rows
    for (optionRow <- resultsOpts) {
      symbolMap(optionRow.symbolUnderlying) = symbolMap(optionRow.symbolUnderlying) :+ optionRow
      val symbolDateString = optionRow.symbolUnderlying + ":" + optionRow.date
      symbolDateMap.getOrElse(symbolDateString, symbolDateMap += symbolDateString -> List())
      symbolDateMap(symbolDateString) = symbolDateMap(symbolDateString) :+ optionRow
    }

    // get option sum
    for ((key, optList) <- symbolMap) {
      profitMap(key) = optList.foldLeft(0.0)(_ + _.profit)
    }
    for ((key, optList) <- symbolDateMap) {
      profitDateMap(key) = optList.foldLeft(0.0)(_ + _.profit)
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
    println("STOCK RESULTS: \n" + resultsStock.sortBy(_.symbol).map( sRow => sRow.symbol + " -> " + sRow.profit).mkString("\n"))
    println("OPTION RESULTS BY DATE: \n" + profitDateMap.toList.sortBy(_._1).map{case (symDate, profit) => symDate + " -> " + profit}.mkString("\n"))
    println("\nFINAL RESULTS: \n" + profitMap.toList.sortBy(_._1).map{case (sym, price) => sym + " -> " + price}.mkString("\n"))
  }
}
