package realized

import com.github.rs17.mulligan.DefaultModule
import dataio.CSVImporter.importCSVGreedy
import realized.MatchersRealized.{OpenOptionRow, OpenOptionRowMatcher, OpenStockRowMatcher}
import scala.collection.mutable

/** Summarizes Open positions by underlying symbol in various ways.  This is very particular to my own portfolio and
 * not likely useful for other investors */
object OpenSummarizerRealized extends DefaultModule {
  def main(args: Array[String]): Unit = {

    if (args.length == 0) throw new IllegalArgumentException("Must provide path argument")
    val path = args(0)

    val resultsStock = importCSVGreedy(path, OpenStockRowMatcher).flatten
    val resultsOpts = importCSVGreedy(path, OpenOptionRowMatcher).flatten

    val symbolMap: mutable.Map[String, List[OpenOptionRow]] = mutable.Map()
    val profitMap: mutable.Map[String, Double] = mutable.Map()

    // how much cash needed if all sold puts got executed?
    val allPutsRiskMap: mutable.Map[String, Double] = mutable.Map[String, Double]()
    // How much total option spread is there (not counting box reversal, basically same-side only)
    val spreadRiskMap: mutable.Map[String, Double] = mutable.Map[String, Double]()
    // TODO: How much will be gained/lost if there is rapid shift in interest rates (difficult)
    val interestRiskMap: mutable.Map[String, Double] = mutable.Map[String, Double]()

    // setup maps
    for (name <- (resultsOpts.map(_.symbolUnderlying) ++ resultsStock.map(_.symbol)).distinct) {
      symbolMap += name -> List()
      Seq(profitMap, allPutsRiskMap, spreadRiskMap, interestRiskMap).map(_ += name -> 0.0)
    }

    // add option rows
    for (optionRow <- resultsOpts) {
      symbolMap(optionRow.symbolUnderlying) = symbolMap(optionRow.symbolUnderlying) :+ optionRow
    }

    // get option sum, put risk, spread risk
    for ((key, optList) <- symbolMap) {
      profitMap(key) = optList.foldLeft(0.0)(_ + _.unrealizedProfit)
      allPutsRiskMap(key) = optList
        .filter(opt => opt.putCall == "P" && opt.size < 0)
        .foldLeft(0.0)((tot, opt) => tot + opt.strike * opt.size * 100)
      spreadRiskMap(key) = getSymbolSpreadRisk(optList)
    }

    // get stock sum
    for (stockRow <- resultsStock) {
      profitMap(stockRow.symbol) += stockRow.unrealizedProfit
    }

    println()
    println("FINAL RESULTS - PROFIT: \n" + profitMap.toList.sortBy(_._1).map { case (sym, price) => sym + " -> " + price }.mkString("\n"))
    println()
    println("PUT RISK: \n" +allPutsRiskMap.filter(_._2 != 0.0).toList.sortBy(_._1).map { case (sym, risk) => sym + " -> " + risk }.mkString("\n"))
    println()
    println("SPREAD RISK: \n" +spreadRiskMap.filter(_._2 != 0.0).toList.sortBy(_._1).map { case (sym, risk) => sym + " -> " + risk }.mkString("\n"))
  }

  /** for spreads, put in spread risk map by date if not found match, otherwise return amt
   *
   * @param openOptionRow
   * @return Amount of spread risk represented by position.  Sold call
   */

  // 1. Splity by date
  def getSymbolSpreadRisk(symbolOpts: List[OpenOptionRow]): Double = {
    val optsByDate: Map[String, List[OpenOptionRow]] = symbolOpts.groupBy(_.date)
    optsByDate.toList.foldLeft(0.0)((total, row) => total + getDateSpreadRisk(row._2))
  }

  // 2. Split by put/call,
  //Once done with calls, do puts. Compare amounts at end, should be same. If they are, one of them is the spread risk.
  // If not, warn
  def getDateSpreadRisk(symbolDateOpts: List[OpenOptionRow]): Double ={
    val (puts, calls) = symbolDateOpts.partition(_.putCall == "C")
    val putSideRisk = getSideSpreadRisk(puts)
    val callSideRisk = getSideSpreadRisk(calls)
    if(callSideRisk != putSideRisk){
      println("\n WARNING: Put and Call sides not equal " + symbolDateOpts.mkString("|") + " \n")
      Math.max(putSideRisk, callSideRisk)
    } else {
      println("got date spread risk " + symbolDateOpts.head.date + " " + symbolDateOpts.head.symbolUnderlying + ": " + putSideRisk)
      putSideRisk
    }
  }

  // then split by negative/positive.
  // Sum up all negative strike * amt  and same for positive.  Subtract pos from negative or vice versa depending on side.
  // do check to ensure sums are equal, otherwise this will get screwy
  def getSideSpreadRisk(symbolDateSideOpts: List[OpenOptionRow]):Double = {
    if(symbolDateSideOpts.foldLeft(0.0)((total, opt)=>total + opt.size)!= 0.0) {
      println("Warning, size mismatch" + symbolDateSideOpts.mkString("|"))
    }
    val optValue = symbolDateSideOpts.foldLeft(0.0)((total, opt)=> total + opt.strike * opt.size)

    symbolDateSideOpts.headOption match {
      case None => 0.0
      case Some(opt) if opt.putCall == "C" => optValue * 100
      case Some(opt) if opt.putCall == "P" => -optValue * 100
    }
  }

}
